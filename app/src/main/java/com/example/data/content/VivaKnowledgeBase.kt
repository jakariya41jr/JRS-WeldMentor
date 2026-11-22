package com.example.data.content

import com.example.data.model.FlashCard
import com.example.data.model.QuizQuestion
import com.example.i18n.AppLanguage
import com.example.i18n.BiText

data class VivaEvalResult(
    val passed: Boolean,
    val feedback: String,
    val evaluatedByAi: Boolean
)

object VivaKnowledgeBase {

    val quizQuestions: List<QuizQuestion> = listOf(
        QuizQuestion(
            id = "q_what_is_welding",
            topic = BiText("Welding Theory", "ওয়েল্ডিং থিওরি"),
            question = BiText(
                "What is welding and how does arc fusion join two metals?",
                "ওয়েল্ডিং কী এবং কীভাবে দুটি ধাতুকে স্থায়ীভাবে জোড়া লাগানো হয়?"
            ),
            options = listOf(
                BiText(
                    "Permanent joining of metals by melting/fusing them with heat (and optional filler/pressure)",
                    "তাপ ও প্রয়োজনে চাপ/ফিলার দিয়ে ধাতু গলিয়ে (ফিউশন) স্থায়ীভাবে জোড়া দেওয়ার প্রক্রিয়া"
                ),
                BiText("Temporary gluing with epoxy resin", "আঠা দিয়ে অস্থায়ী জোড়া"),
                BiText("Bolting two plates with nuts", "নাট-বোল্ট দিয়ে আটকানো"),
                BiText("Painting carbon steel to stop rust", "মরিচা ঠেকাতে রং করা")
            ),
            correctIndex = 0,
            explanation = BiText(
                "Welding permanently joins metals by heating base edges to melting/fusion temperature, adding filler metal and shielding (flux or gas) against oxidation.",
                "ওয়েল্ডিং হলো বেস মেটাল গলিয়ে ও ফিলার মেটাল মিশিয়ে ফ্লাক্স বা শিল্ডিং গ্যাসের সাহায্যে দুটি ধাতুকে স্থায়ীভাবে একীভূত করার প্রক্রিয়া।"
            )
        ),
        QuizQuestion(
            id = "q_welding_types",
            topic = BiText("Welding Theory", "ওয়েল্ডিং থিওরি"),
            question = BiText(
                "What are the four main industrial arc welding processes used in pipe & structural fabrication?",
                "পাইপ ও স্ট্রাকচারাল ফ্যাব্রিকেশনে ব্যবহৃত চারটি প্রধান আর্ক ওয়েল্ডিং প্রসেস কী কী?"
            ),
            options = listOf(
                BiText(
                    "SMAW (Stick), GTAW (TIG), GMAW (MIG/MAG), and FCAW",
                    "SMAW (আর্ক/স্টিক), GTAW (TIG), GMAW (MIG/MAG) এবং FCAW"
                ),
                BiText("Soldering and Brazing only", "শুধুমাত্র সোল্ডারিং ও ব্রেজিং"),
                BiText("Lathe turning and Milling", "লেদ ও মিলিং"),
                BiText("Sandblasting and Painting", "স্যান্ডব্লাস্টিং ও পেইন্টিং")
            ),
            correctIndex = 0,
            explanation = BiText(
                "The 4 core processes tested for industrial & overseas jobs are SMAW (Shielded Metal Arc), GTAW (TIG), GMAW (MIG/MAG), and FCAW (Flux-Cored Arc).",
                "শিল্প ও বিদেশি চাকরির ট্রেড টেস্টে প্রধান ৪টি প্রসেস হলো SMAW (স্টিক), GTAW (TIG), GMAW (MIG/MAG) এবং FCAW।"
            )
        ),
        QuizQuestion(
            id = "q_tig_polarity_gas",
            topic = BiText("Welding Theory", "ওয়েল্ডিং থিওরি"),
            question = BiText(
                "What polarity and shielding gas are standard for GTAW (TIG) welding of Carbon Steel & Stainless Steel?",
                "কার্বন ও স্টেইনলেস স্টিলে GTAW (TIG) ওয়েল্ডিংয়ে কোন পোলারিটি ও শিল্ডিং গ্যাস ব্যবহার হয়?"
            ),
            options = listOf(
                BiText("DCEN (Straight Polarity, -) with 100% Argon", "DCEN (মাইনাস পোলারিটি) ও ১০০% আর্গন গ্যাস"),
                BiText("AC with 100% Oxygen", "AC ও ১০০% অক্সিজেন"),
                BiText("DCEP with CO2 gas", "DCEP ও CO2 গ্যাস"),
                BiText("No gas needed in TIG", "TIG-তে কোনো গ্যাস লাগে না")
            ),
            correctIndex = 0,
            explanation = BiText(
                "DCEN (-) concentrates ~2/3 heat into the steel workpiece while protecting the tungsten tip, shielded by 100% pure Argon (10–15 L/min).",
                "DCEN (-)-এ কাজের ধাতুতে বেশি তাপ যায় এবং ১০০% আর্গন গ্যাস (১০–১৫ লি/মি) ওয়েল্ড ও টাংস্টেনকে সুরক্ষা দেয়।"
            )
        ),
        QuizQuestion(
            id = "q_e7018_meaning",
            topic = BiText("Electrode Codes", "ইলেকট্রোড কোড"),
            question = BiText(
                "In AWS E7018 electrode, what does '70' represent?",
                "AWS E7018 ইলেকট্রোডে '70' সংখ্যাটি কী বোঝায়?"
            ),
            options = listOf(
                BiText("70,000 PSI minimum tensile strength", "সর্বনিম্ন ৭০,০০০ PSI টেনসাইল স্ট্রেংথ"),
                BiText("70 Amps recommended current", "৭০ অ্যাম্পিয়ার কারেন্ট"),
                BiText("70% iron powder in flux", "ফ্লাক্সে ৭০% আয়রন পাউডার"),
                BiText("70° electrode travel angle", "৭০° ট্রাভেল অ্যাঙ্গেল")
            ),
            correctIndex = 0,
            explanation = BiText(
                "First two digits (70) = 70,000 PSI tensile strength; '1' = all-position; '8' = low-hydrogen potassium iron-powder coating (DC+/AC).",
                "প্রথম দুটি সংখ্যা (70) = ৭০,০০০ PSI টেনসাইল স্ট্রেংথ; '1' = সব পজিশন; '8' = লো-হাইড্রোজেন পটাশিয়াম আয়রন-পাউডার কোটিং।"
            )
        ),
        QuizQuestion(
            id = "q_lb52_code",
            topic = BiText("Electrode Codes", "ইলেকট্রোড কোড"),
            question = BiText(
                "Kobe LB-52 (popular for pipe root/hot pass) corresponds to which AWS classification?",
                "পাইপের রুট ও হট পাসের জন্য পরিচিত Kobe LB-52 কোন AWS ক্লাসিফিকেশনের সমতুল্য?"
            ),
            options = listOf(
                BiText("E6013", "E6013"),
                BiText("E7016", "E7016"),
                BiText("E6010", "E6010"),
                BiText("E309L-16", "E309L-16")
            ),
            correctIndex = 1,
            explanation = BiText(
                "LB-52 is an AWS A5.1 E7016 low-hydrogen potassium electrode prized for smooth root penetration and X-ray quality pipe welds.",
                "LB-52 হলো AWS E7016 লো-হাইড্রোজেন পটাশিয়াম ইলেকট্রোড, যা পাইপের রুট পেনিট্রেশন এবং এক্স-রে মানের ওয়েল্ডের জন্য অত্যন্ত জনপ্রিয়।"
            )
        ),
        QuizQuestion(
            id = "q_e6013_use",
            topic = BiText("Electrode Codes", "ইলেকট্রোড কোড"),
            question = BiText(
                "Which electrode is a rutile all-position rod suited for light sheet metal and general fabrication (not X-ray pipe root)?",
                "পাতলা শিট ও সাধারণ ফ্যাব্রিকেশনের জন্য কোন রুটাইল অল-পজিশন রড ব্যবহার হয় (এক্স-রে পাইপ রুট নয়)?"
            ),
            options = listOf(
                BiText("E7018", "E7018"),
                BiText("E6013", "E6013"),
                BiText("E7016", "E7016"),
                BiText("ER316L", "ER316L")
            ),
            correctIndex = 1,
            explanation = BiText(
                "E6013 has a high-titania (rutile) coating for easy arc striking, smooth bead, and light penetration.",
                "E6013 রুটাইল কোটিংযুক্ত—সহজে আর্ক ধরে, মসৃণ বিড দেয় এবং হালকা পেনিট্রেশনের কাজে ব্যবহৃত হয়।"
            )
        ),
        QuizQuestion(
            id = "q_cs_vs_ss_filler",
            topic = BiText("CS vs SS Fillers", "CS বনাম SS ফিলার"),
            question = BiText(
                "Which TIG filler rod is used for welding 316/316L Stainless Steel pipe?",
                "316/316L স্টেইনলেস স্টিল (SS) পাইপ ওয়েল্ডিংয়ে কোন TIG ফিলার রড ব্যবহার করা হয়?"
            ),
            options = listOf(
                BiText("ER70S-6", "ER70S-6"),
                BiText("ER316L", "ER316L"),
                BiText("E7018-1", "E7018-1"),
                BiText("ER4043", "ER4043")
            ),
            correctIndex = 1,
            explanation = BiText(
                "ER316L contains molybdenum for corrosion resistance in 316L SS; ER70S-6 is for Carbon Steel (CS).",
                "316L স্টেইনলেস স্টিলের ক্ষয়রোধী গুণের জন্য মলিবডেনামযুক্ত ER316L ব্যবহার হয়; আর ER70S-6 কার্বন স্টিলের জন্য।"
            )
        ),
        QuizQuestion(
            id = "q_er70s6_deoxidizer",
            topic = BiText("CS vs SS Fillers", "CS বনাম SS ফিলার"),
            question = BiText(
                "Why is ER70S-6 preferred over ER70S-2 on slightly mill-scaled Carbon Steel?",
                "কার্বন স্টিলে সামান্য মিল-স্কেল থাকলে ER70S-6 কেন বেশি উপযোগী?"
            ),
            options = listOf(
                BiText("Higher silicon & manganese deoxidizers", "বেশি সিলিকন ও ম্যাঙ্গানিজ ডি-অক্সিডাইজার থাকে"),
                BiText("It uses pure oxygen shielding", "এতে অক্সিজেন শিল্ডিং লাগে"),
                BiText("It is an aluminum filler", "এটি অ্যালুমিনিয়াম ফিলার"),
                BiText("It requires AC polarity only", "এটি শুধু AC পোলারিটিতে চলে")
            ),
            correctIndex = 0,
            explanation = BiText(
                "ER70S-6 has higher Mn/Si deoxidizers, wetting out cleanly and preventing porosity on CS.",
                "ER70S-6 এ ম্যাঙ্গানিজ ও সিলিকন বেশি থাকায় অক্সাইড পরিষ্কার করে পোরোসিটি রোধ করে।"
            )
        ),
        QuizQuestion(
            id = "q_6g_position",
            topic = BiText("Positions 1G-6G+", "পজিশন 1G-6G+"),
            question = BiText(
                "In pipe welding qualification, what defines the 6G position?",
                "পাইপ ওয়েল্ডিং কোয়ালিফিকেশনে 6G পজিশনের শর্ত কী?"
            ),
            options = listOf(
                BiText("Pipe rotated horizontally", "পাইপ হরাইজন্টালে ঘোরানো হয়"),
                BiText("Pipe fixed at 45° incline, no rotation allowed", "পাইপ ৪৫° কোণে ফিক্সড, ঘোরানো যাবে না"),
                BiText("Pipe vertical, weld horizontal", "পাইপ খাড়া, ওয়েল্ড হরাইজন্টাল"),
                BiText("Plate flat groove only", "শুধুমাত্র ফ্ল্যাট প্লেট")
            ),
            correctIndex = 1,
            explanation = BiText(
                "In 6G, the pipe axis is fixed at 45° ± 5°. Passing 6G qualifies the welder for all positions (flat, horizontal, vertical, overhead).",
                "6G পজিশনে পাইপ ৪৫° কোণে স্থির থাকে। 6G পাস করলে ফ্ল্যাট, হরাইজন্টাল, ভার্টিক্যাল ও ওভারহেড—সব পজিশনে যোগ্যতা মেলে।"
            )
        ),
        QuizQuestion(
            id = "q_3g_direction",
            topic = BiText("Positions 1G-6G+", "পজিশন 1G-6G+"),
            question = BiText(
                "For structural E7018 3G plate trade tests, which travel direction gives stronger penetration and fewer slag traps?",
                "E7018 দিয়ে 3G প্লেট ট্রেড টেস্টে শক্তিশালী পেনিট্রেশন ও কম স্ল্যাগ ট্র্যাপের জন্য কোন দিকে ওয়েল্ড করতে হয়?"
            ),
            options = listOf(
                BiText("Vertical Up (3G Uphill)", "ভার্টিক্যাল আপ (নিচ থেকে উপরে)"),
                BiText("Vertical Down (Downhill)", "ভার্টিক্যাল ডাউন (উপর থেকে নিচে)"),
                BiText("Sideways weave only", "শুধুমাত্র পাশাপাশি উইভ"),
                BiText("Any random direction", "যেকোনো দিকে")
            ),
            correctIndex = 0,
            explanation = BiText(
                "E7018 is always welded Vertical-Up (uphill) in 3G structural tests so slag drains downward behind the puddle.",
                "E7018 সর্বদা নিচ থেকে উপরে (Vertical Up) ওয়েল্ড করতে হয় যাতে স্ল্যাগ নিচে সরে যায় এবং গলিত মেটালে আটকে না থাকে।"
            )
        ),
        QuizQuestion(
            id = "q_4g_overhead",
            topic = BiText("Positions 1G-6G+", "পজিশন 1G-6G+"),
            question = BiText(
                "In 4G (Overhead) SMAW with E7018 2.5mm/3.2mm, how do you prevent molten metal from dripping?",
                "4G (ওভারহেড) SMAW-তে গলিত ধাতু ঝরে পড়া ঠেকাতে কী করা উচিত?"
            ),
            options = listOf(
                BiText("Use long arc and very high amps", "বড় আর্ক ও অতিরিক্ত অ্যাম্পিয়ার"),
                BiText("Keep a tight short arc, slightly lower amps & fast freeze control", "খুব ছোট টাইট আর্ক, সামান্য কম অ্যাম্পিয়ার ও নিয়ন্ত্রিত স্পিড"),
                BiText("Weld without flux", "ফ্লাক্স ছাড়া ওয়েল্ড"),
                BiText("Hold rod at 20° flat", "রড ২০° ফ্ল্যাটে ধরা")
            ),
            correctIndex = 1,
            explanation = BiText(
                "A tight arc keeps arc force pushing the puddle upward and prevents excessive heat buildup.",
                "ছোট ও টাইট আর্ক মেটালকে উপরে ধরে রাখে এবং অতিরিক্ত তাপ জমা হতে দেয় না।"
            )
        ),
        QuizQuestion(
            id = "q_joint_types",
            topic = BiText("Joint Types", "জয়েন্ট টাইপ"),
            question = BiText(
                "Which joint places two workpieces in the same plane and joins their edges edge-to-edge (common in pipe 6G)?",
                "দুটি প্লেট বা পাইপকে একই সমতলে মুখোমুখি রেখে যে জয়েন্ট করা হয় (যেমন 6G পাইপ), তার নাম কী?"
            ),
            options = listOf(
                BiText("Lap Joint", "ল্যাপ জয়েন্ট"),
                BiText("Butt Joint", "বাট জয়েন্ট"),
                BiText("Corner Joint", "কর্নার জয়েন্ট"),
                BiText("Tee Joint", "টি জয়েন্ট")
            ),
            correctIndex = 1,
            explanation = BiText(
                "A Butt Joint aligns two members in the same plane (often beveled with a root gap and root face for full penetration).",
                "বাট জয়েন্টে দুটি অংশ মুখোমুখি থাকে; পূর্ণ পেনিট্রেশনের জন্য এতে বেভেল, রুট ফেস ও রুট গ্যাপ রাখা হয়।"
            )
        ),
        QuizQuestion(
            id = "q_tig_back_cap",
            topic = BiText("TIG Torch & Cup", "TIG টর্চ ও কাপ"),
            question = BiText(
                "When welding inside a tight pipe rack or 6G restricted area, which TIG back cap should you install?",
                "চাপা জায়গায় বা 6G রেস্ট্রিক্টেড পাইপে TIG ওয়েল্ডিংয়ের সময় কোন ব্যাক ক্যাপ ব্যবহার করা ভালো?"
            ),
            options = listOf(
                BiText("Long Back Cap", "লং ব্যাক ক্যাপ"),
                BiText("Short (Stubby) Back Cap", "শর্ট (স্টাবি) ব্যাক ক্যাপ"),
                BiText("No Back Cap", "ব্যাক ক্যাপ ছাড়া"),
                BiText("Double Ceramic Cup", "ডাবল সিরামিক কাপ")
            ),
            correctIndex = 1,
            explanation = BiText(
                "A Short Back Cap reduces torch head length for tight clearance; Medium/Long caps hold full-length tungsten rods.",
                "শর্ট ব্যাক ক্যাপ টর্চের দৈর্ঘ্য কমিয়ে চাপা জায়গায় সহজে ঘোরাতে সাহায্য করে।"
            )
        ),
        QuizQuestion(
            id = "q_tig_gas_lens",
            topic = BiText("TIG Torch & Cup", "TIG টর্চ ও কাপ"),
            question = BiText(
                "What is the main advantage of replacing a standard collet body with a TIG Gas Lens?",
                "স্ট্যান্ডার্ড কলেট বডির বদলে TIG গ্যাস লেন্স ব্যবহার করলে প্রধান সুবিধা কী?"
            ),
            options = listOf(
                BiText("Produces laminar argon shielding allowing longer tungsten stick-out", "স্থির ল্যামিনার আর্গন প্রবাহ দেয়, ফলে টাংস্টেন বেশি বের করে কাজ করা যায়"),
                BiText("Doubles machine voltage", "মেশিন ভোল্টেজ দ্বিগুণ করে"),
                BiText("Eliminates the need for filler rod", "ফিলার রডের প্রয়োজন শেষ করে"),
                BiText("Replaces the ground clamp", "আর্থ ক্ল্যাম্পের বদলে কাজ করে")
            ),
            correctIndex = 0,
            explanation = BiText(
                "Gas lens screens smooth out turbulence, improving gas coverage in deep pipe bevels and SS welds.",
                "গ্যাস লেন্স আর্গন গ্যাসের ঘূর্ণি কমিয়ে সমান আবরণ দেয়, যা 6G রুট ও SS ওয়েল্ডে খুব কার্যকর।"
            )
        ),
        QuizQuestion(
            id = "q_tig_tungsten_color",
            topic = BiText("TIG Torch & Cup", "TIG টর্চ ও কাপ"),
            question = BiText(
                "Which non-radioactive tungsten color is widely used for DC TIG welding of Carbon Steel & Stainless Steel?",
                "কার্বন ও স্টেইনলেস স্টিলে DC TIG ওয়েল্ডিংয়ের জন্য কোন নন-রেডিওঅ্যাক্টিভ টাংস্টেন কালার ব্যাপকভাবে ব্যবহৃত হয়?"
            ),
            options = listOf(
                BiText("Green (Pure Tungsten)", "সবুজ (পিওর টাংস্টেন)"),
                BiText("Blue (2% Lanthanated - WL20)", "নীল (২% ল্যানথানেটেড - WL20)"),
                BiText("Yellow", "হলুদ"),
                BiText("White", "সাদা")
            ),
            correctIndex = 1,
            explanation = BiText(
                "Blue (2% Lanthanated) or Grey (2% Ceriated) gives crisp DC arc starts without thorium radiation; Red (2% Thoriated) is also common in shops.",
                "নীল (WL20) বা ধূসর (WC20) রেডিয়েশনমুক্ত ও DC-তে দারুণ আর্ক দেয়; এছাড়া লাল (WT20) ওয়ার্কশপে প্রচলিত।"
            )
        ),
        QuizQuestion(
            id = "q_rod_size_amps",
            topic = BiText("Rod Sizes & Amps", "রড সাইজ ও অ্যাম্পস"),
            question = BiText(
                "What is a typical starting amperage range for a 2.5mm E7018 electrode in 3G/6G fill passes?",
                "3G বা 6G ফিল পাসে 2.5mm E7018 ইলেকট্রোডের সাধারণ শুরুর অ্যাম্পিয়ার রেঞ্জ কত?"
            ),
            options = listOf(
                BiText("30 – 45 Amps", "৩০ – ৪৫ অ্যাম্পিয়ার"),
                BiText("65 – 95 Amps", "৬৫ – ৯৫ অ্যাম্পিয়ার"),
                BiText("150 – 190 Amps", "১৫০ – ১৯০ অ্যাম্পিয়ার"),
                BiText("220 – 260 Amps", "২২০ – ২৬০ অ্যাম্পিয়ার")
            ),
            correctIndex = 1,
            explanation = BiText(
                "2.5mm E7018 typically runs around 65–95A (vertical/overhead slightly lower than flat); 3.2mm runs ~95–130A.",
                "2.5mm E7018 সাধারণত ৬৫–৯৫ অ্যাম্পিয়ারে চলে; আর 3.2mm চলে প্রায় ৯৫–১৩০ অ্যাম্পিয়ারে।"
            )
        ),
        QuizQuestion(
            id = "q_low_hy_baking",
            topic = BiText("Rod Sizes & Amps", "রড সাইজ ও অ্যাম্পস"),
            question = BiText(
                "Why must E7018 / LB52 electrodes be stored in a heated rod oven (quiver) before welding?",
                "ওয়েল্ডিংয়ের আগে E7018 / LB52 রড কেন হিটেড ওভেন বা কুইভারে রাখতে হয়?"
            ),
            options = listOf(
                BiText("To prevent moisture pickup that causes hydrogen cracking & porosity", "আর্দ্রতা শোষণ ঠেকাতে, নইলে হাইড্রোজেন ক্র্যাক ও পোরোসিটি হয়"),
                BiText("To melt the flux before striking", "আর্কের আগেই ফ্লাক্স গলানোর জন্য"),
                BiText("To change rod diameter", "রডের সাইজ বদলানোর জন্য"),
                BiText("To make the rod magnetic", "রডকে চুম্বকীয় করতে")
            ),
            correctIndex = 0,
            explanation = BiText(
                "Low-hydrogen flux absorbs atmospheric humidity rapidly; moisture breaks into hydrogen in the arc causing porosity and underbead cracks.",
                "লো-হাইড্রোজেন ফ্লাক্স বাতাস থেকে জলীয়বাষ্প টেনে নেয়, যা ওয়েল্ডে পোরোসিটি ও ক্র্যাক সৃষ্টি করে।"
            )
        ),
        QuizQuestion(
            id = "q_measuring_vernier",
            topic = BiText("Measuring Tools", "পরিমাপক যন্ত্র"),
            question = BiText(
                "Which tool accurately measures pipe wall thickness, root gap, and inside/outside diameters to 0.02mm?",
                "পাইপের ওয়াল থিকনেস, রুট গ্যাপ ও ভেতর/বাহিরের মাপ ০.০২ মিমি সূক্ষ্মতায় মাপতে কোন যন্ত্র ব্যবহার হয়?"
            ),
            options = listOf(
                BiText("Vernier Caliper", "ভার্নিয়ার ক্যালিপার"),
                BiText("Laser Level", "লেজার লেভেল"),
                BiText("Chipping Hammer", "চিপিং হ্যামার"),
                BiText("Angle Locator only", "শুধু অ্যাঙ্গেল লোকেটর")
            ),
            correctIndex = 0,
            explanation = BiText(
                "A Vernier Caliper measures OD, ID, step, and depth precisely; a Micrometer goes even finer (0.01mm) for wire/thickness.",
                "ভার্নিয়ার ক্যালিপার দিয়ে OD, ID ও গভীরতা সূক্ষ্মভাবে মাপা যায়; আর মাইক্রোমিটার ০.০১ মিমি পর্যন্ত মাপে।"
            )
        ),
        QuizQuestion(
            id = "q_measuring_angle",
            topic = BiText("Measuring Tools", "পরিমাপক যন্ত্র"),
            question = BiText(
                "Before starting a 6G trade test coupon, which tool verifies the pipe is set exactly at 45°?",
                "6G ট্রেড টেস্ট শুরুর আগে পাইপটি ঠিক ৪৫° কোণে বসানো হয়েছে কি না তা কোন যন্ত্র দিয়ে যাচাই করবেন?"
            ),
            options = listOf(
                BiText("Angle Locator / Digital Angle Gauge", "অ্যাঙ্গেল লোকেটর / ডিজিটাল অ্যাঙ্গেল গেজ"),
                BiText("Wire Brush", "ওয়্যার ব্রাশ"),
                BiText("Micrometer", "মাইক্রোমিটার"),
                BiText("Electrode Holder", "ইলেকট্রোড হোল্ডার")
            ),
            correctIndex = 0,
            explanation = BiText(
                "An Angle Locator or Digital Angle Gauge confirms exact 45° pipe inclination (plus Laser Level/Spirit Level for alignment).",
                "অ্যাঙ্গেল লোকেটর বা ডিজিটাল অ্যাঙ্গেল গেজ দিয়ে পাইপের ৪৫° কোণ সঠিকভাবে মেপে নেওয়া হয়।"
            )
        ),
        QuizQuestion(
            id = "q_ppe_shade",
            topic = BiText("PPE & HSE Rules", "PPE ও HSE নিয়ম"),
            question = BiText(
                "What welding helmet lens shade range is standard for SMAW/GTAW above 75–200 Amps?",
                "৭৫–২০০ অ্যাম্পিয়ার SMAW/GTAW ওয়েল্ডিংয়ের জন্য হেলমেট গ্লাসের স্ট্যান্ডার্ড শেড কত?"
            ),
            options = listOf(
                BiText("Shade 4 – 5 (Oxy-cutting only)", "শেড ৪ – ৫"),
                BiText("Shade 10 – 12", "শেড ১০ – ১২"),
                BiText("Clear safety glass only", "শুধু পরিষ্কার চশমা"),
                BiText("Shade 1", "শেড ১")
            ),
            correctIndex = 1,
            explanation = BiText(
                "Shade 10–12 protects retinas from intense UV/IR arc radiation ('arc eye'); shade 4–5 is only for gas cutting/grinding.",
                "চোখকে তীব্র UV/IR রশ্মি থেকে বাঁচাতে শেড ১০–১২ ব্যবহার করা বাধ্যতামূলক।"
            )
        ),
        QuizQuestion(
            id = "q_ppe_confined",
            topic = BiText("PPE & HSE Rules", "PPE ও HSE নিয়ম"),
            question = BiText(
                "Why must you NEVER use oxygen instead of compressed air to cool down or ventilate inside a pipe/tank?",
                "ট্যাংক বা পাইপের ভেতর বাতাস দেওয়ার জন্য কখনোই অক্সিজেন গ্যাস ব্যবহার করা উচিত নয় কেন?"
            ),
            options = listOf(
                BiText("Oxygen enriches clothing and sparks cause instant fatal flash fire", "অক্সিজেন পোশাকে মিশে সামান্য স্ফুলিঙ্গেই ভয়াবহ বিস্ফোরক আগুন ধরায়"),
                BiText("It rusts the tungsten", "এতে টাংস্টেনে মরিচা ধরে"),
                BiText("It cools too slowly", "এটি ধীরে ঠান্ডা করে"),
                BiText("It makes the arc blue", "আর্ক নীল করে দেয়")
            ),
            correctIndex = 0,
            explanation = BiText(
                "Oxygen-enriched atmospheres turn leather, cotton, and hair into explosive fuel upon a single welding spark.",
                "অক্সিজেন-সমৃদ্ধ পরিবেশে কাপড় ও চামড়া আগুনের সংস্পর্শে মুহূর্তেই ভয়াবহ অগ্নিকাণ্ড ঘটায়।"
            )
        ),
        QuizQuestion(
            id = "q_ss_purge",
            topic = BiText("CS vs SS Fillers", "CS বনাম SS ফিলার"),
            question = BiText(
                "What happens to the inside root of a 316L Stainless Steel pipe if TIG welding is done without Argon back-purging?",
                "আর্গন ব্যাক-পার্জিং ছাড়া 316L স্টেইনলেস স্টিল পাইপ TIG করলে ভেতরের রুটে কী হয়?"
            ),
            options = listOf(
                BiText("Sugaring (heavy black/granular oxidation) that fails inspection", "সুগারিং (কালো দানাদার অক্সিডেশন) হয় এবং টেস্টে ফেল করে"),
                BiText("Mirror-bright root bead", "আয়নার মতো চকচকে রুট হয়"),
                BiText("Higher corrosion resistance", "মরিচা প্রতিরোধ ক্ষমতা বাড়ে"),
                BiText("Faster travel speed", "স্পিড বেড়ে যায়")
            ),
            correctIndex = 0,
            explanation = BiText(
                "Without Argon purge inside SS pipe, hot chromium reacts with air causing 'sugaring' and brittle, corroded roots.",
                "ভেতরে আর্গন না দিলে গরম ক্রোমিয়াম বাতাসের সাথে বিক্রিয়া করে কালো 'সুগারিং' তৈরি করে যা এক্স-রে বা ভিজ্যুয়াল টেস্টে রিজেক্ট হয়।"
            )
        ),
        QuizQuestion(
            id = "q_undercut_cause",
            topic = BiText("Defect Control", "ত্রুটি নিয়ন্ত্রণ"),
            question = BiText(
                "Which combination most commonly causes Undercut along the weld toe?",
                "ওয়েল্ডের কিনারায় আন্ডারকাট (Undercut) হওয়ার প্রধান কারণ কোনটি?"
            ),
            options = listOf(
                BiText("Excessive amperage, too fast travel, or long arc length", "অতিরিক্ত অ্যাম্পিয়ার, খুব দ্রুত টানা বা লম্বা আর্ক"),
                BiText("Too low amperage and short arc", "খুব কম অ্যাম্পিয়ার ও ছোট আর্ক"),
                BiText("Clean bevel preparation", "পরিষ্কার বেভেল"),
                BiText("Using a Vernier caliper", "ভার্নিয়ার ক্যালিপার ব্যবহার")
            ),
            correctIndex = 0,
            explanation = BiText(
                "Reduce amps slightly, pause briefly at the weave toes to fill, keep a tight arc, and maintain proper electrode angle.",
                "অ্যাম্পিয়ার সামান্য কমিয়ে, দুই কিনারায় ক্ষণিক থেমে মেটাল ভরে দিয়ে ও ছোট আর্ক রেখে আন্ডারকাট দূর করা যায়।"
            )
        )
    )

