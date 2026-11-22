package com.example.data.content

import com.example.i18n.BiText

data class CurrencyInfo(
    val code: String,
    val symbol: String,
    val flag: String,
    val name: BiText,
    val country: BiText,
    val defaultUnitsPerUsd: Double,
    val isWeldingPopular: Boolean = false
)

object CurrencyCatalog {

    val pinnedWeldingCodes = listOf(
        "SAR", "AED", "QAR", "KWD", "OMR", "BHD", "MYR", "SGD", "USD", "EUR"
    )

    val allCurrencies: List<CurrencyInfo> = listOf(
        // Home Currency & Popular Welding / Overseas Destinations first
        CurrencyInfo("BDT", "৳", "🇧🇩", BiText("Bangladeshi Taka", "বাংলাদেশি টাকা"), BiText("Bangladesh", "বাংলাদেশ"), 119.50, true),
        CurrencyInfo("SAR", "﷼", "🇸🇦", BiText("Saudi Riyal", "সৌদি রিয়াল"), BiText("Saudi Arabia", "সৌদি আরব"), 3.75, true),
        CurrencyInfo("AED", "د.إ", "🇦🇪", BiText("UAE Dirham", "সংযুক্ত আরব আমিরাত দিরহাম"), BiText("United Arab Emirates", "সংযুক্ত আরব আমিরাত"), 3.6725, true),
        CurrencyInfo("QAR", "﷼", "🇶🇦", BiText("Qatari Riyal", "কাতারি রিয়াল"), BiText("Qatar", "কাতার"), 3.64, true),
        CurrencyInfo("KWD", "د.ك", "🇰🇼", BiText("Kuwaiti Dinar", "কুয়েতি দিনার"), BiText("Kuwait", "কুয়েত"), 0.307, true),
        CurrencyInfo("OMR", "ر.ع.", "🇴🇲", BiText("Omani Rial", "ওমানি রিয়াল"), BiText("Oman", "ওমান"), 0.3845, true),
        CurrencyInfo("BHD", ".د.ب", "🇧🇭", BiText("Bahraini Dinar", "বাহরাইনি দিনার"), BiText("Bahrain", "বাহরাইন"), 0.376, true),
        CurrencyInfo("MYR", "RM", "🇲🇾", BiText("Malaysian Ringgit", "মালয়েশিয়ান রিঙ্গিত"), BiText("Malaysia", "মালয়েশিয়া"), 4.45, true),
        CurrencyInfo("SGD", "S$", "🇸🇬", BiText("Singapore Dollar", "সিঙ্গাপুর ডলার"), BiText("Singapore", "সিঙ্গাপুর"), 1.34, true),
        CurrencyInfo("USD", "$", "🇺🇸", BiText("US Dollar", "মার্কিন ডলার"), BiText("United States", "যুক্তরাষ্ট্র"), 1.0, true),
        CurrencyInfo("EUR", "€", "🇪🇺", BiText("Euro", "ইউরো"), BiText("Eurozone", "ইউরোপীয় ইউনিয়ন"), 0.94, true),

        // Major Global & Regional Currencies (A-Z)
        CurrencyInfo("AFN", "؋", "🇦🇫", BiText("Afghan Afghani", "আফগান আফগানি"), BiText("Afghanistan", "আফগানিস্তান"), 68.5),
        CurrencyInfo("ALL", "L", "🇦🇱", BiText("Albanian Lek", "আলবেনিয়ান লেক"), BiText("Albania", "আলবেনিয়া"), 92.5),
        CurrencyInfo("DZD", "د.ج", "🇩🇿", BiText("Algerian Dinar", "আলজেরিয়ান দিনার"), BiText("Algeria", "আলজেরিয়া"), 133.5),
        CurrencyInfo("AOA", "Kz", "🇦🇴", BiText("Angolan Kwanza", "অ্যাঙ্গোলান কোয়ানজা"), BiText("Angola", "অ্যাঙ্গোলা"), 912.0),
        CurrencyInfo("ARS", "$", "🇦🇷", BiText("Argentine Peso", "আর্জেন্টাইন পেসো"), BiText("Argentina", "আর্জেন্টিনা"), 1005.0),
        CurrencyInfo("AMD", "֏", "🇦🇲", BiText("Armenian Dram", "আর্মেনিয়ান ড্রাম"), BiText("Armenia", "আর্মেনিয়া"), 388.0),
        CurrencyInfo("AUD", "A$", "🇦🇺", BiText("Australian Dollar", "অস্ট্রেলিয়ান ডলার"), BiText("Australia", "অস্ট্রেলিয়া"), 1.54),
        CurrencyInfo("AZN", "₼", "🇦🇿", BiText("Azerbaijani Manat", "আজারবাইজানি মানাত"), BiText("Azerbaijan", "আজারবাইজান"), 1.70),
        CurrencyInfo("BSD", "$", "🇧🇸", BiText("Bahamian Dollar", "বাহামিয়ান ডলার"), BiText("Bahamas", "বাহামা"), 1.00),
        CurrencyInfo("BBD", "$", "🇧🇧", BiText("Barbadian Dollar", "বার্বাডিয়ান ডলার"), BiText("Barbados", "বার্বাডোস"), 2.00),
        CurrencyInfo("BYN", "Br", "🇧🇾", BiText("Belarusian Ruble", "বেলারুশিয়ান রুবল"), BiText("Belarus", "বেলারুশ"), 3.27),
        CurrencyInfo("BZD", "$", "🇧🇿", BiText("Belize Dollar", "বেলিজ ডলার"), BiText("Belize", "বেলিজ"), 2.00),
        CurrencyInfo("BTN", "Nu.", "🇧🇹", BiText("Bhutanese Ngultrum", "ভুটানি গুলট্রাম"), BiText("Bhutan", "ভুটান"), 84.4),
        CurrencyInfo("BOB", "Bs.", "🇧🇴", BiText("Bolivian Boliviano", "বলিভিয়ান বলিভিয়ানো"), BiText("Bolivia", "বলিভিয়া"), 6.91),
        CurrencyInfo("BAM", "KM", "🇧🇦", BiText("Bosnia Convertible Mark", "বসনিয়া মার্ক"), BiText("Bosnia & Herzegovina", "বসনিয়া"), 1.84),
        CurrencyInfo("BWP", "P", "🇧🇼", BiText("Botswana Pula", "বতসোয়ানা পুলা"), BiText("Botswana", "বতসোয়ানা"), 13.6),
        CurrencyInfo("BRL", "R$", "🇧🇷", BiText("Brazilian Real", "ব্রাজিলিয়ান রিয়াল"), BiText("Brazil", "ব্রাজিল"), 5.78),
        CurrencyInfo("BND", "B$", "🇧🇳", BiText("Brunei Dollar", "ব্রুনাই ডলার"), BiText("Brunei", "ব্রুনাই"), 1.34),
        CurrencyInfo("BGN", "лв", "🇧🇬", BiText("Bulgarian Lev", "বুলগেরিয়ান লেভ"), BiText("Bulgaria", "বুলগেরিয়া"), 1.84),
        CurrencyInfo("KHR", "៛", "🇰🇭", BiText("Cambodian Riel", "কম্বোডিয়ান রিয়েল"), BiText("Cambodia", "কম্বোডিয়া"), 4065.0),
        CurrencyInfo("CAD", "C$", "🇨🇦", BiText("Canadian Dollar", "কানাডিয়ান ডলার"), BiText("Canada", "কানাডা"), 1.40),
        CurrencyInfo("CLP", "$", "🇨🇱", BiText("Chilean Peso", "চিলিয়ান পেসো"), BiText("Chile", "চিলি"), 975.0),
        CurrencyInfo("CNY", "¥", "🇨🇳", BiText("Chinese Yuan", "চীনা ইউয়ান"), BiText("China", "চীন"), 7.24),
        CurrencyInfo("COP", "$", "🇨🇴", BiText("Colombian Peso", "কলম্বিয়ান পেসো"), BiText("Colombia", "কলম্বিয়া"), 4410.0),
        CurrencyInfo("CRC", "₡", "🇨🇷", BiText("Costa Rican Colón", "কোস্টারিকান কোলন"), BiText("Costa Rica", "কোস্টারিকা"), 512.0),
        CurrencyInfo("CUP", "$", "🇨🇺", BiText("Cuban Peso", "কিউবান পেসো"), BiText("Cuba", "কিউবা"), 24.0),
        CurrencyInfo("CZK", "Kč", "🇨🇿", BiText("Czech Koruna", "চেক কোরুনা"), BiText("Czechia", "চেকিয়া"), 23.8),
        CurrencyInfo("DKK", "kr", "🇩🇰", BiText("Danish Krone", "ড্যানিশ ক্রোন"), BiText("Denmark", "ডেনমার্ক"), 7.02),
        CurrencyInfo("DOP", "RD$", "🇩🇴", BiText("Dominican Peso", "ডোমিনিকান পেসো"), BiText("Dominican Republic", "ডোমিনিকান প্রজাতন্ত্র"), 60.2),
        CurrencyInfo("EGP", "E£", "🇪🇬", BiText("Egyptian Pound", "মিশরীয় পাউন্ড"), BiText("Egypt", "মিশর"), 49.6),
        CurrencyInfo("ETB", "Br", "🇪🇹", BiText("Ethiopian Birr", "ইথিওপিয়ান বির"), BiText("Ethiopia", "ইথিওপিয়া"), 124.0),
        CurrencyInfo("FJD", "FJ$", "🇫🇯", BiText("Fijian Dollar", "ফিজিয়ান ডলার"), BiText("Fiji", "ফিজি"), 2.26),
        CurrencyInfo("GEL", "₾", "🇬🇪", BiText("Georgian Lari", "জর্জিয়ান লারি"), BiText("Georgia", "জর্জিয়া"), 2.73),
        CurrencyInfo("GHS", "₵", "🇬🇭", BiText("Ghanaian Cedi", "ঘানাইয়ান সেডি"), BiText("Ghana", "ঘানা"), 15.8),
        CurrencyInfo("GTQ", "Q", "🇬🇹", BiText("Guatemalan Quetzal", "গুয়াতেমালান কোয়েটজাল"), BiText("Guatemala", "গুয়াতেমালা"), 7.72),
        CurrencyInfo("HKD", "HK$", "🇭🇰", BiText("Hong Kong Dollar", "হংকং ডলার"), BiText("Hong Kong", "হংকং"), 7.78),
        CurrencyInfo("HUF", "Ft", "🇭🇺", BiText("Hungarian Forint", "হাঙ্গেরিয়ান ফোরিন্ট"), BiText("Hungary", "হাঙ্গেরি"), 386.0),
        CurrencyInfo("ISK", "kr", "🇮🇸", BiText("Icelandic Króna", "আইসল্যান্ডিক ক্রোনা"), BiText("Iceland", "আইসল্যান্ড"), 138.0),
        CurrencyInfo("INR", "₹", "🇮🇳", BiText("Indian Rupee", "ভারতীয় রুপি"), BiText("India", "ভারত"), 84.4),
        CurrencyInfo("IDR", "Rp", "🇮🇩", BiText("Indonesian Rupiah", "ইন্দোনেশিয়ান রুপিয়া"), BiText("Indonesia", "ইন্দোনেশিয়া"), 15890.0),
        CurrencyInfo("IRR", "﷼", "🇮🇷", BiText("Iranian Rial", "ইরানি রিয়াল"), BiText("Iran", "ইরান"), 42000.0),
        CurrencyInfo("IQD", "ع.দ", "🇮🇶", BiText("Iraqi Dinar", "ইরাকি দিনার"), BiText("Iraq", "ইরাক"), 1310.0),
        CurrencyInfo("ILS", "₪", "🇮🇱", BiText("Israeli New Shekel", "ইসরায়েলি শেকেল"), BiText("Israel", "ইসরায়েল"), 3.74),
        CurrencyInfo("JMD", "J$", "🇯🇲", BiText("Jamaican Dollar", "জ্যামাইকান ডলার"), BiText("Jamaica", "জ্যামাইকা"), 158.0),
        CurrencyInfo("JPY", "¥", "🇯🇵", BiText("Japanese Yen", "জাপানি ইয়েন"), BiText("Japan", "জাপান"), 154.5),
        CurrencyInfo("JOD", "د.ا", "🇯🇴", BiText("Jordanian Dinar", "জর্ডানীয় দিনার"), BiText("Jordan", "জর্ডান"), 0.709),
        CurrencyInfo("KZT", "₸", "🇰🇿", BiText("Kazakhstani Tenge", "কাজাখস্তানি টেঙ্গে"), BiText("Kazakhstan", "কাজাখস্তান"), 495.0),
        CurrencyInfo("KES", "KSh", "🇰🇪", BiText("Kenyan Shilling", "কেনিয়ান শিলিং"), BiText("Kenya", "কেনিয়া"), 129.0),
        CurrencyInfo("KRW", "₩", "🇰🇷", BiText("South Korean Won", "দক্ষিণ কোরিয়ান ওন"), BiText("South Korea", "দক্ষিণ কোরিয়া"), 1395.0),
        CurrencyInfo("KGS", "с", "🇰🇬", BiText("Kyrgyzstani Som", "কিরগিজস্তানি সোম"), BiText("Kyrgyzstan", "কিরগিজস্তান"), 86.5),
        CurrencyInfo("LAK", "₭", "🇱🇦", BiText("Lao Kip", "লাও কিপ"), BiText("Laos", "লাওস"), 21900.0),
        CurrencyInfo("LBP", "ل.ل", "🇱🇧", BiText("Lebanese Pound", "লেবানিজ পাউন্ড"), BiText("Lebanon", "লেবানন"), 89500.0),
        CurrencyInfo("LYD", "ل.দ", "🇱🇾", BiText("Libyan Dinar", "লিবিয়ান দিনার"), BiText("Libya", "লিবিয়া"), 4.86),
        CurrencyInfo("MOP", "MOP$", "🇲🇴", BiText("Macanese Pataca", "ম্যাকাও পাতাকা"), BiText("Macau", "ম্যাকাও"), 8.01),
        CurrencyInfo("MVR", "Rf", "🇲🇻", BiText("Maldivian Rufiyaa", "মালদ্বীপিয়ান রুফিয়া"), BiText("Maldives", "মালদ্বীপ"), 15.4),
        CurrencyInfo("MUR", "₨", "🇲🇺", BiText("Mauritian Rupee", "মরিশাস রুপি"), BiText("Mauritius", "মরিশাস"), 46.5),
        CurrencyInfo("MXN", "$", "🇲🇽", BiText("Mexican Peso", "মেক্সিকান পেসো"), BiText("Mexico", "মেক্সিকো"), 20.3),
        CurrencyInfo("MDL", "L", "🇲🇩", BiText("Moldovan Leu", "মলদোভান লেউ"), BiText("Moldova", "মলদোভা"), 18.1),
        CurrencyInfo("MNT", "₮", "🇲🇳", BiText("Mongolian Tögrög", "মঙ্গোলিয়ান তুগ্রিক"), BiText("Mongolia", "মঙ্গোলিয়া"), 3410.0),
        CurrencyInfo("MAD", "د.م.", "🇲🇦", BiText("Moroccan Dirham", "মরক্কোর দিরহাম"), BiText("Morocco", "মরক্কো"), 9.95),
        CurrencyInfo("MMK", "K", "🇲🇲", BiText("Myanmar Kyat", "মিয়ানমার কিয়াট"), BiText("Myanmar", "মিয়ানমার"), 2100.0),
        CurrencyInfo("NAD", "N$", "🇳🇦", BiText("Namibian Dollar", "নামিবিয়ান ডলার"), BiText("Namibia", "নামিবিয়া"), 18.1),
        CurrencyInfo("NPR", "₨", "🇳🇵", BiText("Nepalese Rupee", "নেপালি রুপি"), BiText("Nepal", "নেপাল"), 135.0),
        CurrencyInfo("NZD", "NZ$", "🇳🇿", BiText("New Zealand Dollar", "নিউজিল্যান্ড ডলার"), BiText("New Zealand", "নিউজিল্যান্ড"), 1.71),
        CurrencyInfo("NGN", "₦", "🇳🇬", BiText("Nigerian Naira", "নাইজেরিয়ান নাইরা"), BiText("Nigeria", "নাইজেরিয়া"), 1660.0),
        CurrencyInfo("MKD", "ден", "🇲🇰", BiText("Macedonian Denar", "ম্যাসেডোনিয়ান দিনার"), BiText("North Macedonia", "উত্তর ম্যাসেডোনিয়া"), 57.8),
        CurrencyInfo("NOK", "kr", "🇳🇴", BiText("Norwegian Krone", "নরওয়েজিয়ান ক্রোন"), BiText("Norway", "নরওয়ে"), 11.05),
        CurrencyInfo("PKR", "₨", "🇵🇰", BiText("Pakistani Rupee", "পাকিস্তানি রুপি"), BiText("Pakistan", "পাকিস্তান"), 277.8),
        CurrencyInfo("PAB", "B/.", "🇵🇦", BiText("Panamanian Balboa", "পানামানিয়ান বালবোয়া"), BiText("Panama", "পানামা"), 1.00),
        CurrencyInfo("PGK", "K", "🇵🇬", BiText("Papua New Guinean Kina", "পাপুয়া নিউগিনি কিনা"), BiText("Papua New Guinea", "পাপুয়া নিউগিনি"), 4.02),
        CurrencyInfo("PYG", "₲", "🇵🇾", BiText("Paraguayan Guaraní", "প্যারাগুয়ান গুয়ারানি"), BiText("Paraguay", "প্যারাগুয়ে"), 7780.0),
        CurrencyInfo("PEN", "S/", "🇵🇪", BiText("Peruvian Sol", "পেরুভিয়ান সোল"), BiText("Peru", "পেরু"), 3.79),
        CurrencyInfo("PHP", "₱", "🇵🇭", BiText("Philippine Peso", "ফিলিপাইন পেসো"), BiText("Philippines", "ফিলিপাইন"), 58.9),
        CurrencyInfo("PLN", "zł", "🇵🇱", BiText("Polish Złoty", "পোলিশ জ্লটি"), BiText("Poland", "পোল্যান্ড"), 4.10),
        CurrencyInfo("GBP", "£", "🇬🇧", BiText("British Pound Sterling", "ব্রিটিশ পাউন্ড"), BiText("United Kingdom", "যুক্তরাজ্য"), 0.79),
        CurrencyInfo("RON", "lei", "🇷🇴", BiText("Romanian Leu", "রোমানিয়ান লেউ"), BiText("Romania", "রোমানিয়া"), 4.68),
        CurrencyInfo("RUB", "₽", "🇷🇺", BiText("Russian Ruble", "রাশিয়ান রুবল"), BiText("Russia", "রাশিয়া"), 100.0),
        CurrencyInfo("RWF", "FRw", "🇷🇼", BiText("Rwandan Franc", "রুয়ান্ডান ফ্রাঙ্ক"), BiText("Rwanda", "রুয়ান্ডা"), 1370.0),
        CurrencyInfo("RSD", "дин.", "🇷🇸", BiText("Serbian Dinar", "সার্বিয়ান দিনার"), BiText("Serbia", "সার্বিয়া"), 110.0),
        CurrencyInfo("SCR", "₨", "🇸🇨", BiText("Seychellois Rupee", "সেশেলস রুপি"), BiText("Seychelles", "সেশেলস"), 14.0),
        CurrencyInfo("ZAR", "R", "🇿🇦", BiText("South African Rand", "দক্ষিণ আফ্রিকান র‌্যান্ড"), BiText("South Africa", "দক্ষিণ আফ্রিকা"), 18.1),
        CurrencyInfo("LKR", "Rs", "🇱🇰", BiText("Sri Lankan Rupee", "শ্রীলঙ্কান রুপি"), BiText("Sri Lanka", "শ্রীলঙ্কা"), 291.5),
        CurrencyInfo("SDG", "ج.س.", "🇸🇩", BiText("Sudanese Pound", "সুদানিজ পাউন্ড"), BiText("Sudan", "সুদান"), 601.0),
        CurrencyInfo("SEK", "kr", "🇸🇪", BiText("Swedish Krona", "সুইডিশ ক্রোনা"), BiText("Sweden", "সুইডেন"), 10.95),
        CurrencyInfo("CHF", "CHF", "🇨🇭", BiText("Swiss Franc", "সুইস ফ্রাঙ্ক"), BiText("Switzerland", "সুইজারল্যান্ড"), 0.885),
        CurrencyInfo("SYP", "£S", "🇸🇾", BiText("Syrian Pound", "সিরিয়ান পাউন্ড"), BiText("Syria", "সিরিয়া"), 13000.0),
        CurrencyInfo("TWD", "NT$", "🇹🇼", BiText("New Taiwan Dollar", "তাইওয়ান ডলার"), BiText("Taiwan", "তাইওয়ান"), 32.5),
        CurrencyInfo("TJS", "ЅМ", "🇹🇯", BiText("Tajikistani Somoni", "তাজিকিস্তানি সোমোনি"), BiText("Tajikistan", "তাজিকিস্তান"), 10.7),
        CurrencyInfo("TZS", "TSh", "🇹🇿", BiText("Tanzanian Shilling", "তানজানিয়ান শিলিং"), BiText("Tanzania", "তানজানিয়া"), 2650.0),
        CurrencyInfo("THB", "฿", "🇹🇭", BiText("Thai Baht", "থাই বাথ"), BiText("Thailand", "থাইল্যান্ড"), 34.6),
        CurrencyInfo("TTD", "TT$", "🇹🇹", BiText("Trinidad & Tobago Dollar", "ত্রিনিদাদ ডলার"), BiText("Trinidad & Tobago", "ত্রিনিদাদ ও টোবাগো"), 6.78),
        CurrencyInfo("TND", "د.ت", "🇹🇳", BiText("Tunisian Dinar", "তিউনিসিয়ান দিনার"), BiText("Tunisia", "তিউনিসিয়া"), 3.15),
        CurrencyInfo("TRY", "₺", "🇹🇷", BiText("Turkish Lira", "তুর্কি লিরা"), BiText("Turkey", "তুরস্ক"), 34.5),
        CurrencyInfo("TMT", "m", "🇹🇲", BiText("Turkmenistan Manat", "তুর্কমেনিস্তান মানাত"), BiText("Turkmenistan", "তুর্কমেনিস্তান"), 3.50),
        CurrencyInfo("UGX", "USh", "🇺🇬", BiText("Ugandan Shilling", "উগান্ডান শিলিং"), BiText("Uganda", "উগান্ডা"), 3680.0),
        CurrencyInfo("UAH", "₴", "🇺🇦", BiText("Ukrainian Hryvnia", "ইউক্রেনিয়ান রিভনিয়া"), BiText("Ukraine", "ইউক্রেন"), 41.3),
        CurrencyInfo("UYU", "\$U", "🇺🇾", BiText("Uruguayan Peso", "উরুগুয়ান পেসো"), BiText("Uruguay", "উরুগুয়ে"), 42.6),
        CurrencyInfo("UZS", "so'm", "🇺🇿", BiText("Uzbekistani Som", "উজবেকিস্তানি সোম"), BiText("Uzbekistan", "উজবেকিস্তান"), 12820.0),
        CurrencyInfo("VES", "Bs.", "🇻🇪", BiText("Venezuelan Bolívar", "ভেনেজুয়েলান বলিভার"), BiText("Venezuela", "ভেনেজুয়েলা"), 45.8),
        CurrencyInfo("VND", "₫", "🇻🇳", BiText("Vietnamese Dong", "ভিয়েতনামী ডং"), BiText("Vietnam", "ভিয়েতনাম"), 25400.0),
        CurrencyInfo("YER", "﷼", "🇾🇪", BiText("Yemeni Rial", "ইয়েমেনি রিয়াল"), BiText("Yemen", "ইয়েমেন"), 250.0),
        CurrencyInfo("ZMW", "ZK", "🇿🇲", BiText("Zambian Kwacha", "জাম্বিয়ান কোয়াচা"), BiText("Zambia", "জাম্বিয়া"), 27.1),
        CurrencyInfo("ZWL", "Z$", "🇿🇼", BiText("Zimbabwean Gold / Dollar", "জিম্বাবুয়ে ডলার"), BiText("Zimbabwe", "জিম্বাবুয়ে"), 25.5),
        CurrencyInfo("XAF", "FCFA", "🇨🇲", BiText("Central African CFA Franc", "সেন্ট্রাল আফ্রিকান CFA ফ্রাঙ্ক"), BiText("CEMAC Region", "মধ্য আফ্রিকা"), 616.0),
        CurrencyInfo("XOF", "CFA", "🇸🇳", BiText("West African CFA Franc", "পশ্চিম আফ্রিকান CFA ফ্রাঙ্ক"), BiText("WAEMU Region", "পশ্চিম আফ্রিকা"), 616.0),
        CurrencyInfo("XCD", "EC$", "🇦🇬", BiText("East Caribbean Dollar", "পূর্ব ক্যারিবিয়ান ডলার"), BiText("Eastern Caribbean", "পূর্ব ক্যারিবিয়ান"), 2.70)
    )

