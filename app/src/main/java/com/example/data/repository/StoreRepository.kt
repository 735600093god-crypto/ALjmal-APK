package com.example.data.repository

import com.example.data.model.CartItem
import com.example.data.model.Currency
import com.example.data.model.OrderSubmission
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.ProductCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class StoreRepository {

    val storePhone = "776662630"
    val storeWhatsApp = "967776662630"
    val storeLocation = "صنعاء - حدة داخل التوفير هايبر"

    val sanaaLocations = listOf(
        "حدة - داخل التوفير هايبر",
        "حدة - الحي الدبلوماسي",
        "السبعين - جوار الحديقة",
        "الأصبحي - الشارع الرئيسي",
        "الحي السياسي - شارع عمان",
        "شارع بغداد - تقاطع الجزائر",
        "الصافية - قرب جسر الصافية",
        "مذبح - جولة الستين",
        "الجراف - شارع المطار",
        "هائل - تقاطع الرباط",
        "الروضة - مخرج صنعاء"
    )

    private val initialProducts = listOf(
        // فواكه بلدي (Local Fruits)
        Product(
            id = "p1",
            name = "رمان صعدي بلدي فاخر",
            category = ProductCategory.LOCAL_FRUITS,
            priceYer = 28.0,
            originalPriceYer = 35.0,
            unit = "كجم",
            origin = "اليمن - صعدة",
            rating = 5.0,
            badge = "أشهر منتج بلدي",
            discountPercent = 20,
            imageUrl = "https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=600&q=80",
            description = "رمان صعدي بلدي عصيري مسكر ذو حبات حمراء داكنة مليئة بالفيتامينات ومضادات الأكسدة، قطاف فجر اليوم من مزارع صعدة الخصبة."
        ),
        Product(
            id = "p2",
            name = "عنب رازقي بلدي صنعاني",
            category = ProductCategory.LOCAL_FRUITS,
            priceYer = 30.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "اليمن - بني حشيش",
            rating = 4.9,
            badge = "عنب بني حشيش",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1537640538966-79f369143f8f?auto=format&fit=crop&w=600&q=80",
            description = "عنب رازقي صنعاني مشهور بحلاوته العالية وقشرته الرقيقة، يقطف بعناية فائقة من كروم بني حشيش المشهورة تاريخياً."
        ),
        Product(
            id = "p3",
            name = "تفاح بلدي صعدي مسكر",
            category = ProductCategory.LOCAL_FRUITS,
            priceYer = 25.0,
            originalPriceYer = 30.0,
            unit = "كجم",
            origin = "اليمن - صعدة",
            rating = 4.8,
            badge = "بلدي جبلي",
            discountPercent = 17,
            imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80",
            description = "تفاح صعدي أحمر طبيعي 100% بدون أي مواد كيميائية، يتميز برائحته العطرية المميزة وطعمه المقرمش اللذيذ."
        ),
        Product(
            id = "p4",
            name = "تين شوكي (بلس تركي) مقشر",
            category = ProductCategory.LOCAL_FRUITS,
            priceYer = 20.0,
            originalPriceYer = null,
            unit = "صحن 1 كجم",
            origin = "اليمن - غيمان / الحيمة",
            rating = 4.9,
            badge = "مقشر وجاهز",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1582293041079-7814c2f12063?auto=format&fit=crop&w=600&q=80",
            description = "بلس تركي مبرد ومقشر بعناية في صحون معقمة جاهزة للأكل مباشرة، غني بالألياف والمعادن المنعشة للصيف."
        ),
        Product(
            id = "p5",
            name = "شمام مأربي بلدي عسلي",
            category = ProductCategory.LOCAL_FRUITS,
            priceYer = 18.0,
            originalPriceYer = null,
            unit = "حبة (حوالي 2 كجم)",
            origin = "اليمن - مأرب",
            rating = 4.7,
            badge = "عسلي قطاف اليوم",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=600&q=80",
            description = "شمام عسلي شديد الحلاوة من مزارع مأرب المباركة، قوام ناعم غني بالعصير المنعش ومثالي للضيافة."
        ),
        Product(
            id = "p6",
            name = "مانجو تهامي بلدي قلب الثور",
            category = ProductCategory.LOCAL_FRUITS,
            priceYer = 35.0,
            originalPriceYer = 42.0,
            unit = "كجم",
            origin = "اليمن - تهامة / وادي سردود",
            rating = 5.0,
            badge = "زبدة تهامي فاخر",
            discountPercent = 17,
            imageUrl = "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=600&q=80",
            description = "مانجو قلب الثور والسمكة التهامي الشهير، رائحة فواحة بدون ألياف، يذوب كالكريم في الفم، جودة نخب أول للتصدير."
        ),

        // فواكه مستوردة (Imported Fruits)
        Product(
            id = "p7",
            name = "تفاح رويال غالا واشنطن",
            category = ProductCategory.IMPORTED_FRUITS,
            priceYer = 22.0,
            originalPriceYer = 28.0,
            unit = "كجم",
            origin = "الولايات المتحدة (واشنطن)",
            rating = 4.8,
            badge = "مستورد نخب أول",
            discountPercent = 21,
            imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80",
            description = "تفاح رويال غالا أمريكي أصلي من مزارع ولاية واشنطن، مقرمش حلو المذاق ومتناسق الحجم واللون."
        ),
        Product(
            id = "p8",
            name = "أفوكادو هاس مكسيكي كريمي",
            category = ProductCategory.IMPORTED_FRUITS,
            priceYer = 35.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "المكسيك",
            rating = 4.9,
            badge = "كريمي ممتاز",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1523049673857-eb18f1d7b578?auto=format&fit=crop&w=600&q=80",
            description = "أفوكادو هاس مستورد ناضج ذو قوام زبدي غني بالدهون الصحية والأوميغا 3، مثالي للسلطات والعصائر الصحية."
        ),
        Product(
            id = "p9",
            name = "موز إكوادوري نخب أول",
            category = ProductCategory.IMPORTED_FRUITS,
            priceYer = 14.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "الإكوادور",
            rating = 4.7,
            badge = "نخب أول",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?auto=format&fit=crop&w=600&q=80",
            description = "موز إكوادوري نخب أول حبات كبيرة متماسكة ومغلفة بعناية، مصدر طاقة يومي ممتاز لجميع أفراد الأسرة."
        ),
        Product(
            id = "p10",
            name = "كيوي ذهبي زيسبري",
            category = ProductCategory.IMPORTED_FRUITS,
            priceYer = 40.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "نيوزيلندا",
            rating = 5.0,
            badge = "فيتامين سي مركز",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1518492104633-130d0cc84637?auto=format&fit=crop&w=600&q=80",
            description = "كيوي ذهبي ماركة Zespri النيوزيلندية الشهيرة، حلاوة استثنائية ونسبة مضاعفة من فيتامين C لتعزيز المناعة."
        ),
        Product(
            id = "p11",
            name = "برتقال أبو صرة إسباني",
            category = ProductCategory.IMPORTED_FRUITS,
            priceYer = 18.0,
            originalPriceYer = 22.0,
            unit = "كجم",
            origin = "إسبانيا",
            rating = 4.7,
            badge = "حلو وعصيري",
            discountPercent = 18,
            imageUrl = "https://images.unsplash.com/photo-1547514701-42782101795e?auto=format&fit=crop&w=600&q=80",
            description = "برتقال إسباني طازج خالي من البذور، حلو ومليء بالعصير المنعش، مثالي للأكل المباشر والعصر اليومي."
        ),
        Product(
            id = "p12",
            name = "أناناس كوستاريكي ذهبي حلو",
            category = ProductCategory.IMPORTED_FRUITS,
            priceYer = 30.0,
            originalPriceYer = null,
            unit = "حبة كبيرة",
            origin = "كوستاريكا",
            rating = 4.9,
            badge = "استوائي حلو",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1550258987-190a2d41a8ba?auto=format&fit=crop&w=600&q=80",
            description = "أناناس استوائي كوستاريكي ذهبي مكتمل النضج ذو حلاوة فائقة وعطر منعش يضفي متعة خاصة لمائدتكم."
        ),

        // خضروات (Vegetables)
        Product(
            id = "p13",
            name = "طماطم بلدي طازجة عروق",
            category = ProductCategory.VEGETABLES,
            priceYer = 10.0,
            originalPriceYer = 14.0,
            unit = "كجم",
            origin = "اليمن - تهامة / مأرب",
            rating = 4.9,
            badge = "قطاف فجر اليوم",
            discountPercent = 29,
            imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=600&q=80",
            description = "طماطم بلدي طازجة حمراء وناضجة بعروقها الخضراء، طعم غني أصيل للسلطات والطبخ اليمني التقليدي."
        ),
        Product(
            id = "p14",
            name = "خيار بلدي مقرمش طازج",
            category = ProductCategory.VEGETABLES,
            priceYer = 12.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "اليمن - حوض صنعاء",
            rating = 4.8,
            badge = "بلدي مقرمش",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1449300079323-02e209d9d3a6?auto=format&fit=crop&w=600&q=80",
            description = "خيار بلدي صغير الحجم مقرمش ذو رائحة ترابية منعشة، يقطف فجراً من مزارع حوض صنعاء المروية بالمياه العذبة."
        ),
        Product(
            id = "p15",
            name = "باقة خضار ورقية بلدي طازجة",
            category = ProductCategory.VEGETABLES,
            priceYer = 8.0,
            originalPriceYer = null,
            unit = "باقة مشكلة",
            origin = "مزارع أودية صنعاء",
            rating = 4.8,
            badge = "طازج معطر",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1540420773420-3366772f4999?auto=format&fit=crop&w=600&q=80",
            description = "تشكيلة غنية تضم جرجير، بقدونس، كزبرة، شبت ونعناع بلدي معطر مغسول بعناية وجاهز للمائدة."
        ),
        Product(
            id = "p16",
            name = "بطاطس يمني بلدي جبلية",
            category = ProductCategory.VEGETABLES,
            priceYer = 15.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "اليمن - ذمار / يريم",
            rating = 4.8,
            badge = "جبلية ممتازة",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1518977676601-b53f82aba655?auto=format&fit=crop&w=600&q=80",
            description = "بطاطس ذمارية جبلية خالية من الرطوبة الزائدة، ممتازة للقلي والتحمير وتمنح قرمشة ذهبية لا تقاوم."
        ),
        Product(
            id = "p17",
            name = "كوسة بلدي طازجة للطبخ",
            category = ProductCategory.VEGETABLES,
            priceYer = 14.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "اليمن - حوض صنعاء",
            rating = 4.7,
            badge = "طرية صغيرة",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?auto=format&fit=crop&w=600&q=80",
            description = "كوسة بلدي خضراء صغيرة ناعمة ممتازة للمحاشي والشوربات والطبخات المنزلية الصحية الخفيفة."
        ),
        Product(
            id = "p18",
            name = "بروكلي هولندي طازج",
            category = ProductCategory.VEGETABLES,
            priceYer = 25.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "هولندا",
            rating = 4.8,
            badge = "نخب أول",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1459411621453-7b03977f4bfc?auto=format&fit=crop&w=600&q=80",
            description = "رؤوس بروكلي طازجة متماسكة وخضراء داكنة غنية بمضادات الأكسدة والمعادن الأساسية، مثالية للأكلات الصحية."
        ),
        Product(
            id = "p19",
            name = "بصل أحمر بلدي عالي الجودة",
            category = ProductCategory.VEGETABLES,
            priceYer = 12.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "اليمن - تهامة",
            rating = 4.8,
            badge = "يدوم طويلاً",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?auto=format&fit=crop&w=600&q=80",
            description = "بصل تهامي أحمر حاد النكهة وجاف تماماً للتخزين الطويل، ركيزة أساسية لكل طبخة يمنية أصيلة."
        ),

        // تمور فاخرة (Premium Dates)
        Product(
            id = "p20",
            name = "تمر مجدول فاخر ملكي",
            category = ProductCategory.PREMIUM_DATES,
            priceYer = 50.0,
            originalPriceYer = 65.0,
            unit = "كجم",
            origin = "اليمن - حضرموت",
            rating = 4.9,
            badge = "الأكثر طلباً",
            discountPercent = 23,
            imageUrl = "https://images.unsplash.com/photo-1596797038530-2c107229654b?auto=format&fit=crop&w=600&q=80",
            description = "تمر مجدول حضرمي حبات جامبو ملكية ناعمة، قوام كراميلي فاخر مناسب للضيافة الراقية والهدايا."
        ),
        Product(
            id = "p21",
            name = "تمر عجوة المدينة المباركة",
            category = ProductCategory.PREMIUM_DATES,
            priceYer = 65.0,
            originalPriceYer = 80.0,
            unit = "كجم",
            origin = "المدينة المنورة",
            rating = 5.0,
            badge = "فاخر ممتاز",
            discountPercent = 19,
            imageUrl = "https://images.unsplash.com/photo-1509358271058-acd22cc93898?auto=format&fit=crop&w=600&q=80",
            description = "عجوة عالية المدينة المنورة المباركة، نخب أول منتقى حبة حبة، بركة وشفاء وغذاء فاخر للروح والجسد."
        ),
        Product(
            id = "p22",
            name = "تمر سكري رطب القصيم",
            category = ProductCategory.PREMIUM_DATES,
            priceYer = 45.0,
            originalPriceYer = 55.0,
            unit = "كجم",
            origin = "القصيم",
            rating = 4.8,
            badge = "رطب ذهبي",
            discountPercent = 18,
            imageUrl = "https://images.unsplash.com/photo-1549488344-cbb6c34cf08b?auto=format&fit=crop&w=600&q=80",
            description = "تمر سكري رطب ذهبي من بساتين القصيم، يذوب في الفم مع القهوة العربية والبن اليمني الفاخر."
        ),
        Product(
            id = "p23",
            name = "تمر خلاص الأحساء فاخر",
            category = ProductCategory.PREMIUM_DATES,
            priceYer = 40.0,
            originalPriceYer = 50.0,
            unit = "كجم",
            origin = "الأحساء",
            rating = 4.9,
            badge = "نخب أول",
            discountPercent = 20,
            imageUrl = "https://images.unsplash.com/photo-1574886616037-33bf320c2423?auto=format&fit=crop&w=600&q=80",
            description = "خلاص الأحساء الملكي المشهور بلمعانه وعسله الطبيعي الكثيف، الخيار المفضل للضيافة اليومية."
        ),
        Product(
            id = "p24",
            name = "تمر صقعي محشو باللوز والهيل",
            category = ProductCategory.PREMIUM_DATES,
            priceYer = 70.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "محلي يدوي",
            rating = 4.9,
            badge = "للضيافة",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1509358271058-acd22cc93898?auto=format&fit=crop&w=600&q=80",
            description = "تمر صقعي فاخر محشو باللوز المحمص ومتبل بنكهة الهيل والزعفران، مجهز بأيدي ماهرة لمناسباتكم الخاصة."
        ),

        // فواكه مجففة (Dried Fruits)
        Product(
            id = "p25",
            name = "تين تركي مجفف فاخر",
            category = ProductCategory.DRIED_FRUITS,
            priceYer = 55.0,
            originalPriceYer = 68.0,
            unit = "كجم",
            origin = "تركيا (إزمير)",
            rating = 4.9,
            badge = "طبيعي 100%",
            discountPercent = 19,
            imageUrl = "https://images.unsplash.com/photo-1582293041079-7814c2f12063?auto=format&fit=crop&w=600&q=80",
            description = "تين إزمير التركي المجفف شمسياً دون إضافات سكرية، طري ومحشو بالعسل الطبيعي ومليء بالفيتامينات."
        ),
        Product(
            id = "p26",
            name = "مشمش مجفف ملاطية نخب أول",
            category = ProductCategory.DRIED_FRUITS,
            priceYer = 48.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "تركيا (ملاطية)",
            rating = 4.8,
            badge = "طري وغني بالألياف",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1608686207856-001b95cf60ca?auto=format&fit=crop&w=600&q=80",
            description = "مشمش ملاطية المجفف برتقالي زاهي طري ولذيذ، يساعد على الهضم ويمنح طاقة حيوية فورية."
        ),
        Product(
            id = "p27",
            name = "زبيب بلدي أسود رازقي",
            category = ProductCategory.DRIED_FRUITS,
            priceYer = 52.0,
            originalPriceYer = 62.0,
            unit = "كجم",
            origin = "اليمن - خولان / صنعاء",
            rating = 5.0,
            badge = "دواء وغذاء",
            discountPercent = 16,
            imageUrl = "https://images.unsplash.com/photo-1533230408708-8f9f91d1235a?auto=format&fit=crop&w=600&q=80",
            description = "زبيب خولاني أسود غني بالحديد الطبيعي ومضادات الأكسدة، علاج شعبي مجرب للأنيميا وتقوية الذاكرة."
        ),
        Product(
            id = "p28",
            name = "زبيب بلدي ذهبي فاخر",
            category = ProductCategory.DRIED_FRUITS,
            priceYer = 46.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "اليمن",
            rating = 4.8,
            badge = "نقي وفاخر",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=600&q=80",
            description = "زبيب عنب بياض يمني ذهبي مجفف بطريقة تقليدية نقية، حبة مليئة بالرحيق وحلوة المذاق كالعسل."
        ),
        Product(
            id = "p29",
            name = "قراصيا (برقوق مجفف) بدون نوى",
            category = ProductCategory.DRIED_FRUITS,
            priceYer = 42.0,
            originalPriceYer = null,
            unit = "كجم",
            origin = "تشيلي",
            rating = 4.7,
            badge = "طبيعي ممتاز",
            discountPercent = null,
            imageUrl = "https://images.unsplash.com/photo-1596797038530-2c107229654b?auto=format&fit=crop&w=600&q=80",
            description = "برقوق مجفف مستورد منزوع النوى ناعم وداكن، غني بالألياف ومضادات الأكسدة ومثالي للأنظمة الصحية."
        ),
        Product(
            id = "p30",
            name = "صينية فواكه مجففة مشكلة للضيافة",
            category = ProductCategory.DRIED_FRUITS,
            priceYer = 90.0,
            originalPriceYer = 110.0,
            unit = "صينية 1.5 كجم",
            origin = "تشكيلة مختارة",
            rating = 5.0,
            badge = "هدية وضيافة",
            discountPercent = 18,
            imageUrl = "https://images.unsplash.com/photo-1608686207856-001b95cf60ca?auto=format&fit=crop&w=600&q=80",
            description = "صينية فاخرة مزينة تضم تين، مشمش، زبيب أسود وذهبي، قراصيا، لوز، وجوز، مغلفة بشريط ذهبي أنيق للضيافة والأعياد."
        )
    )

    private val _products = MutableStateFlow(initialProducts)
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cart = MutableStateFlow<Map<String, Int>>(
        mapOf("p1" to 1, "p13" to 1) // default items from HTML mockup
    )
    val cart: StateFlow<Map<String, Int>> = _cart.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(
        setOf("p1", "p13") // default favorites from HTML
    )
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    // 4 Slots for exclusive offers (default empty as in HTML: 0 من 4 خانات)
    private val _offerSlotProductIds = MutableStateFlow<List<String>>(emptyList())
    val offerSlotProductIds: StateFlow<List<String>> = _offerSlotProductIds.asStateFlow()

    private val _deliveryLocation = MutableStateFlow("حدة - داخل التوفير هايبر")
    val deliveryLocation: StateFlow<String> = _deliveryLocation.asStateFlow()

    private val _currency = MutableStateFlow(Currency.YER)
    val currency: StateFlow<Currency> = _currency.asStateFlow()

    fun setCurrency(newCurrency: Currency) {
        _currency.value = newCurrency
    }

    fun setDeliveryLocation(location: String) {
        _deliveryLocation.value = location
    }

    fun toggleFavorite(productId: String) {
        _favorites.update { current ->
            if (current.contains(productId)) current - productId else current + productId
        }
    }

    fun addToCart(productId: String) {
        _cart.update { current ->
            val count = current[productId] ?: 0
            current + (productId to count + 1)
        }
    }

    fun decrementFromCart(productId: String) {
        _cart.update { current ->
            val count = current[productId] ?: 0
            if (count <= 1) {
                current - productId
            } else {
                current + (productId to count - 1)
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cart.update { current ->
            current - productId
        }
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    fun toggleOfferSlot(productId: String) {
        _offerSlotProductIds.update { slots ->
            if (slots.contains(productId)) {
                slots - productId
            } else {
                if (slots.size < 4) {
                    slots + productId
                } else {
                    slots // max 4 slots
                }
            }
        }
    }

    fun updateProductPrice(productId: String, newPriceYer: Double, newOriginalPriceYer: Double?) {
        _products.update { list ->
            list.map { product ->
                if (product.id == productId) {
                    val discount = if (newOriginalPriceYer != null && newOriginalPriceYer > newPriceYer) {
                        (((newOriginalPriceYer - newPriceYer) / newOriginalPriceYer) * 100).toInt()
                    } else null

                    product.copy(
                        priceYer = newPriceYer,
                        originalPriceYer = newOriginalPriceYer,
                        discountPercent = discount
                    )
                } else {
                    product
                }
            }
        }
    }

    fun addNewProduct(product: Product) {
        _products.update { listOf(product) + it }
    }
}
