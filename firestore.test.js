const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ADMIN_UID = "admin_owner_uid";
const ADMIN_EMAIL = "akrmshaker8@gmail.com";
const ALICE_UID = "alice_customer_123";
const BOB_UID = "bob_customer_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function nowTimestamp() {
  return new Date(Date.now() - 1000);
}

function validProductPayload(ownerId) {
  const ts = nowTimestamp();
  return {
    storeId: "aljamal_main",
    ownerId,
    name: "رمان صعدي بلدي",
    categoryId: "local_fruits",
    priceYer: 2800,
    originalPriceYer: 3500,
    unit: "كجم",
    origin: "اليمن - صعدة",
    rating: 5.0,
    badge: "طازج",
    imageUrl: "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?w=600",
    discountPercent: 20,
    description: "رمان صعدي بلدي فاخر",
    inStock: true,
    createdAt: ts,
    updatedAt: ts,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated shopper: can list filtered public catalog, denied writes and private collections", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("store_products").doc("p1").set(validProductPayload(ADMIN_UID));
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  // StoreId-filtered storefront read is allowed for shoppers without login
  await assertSucceeds(unauthDb.collection("store_products").where("storeId", "==", "aljamal_main").get());

  // Unfiltered list still denied
  await assertFails(unauthDb.collection("store_products").get());
  // Writes and private collections strictly denied
  await assertFails(unauthDb.collection("store_products").doc("p2").set(validProductPayload(ALICE_UID)));
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("admin_security").doc("pin_config").get());
  await assertFails(unauthDb.collection("security_logs").where("userId", "==", ALICE_UID).get());
});

test("Email spoofing attack: unverified admin email cannot write store_products", async () => {
  const spoofedDb = testEnv.authenticatedContext("attacker_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();

  await assertFails(
    spoofedDb.collection("store_products").doc("p1").set(validProductPayload("attacker_uid"))
  );
});

test("Customer user: cannot write, update, or delete store_products or escalate role", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  // Customer cannot publish a product
  await assertFails(
    aliceDb.collection("store_products").doc("p1").set(validProductPayload(ALICE_UID))
  );

  // Customer cannot self-assign admin in /admins
  const ts = nowTimestamp();
  await assertFails(
    aliceDb.collection("admins").doc(ALICE_UID).set({
      uid: ALICE_UID,
      email: "alice@example.com",
      grantedBy: ALICE_UID,
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Customer cannot self-assign role: 'admin' in /users/{uid}
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      role: "admin",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  // Customer CAN create their own valid customer profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      role: "customer",
      createdAt: ts,
      updatedAt: ts,
    })
  );
});

test("PII isolation: customer cannot read another user's profile", async () => {
  const ts = nowTimestamp();
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      email: "bob@example.com",
      role: "customer",
      createdAt: ts,
      updatedAt: ts,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  await assertFails(aliceDb.collection("users").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("admin_security").doc("pin_config").get());
});

test("Query alignment: unfiltered list on store_products fails, filtered by storeId succeeds for signed-in user", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("store_products").doc("p1").set(validProductPayload(ADMIN_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  // Unfiltered list fails (rules are not filters)
  await assertFails(aliceDb.collection("store_products").get());

  // Filtered list aligned with rule succeeds
  await assertSucceeds(
    aliceDb.collection("store_products").where("storeId", "==", "aljamal_main").get()
  );
});

test("Bootstrapped verified admin: can publish products, reject shadow ghost fields & value poisoning", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();

  // Valid publish succeeds
  await assertSucceeds(
    adminDb.collection("store_products").doc("p1").set(validProductPayload(ADMIN_UID))
  );

  // Shadow update with ghost field fails
  const shadowPayload = { ...validProductPayload(ADMIN_UID), isVerified: true };
  await assertFails(
    adminDb.collection("store_products").doc("p2").set(shadowPayload)
  );

  // Value poisoning (negative price / bad image scheme) fails
  const badPrice = { ...validProductPayload(ADMIN_UID), priceYer: -50 };
  await assertFails(
    adminDb.collection("store_products").doc("p3").set(badPrice)
  );

  const badUrl = { ...validProductPayload(ADMIN_UID), imageUrl: "javascript:alert(1)" };
  await assertFails(
    adminDb.collection("store_products").doc("p4").set(badUrl)
  );
});

test("Admin security config & append-only logs protection", async () => {
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  const ts = nowTimestamp();
  const validPinConfig = {
    ownerId: ADMIN_UID,
    pinHash: "a".repeat(64),
    pinSalt: "0123456789abcdef0123456789abcdef",
    createdAt: ts,
    updatedAt: ts,
  };

  // Admin can write and read pin_config
  await assertSucceeds(adminDb.collection("admin_security").doc("pin_config").set(validPinConfig));
  await assertSucceeds(adminDb.collection("admin_security").doc("pin_config").get());

  // Customer cannot read or write pin_config
  await assertFails(aliceDb.collection("admin_security").doc("pin_config").get());
  await assertFails(aliceDb.collection("admin_security").doc("pin_config").set(validPinConfig));

  // Security logs are append-only: create allowed for own UID, update/delete rejected
  await assertSucceeds(
    aliceDb.collection("security_logs").doc("log_1").set({
      userId: ALICE_UID,
      eventType: "FAILED_PIN",
      details: "محاولة إدخال رمز غير صحيح",
      createdAt: ts,
    })
  );
  await assertFails(
    aliceDb.collection("security_logs").doc("log_1").update({ details: "tampered" })
  );
  await assertFails(
    aliceDb.collection("security_logs").doc("log_1").delete()
  );
});