    private val byCodeMap = allCurrencies.associateBy { it.code }

    fun findByCode(code: String): CurrencyInfo =
        byCodeMap[code.uppercase()] ?: CurrencyInfo(
            code = code.uppercase(),
            symbol = code.uppercase(),
            flag = "🌐",
            name = BiText(code.uppercase(), code.uppercase()),
            country = BiText("World", "বিশ্ব"),
            defaultUnitsPerUsd = 1.0
        )

    fun buildDefaultRatesMap(): Map<String, Double> =
        allCurrencies.associate { it.code to it.defaultUnitsPerUsd }

    fun mergeRates(cachedRates: Map<String, Double>): Map<String, Double> {
        val merged = buildDefaultRatesMap().toMutableMap()
        cachedRates.forEach { (k, v) ->
            if (v > 0.0) merged[k.uppercase()] = v
        }
        return merged
    }

    fun getAllSupportedCurrencies(rates: Map<String, Double>): List<CurrencyInfo> {
        val extras = rates.keys
            .filter { !byCodeMap.containsKey(it.uppercase()) }
            .sorted()
            .map { code ->
                CurrencyInfo(
                    code = code.uppercase(),
                    symbol = code.uppercase(),
                    flag = "🌐",
                    name = BiText(code.uppercase(), code.uppercase()),
                    country = BiText("Global", "বিশ্ব"),
                    defaultUnitsPerUsd = rates[code] ?: 1.0
                )
            }
        return allCurrencies + extras
    }
}