    val flashCards: List<FlashCard> = listOf(
        FlashCard(
            id = "fc_e6013",
            category = BiText("SMAW Carbon Steel", "SMAW কার্বন স্টিল"),
            codeTitle = "AWS E6013",
            frontSubtitle = BiText("Rutile General-Purpose Electrode", "রুটাইল সাধারণ কাজের ইলেকট্রোড"),
            backSpecs = BiText(
                "60,000 PSI • All-Position • Rutile (Titania) flux • AC or DC± • Typical 3.2mm: 85–125A",
                "৬০,০০০ PSI • সব পজিশন • রুটাইল ফ্লাক্স • AC বা DC± • ৩.২ মিমি: ৮৫–১২৫A"
            ),
            backTip = BiText(
                "Easy strike & smooth appearance on thin plates/tack welds, NOT used for critical pressure pipe roots.",
                "পাতলা প্লেট ও ট্যাক ওয়েল্ডে সহজে আর্ক ধরে, তবে হাই-প্রেশার পাইপ রুটে নয়।"
            )
        ),
        FlashCard(
            id = "fc_e7018",
            category = BiText("SMAW Low-Hydrogen", "SMAW লো-হাইড্রোজেন"),
            codeTitle = "AWS E7018",
            frontSubtitle = BiText("Structural & Pipe Fill/Cap King", "স্ট্রাকচারাল ও পাইপ ফিল/ক্যাপ রড"),
            backSpecs = BiText(
                "70,000 PSI • All-Position • Low-H2 Iron Powder • DCEP (+) • 2.5mm: 65–95A | 3.2mm: 95–130A",
                "৭০,০০০ PSI • সব পজিশন • লো-হাইড্রোজেন • DCEP (+) • ২.৫মিমি: ৬৫–৯৫A | ৩.২মিমি: ৯৫–১৩০A"
            ),
            backTip = BiText(
                "Keep in 120–150°C rod oven! Maintain tight short arc; never whip or drag too fast.",
                "রড ওভেনে গরম রাখুন! আর্ক খুব ছোট রাখুন; অতিরিক্ত লম্বা করলে পোরোসিটি হবে।"
            )
        ),
        FlashCard(
            id = "fc_lb52",
            category = BiText("Pipe Root / Hot Pass", "পাইপ রুট / হট পাস"),
            codeTitle = "LB-52 (E7016)",
            frontSubtitle = BiText("Kobe Steel Pipe Root Electrode", "পাইপ রুটের জনপ্রিয় ইলেকট্রোড"),
            backSpecs = BiText(
                "70,000 PSI • All-Position • Low-H2 Potassium • DCEP (+) or AC • 2.5mm: 60–90A | 3.2mm: 90–125A",
                "৭০,০০০ PSI • সব পজিশন • লো-হাইড্রোজেন পটাশিয়াম • ২.৫মিমি: ৬০–৯০A | ৩.২মিমি: ৯০–১২৫A"
            ),
            backTip = BiText(
                "Listen for steady 'keyhole' sound on root pass; clean slag thoroughly before hot pass.",
                "রুট পাসে 'কি-হোল' শব্দ ও আকার নজরে রাখুন; হট পাসের আগে স্ল্যাগ ভালোভাবে পরিষ্কার করুন।"
            )
        ),
        FlashCard(
            id = "fc_er70s6",
            category = BiText("GTAW / MIG Carbon Steel", "TIG / MIG কার্বন স্টিল"),
            codeTitle = "ER70S-6",
            frontSubtitle = BiText("Copper-Coated CS Filler Wire/Rod", "কার্বন স্টিল ফিলার রড / তার"),
            backSpecs = BiText(
                "70,000 PSI • High Si/Mn deoxidizers • TIG DCEN (-) with 100% Argon (10–15 L/min) • 2.4mm root/fill",
                "৭০,০০০ PSI • বেশি Si/Mn ডি-অক্সিডাইজার • TIG DCEN (-) ১০০% আর্গন • ২.৪ মিমি রুট/ফিল"
            ),
            backTip = BiText(
                "Wipe rod clean before TIG root; feed steadily into the leading edge of the keyhole.",
                "TIG রুটের আগে রড মুছে নিন; কি-হোলের সামনের কিনারায় সমান তালে ফিলার দিন।"
            )
        ),
        FlashCard(
            id = "fc_er316l",
            category = BiText("GTAW Stainless Steel", "TIG স্টেইনলেস স্টিল"),
            codeTitle = "ER316L / 308L",
            frontSubtitle = BiText("Austenitic Stainless Filler", "স্টেইনলেস স্টিল ফিলার রড"),
            backSpecs = BiText(
                "'L' = Low Carbon (<0.03%) prevents carbide precipitation • DCEN (-) • Argon + Back Purge on Pipe",
                "'L' = লো-কার্বন (<০.০৩%) ক্ষয় রোধ করে • DCEN (-) • পাইপে অবশ্যই আর্গন ব্যাক-পার্জ"
            ),
            backTip = BiText(
                "Keep hot rod tip inside Argon shield envelope so tip never oxidizes black!",
                "রডের মাথা গরম অবস্থায় আর্গন গ্যাসের ভেতরেই রাখুন যেন কালো না হয়ে যায়!"
            )
        ),
        FlashCard(
            id = "fc_positions",
            category = BiText("ASME / AWS Positions", "ওয়েল্ডিং পজিশন"),
            codeTitle = "1G → 6G+",
            frontSubtitle = BiText("G = Groove (Butt) | F = Fillet", "G = গ্রুভ (বাট) | F = ফিলেট"),
            backSpecs = BiText(
                "1G Flat • 2G Horizontal • 3G Vertical • 4G Overhead • 5G Pipe Fixed Horiz. • 6G Pipe 45° Fixed",
                "1G ফ্ল্যাট • 2G হরাইজন্টাল • 3G ভার্টিক্যাল • 4G ওভারহেড • 5G পাইপ ফিক্সড • 6G পাইপ ৪৫°"
            ),
            backTip = BiText(
                "6G+ (with restriction ring/wall) tests mirror/blind access around 6-inch & 12-inch pipe.",
                "6G+ এ রেস্ট্রিকশনসহ ৬\" ও ১২\" পাইপে সীমিত জায়গায় ওয়েল্ডিংয়ের দক্ষতা পরীক্ষা হয়।"
            )
        ),
        FlashCard(
            id = "fc_tig_torch",
            category = BiText("TIG Torch Anatomy", "TIG টর্চ যন্ত্রাংশ"),
            codeTitle = "Cup • Collet • Back Cap",
            frontSubtitle = BiText("WP-17 / WP-26 Consumable Stack", "TIG টর্চ কনজিউমেবল অংশ"),
            backSpecs = BiText(
                "Ceramic Cup (#5–#8) • Collet & Collet Body (or Gas Lens) • Back Cap (Long/Med/Short) • Tungsten",
                "সিরামিক কাপ (#৫–#৮) • কলেট ও বডি (বা গ্যাস লেন্স) • ব্যাক ক্যাপ (লং/মিডিয়াম/শর্ট) • টাংস্টেন"
            ),
            backTip = BiText(
                "Always match Collet size (e.g., 2.4mm) to Tungsten diameter & tighten back cap snugly.",
                "টাংস্টেনের সাইজ অনুযায়ী সঠিক কলেট (যেমন ২.৪ মিমি) ব্যবহার করুন।"
            )
        ),
        FlashCard(
            id = "fc_measuring_tools",
            category = BiText("Inspection & Fit-Up", "পরিমাপ ও ফিট-আপ"),
            codeTitle = "Gauges & Calipers",
            frontSubtitle = BiText("Essential Welder Metrology", "ওয়েল্ডারের প্রয়োজনীয় মাপার যন্ত্র"),
            backSpecs = BiText(
                "Angle Locator / Digital Angle Gauge (45° 6G) • Vernier Caliper • Micrometer • Laser Level • Bridge Cam / Fillet Gauge",
                "অ্যাঙ্গেল লোকেটর / ডিজিটাল গেজ • ভার্নিয়ার ক্যালিপার • মাইক্রোমিটার • লেজার লেভেল • ফিলেট গেজ"
            ),
            backTip = BiText(
                "Always measure root gap (2.5–3.2mm), root face (1–1.5mm), and bevel angle (30–37.5°) before tacking.",
                "ট্যাক দেওয়ার আগে রুট গ্যাপ, রুট ফেস এবং বেভেল অ্যাঙ্গেল মেপে নিশ্চিত হোন।"
            )
        )
    )

    private val stopWords = setOf(
        "the", "and", "for", "with", "that", "this", "from", "into", "only", "are", "used",
        "use", "not", "all", "very", "per", "your", "when", "what", "which", "how", "can",
        "ও", "এবং", "জন্য", "দিয়ে", "হয়", "করা", "থাকে", "এর", "একটি", "কোন", "কী"
    )

    fun evaluateOfflineDelegateAnswer(
        question: QuizQuestion,
        userAnswer: String,
        language: AppLanguage
    ): VivaEvalResult {
        val cleanInput = userAnswer.trim()
        if (cleanInput.length < 3) {
            val msg = if (language == AppLanguage.BN) {
                "দয়া করে আপনার উত্তর একটু বিস্তারিত বলুন বা লিখুন। সঠিক উত্তর: ${question.options[question.correctIndex].bn}"
            } else {
                "Please speak or type a fuller technical answer. Reference: ${question.options[question.correctIndex].en}"
            }
            return VivaEvalResult(passed = false, feedback = msg, evaluatedByAi = false)
        }

        val normalizedInput = cleanInput.lowercase()
        val correctOpt = question.options[question.correctIndex]
        val referenceTokens = (correctOpt.en + " " + correctOpt.bn + " " + question.explanation.en)
            .lowercase()
            .split(Regex("[\\s,/()•\\-]+"))
            .map { it.trim('.', ':', ';', '\'', '"', '%', '°') }
            .filter { it.length >= 2 && it !in stopWords }
            .distinct()

        val matchedCount = referenceTokens.count { token -> normalizedInput.contains(token) }

        val strongCodeTokens = listOf(
            "70,000", "70000", "tensile", "টেনসাইল",
            "e7016", "7016",
            "e6013", "6013",
            "er316l", "316l",
            "er70s-6", "er70s6", "silicon", "manganese", "সিলিকন",
            "45", "৪৫", "all position", "সব পজিশন",
            "uphill", "vertical up", "নিচ থেকে উপরে",
            "short arc", "ছোট", "টাইট",
            "butt", "বাট",
            "short", "stubby", "শর্ট", "স্টাবি",
            "gas lens", "laminar", "ল্যামিনার",
            "blue", "wl20", "lanthanated", "নীল",
            "65", "95", "৬৫", "৯৫",
            "moisture", "hydrogen", "আর্দ্রতা", "হাইড্রোজেন",
            "vernier", "caliper", "ভার্নিয়ার", "ক্যালিপার",
            "angle locator", "angle gauge", "অ্যাঙ্গেল",
            "10", "12", "১০", "১২", "shade", "শেড",
            "fire", "flash", "explosion", "আগুন", "বিস্ফোর",
            "sugar", "oxidation", "সুগারিং", "অক্সিডেশন",
            "high amp", "long arc", "fast", "বেশি অ্যাম্পিয়ার", "লম্বা আর্ক",
            "fusion", "melting", "গলিয়ে", "ফিউশন", "স্থায়ী",
            "smaw", "gtaw", "tig", "fcaw", "mig", "gmaw",
            "dcen", "argon", "আর্গন"
        )
        val hasStrongMatch = strongCodeTokens.any { code ->
            normalizedInput.contains(code) &&
                (correctOpt.en.lowercase().contains(code) ||
                    correctOpt.bn.lowercase().contains(code) ||
                    question.explanation.en.lowercase().contains(code) ||
                    question.explanation.bn.lowercase().contains(code))
        }

        val matchRatio = if (referenceTokens.isEmpty()) 0f else matchedCount.toFloat() / referenceTokens.size.toFloat()
        val passed = hasStrongMatch || matchedCount >= 2 || matchRatio >= 0.22f

        val feedback = if (passed) {
            if (language == AppLanguage.BN) {
                "সঠিক হয়েছে! আপনার উত্তর মূল পয়েন্টের সাথে মিলেছে। ${question.explanation.bn}"
            } else {
                "Good answer — passed! ${question.explanation.en}"
            }
        } else {
            if (language == AppLanguage.BN) {
                "আংশিক বা ভিন্ন উত্তর হয়েছে। কাঙ্ক্ষিত উত্তর: ${correctOpt.bn}। ব্যাখ্যা: ${question.explanation.bn}"
            } else {
                "Needs review. Expected answer: ${correctOpt.en}. Key note: ${question.explanation.en}"
            }
        }

        return VivaEvalResult(passed = passed, feedback = feedback, evaluatedByAi = false)
    }
}
