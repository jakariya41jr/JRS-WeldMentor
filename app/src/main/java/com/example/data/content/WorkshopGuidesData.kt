package com.example.data.content

import com.example.data.model.ChecklistTemplate
import com.example.data.model.DefectGuide
import com.example.data.model.HandsOnTip
import com.example.data.model.ParamRefRow
import com.example.i18n.BiText

object WorkshopGuidesData {

    val handsOnTips: List<HandsOnTip> = listOf(
        HandsOnTip(
            id = "tip_tremor",
            title = BiText("Stopping Hand Tremors & Shaking", "হাত কাঁপা বন্ধ ও স্থির করার কৌশল"),
            category = BiText("Body Control & Ergonomics", "শরীর ও গ্রিপ নিয়ন্ত্রণ"),
            summary = BiText(
                "Smooth welds start with locked bone support—never hover your arm in mid-air.",
                "স্থির ওয়েল্ডিংয়ের মূল রহস্য হলো হাতকে কোনো অবলম্বনে সাপোর্ট দেওয়া—কখনোই হাত শূন্যে ভাসিয়ে রাখবেন না।"
            ),
            steps = listOf(
                BiText(
                    "1. Tripod Stance & Anchor: Plant feet shoulder-width apart. Rest your elbow, forearm, or pinky edge of your torch hand on a cool jig, pipe stand, or free-hand palm.",
                    "১. শক্ত অবস্থান ও সাপোর্ট: পা কাঁধ বরাবর রাখুন। কনুই, হাতের কব্জি বা কনিষ্ঠা আঙুল ঠান্ডা সাপোর্ট বা অন্য হাতের তালুর ওপর রাখুন।"
                ),
                BiText(
                    "2. Relaxed Pencil/Hammer Grip: Squeezing the TIG torch or stinger tightly causes muscle fatigue and shakes. Hold firmly like a pen—let cable weight hang over your shoulder or wrist.",
                    "২. হালকা ও আরামদায়ক গ্রিপ: টর্চ বা হোল্ডার খুব শক্ত করে চেপে ধরলে হাত কাঁপে। কলমের মতো স্বাভাবিক গ্রিপ নিন এবং ক্যাবলের ওজন কাঁধে বা টেবিলে রাখুন।"
                ),
                BiText(
                    "3. Controlled Breathing: Exhale slowly as you strike and travel; avoid holding your breath during long 6G passes.",
                    "৩. শ্বাস-প্রশ্বাস নিয়ন্ত্রণ: আর্ক ধরার সময় ধীরে শ্বাস ছাড়ুন; শ্বাস আটকে রাখলে হাতের কাঁপুনি বাড়ে।"
                ),
                BiText(
                    "4. Dry-Run First: Always rehearse the full hand path with hood down before striking the arc so your glove never snags mid-bead.",
                    "৪. ড্রাই-রান প্র্যাকটিস: আর্ক ধরার আগে হেলমেট ছাড়াই হাত ঘুরিয়ে পুরো পথ অনুশীলন করে নিন।"
                )
            )
        ),
        HandsOnTip(
            id = "tip_straight_bead",
            title = BiText("Pulling Straight Stringers & Layers", "সোজা স্ট্রিংগার ও সমান লেয়ার টানার নিয়ম"),
            category = BiText("Bead Geometry", "বিড নিয়ন্ত্রণ"),
            summary = BiText(
                "Use bevel edges, soapstone lines, and the previous bead toe as your visual guide rail.",
                "বেভেলের কিনারা, চক/সোপস্টোন দাগ এবং আগের বিডের প্রান্তকে গাইড লাইন হিসেবে অনুসরণ করুন।"
            ),
            steps = listOf(
                BiText(
                    "1. Watch the Leading Edge, Not the Arc Core: Focus just ahead of the bright puddle where molten metal meets the base line.",
                    "১. আর্কের ভেতরে নয়, পাডলের সামনের কিনারায় তাকান: গলিত ধাতু যেখানে বেস মেটালের সাথে মিশছে সেখানে ফোকাস রাখুন।"
                ),
                BiText(
                    "2. Overlap 30%–50%: For multi-pass caps (e.g., 3-bead 6G cap), land the next stringer's edge cleanly on the crown of the previous bead.",
                    "২. ৩০%–৫০% ওভারল্যাপ: মাল্টি-পাস ক্যাপে পরবর্তী বিডের কিনারা আগের বিডের চূড়ার ওপর ফেলুন।"
                ),
                BiText(
                    "3. Consistent Travel Speed: Count rhythmically ('1-and-2-and...') or step-pause evenly for TIG/SMAW weave to keep uniform bead width.",
                    "৩. সমান গতি: মনে মনে ছন্দ মিলিয়ে সমান তালে এগোন, এতে বিডের প্রস্থ ও উচ্চতা সব জায়গায় এক থাকে।"
                )
            )
        ),
        HandsOnTip(
            id = "tip_puddle_angle",
            title = BiText("Weld Puddle & Torch/Rod Angles", "ওয়েল্ড পাডল ও টর্চ/রড অ্যাঙ্গেল"),
            category = BiText("Arc & Penetration", "আর্ক ও পেনিট্রেশন"),
            summary = BiText(
                "Work angle controls symmetry between both walls; travel angle pushes slag and shapes penetration.",
                "ওয়ার্ক অ্যাঙ্গেল দুই পাশে সমান ফিউশন দেয় এবং ট্রাভেল অ্যাঙ্গেল স্ল্যাগ ও পেনিট্রেশন নিয়ন্ত্রণ করে।"
            ),
            steps = listOf(
                BiText(
                    "1. SMAW Drag Angle: Tilt 10°–15° back toward the completed bead so arc force keeps slag behind the puddle.",
                    "১. SMAW ড্র্যাগ অ্যাঙ্গেল: সম্পন্ন বিডের দিকে ১০°–১৫° হেলিয়ে টানুন যাতে স্ল্যাগ সামনে না আসে।"
                ),
                BiText(
                    "2. GTAW Push Angle: Lean TIG torch 10°–15° forward in travel direction; feed filler rod at ~15°–20° to plate/pipe tangent.",
                    "২. GTAW পুশ অ্যাঙ্গেল: TIG টর্চ চলার দিকে ১০°–১৫° হেলিয়ে রাখুন এবং ফিলার রড ১৫°–২০° কোণে দিন।"
                ),
                BiText(
                    "3. Watch Puddle Wet-Out: If the puddle turns oval and sluggish, drop amps 5–10A or speed up; if edges look sharp/notched, slow down and let toes wet out.",
                    "৩. পাডল পর্যবেক্ষণ: পাডল অতিরিক্ত তরল হয়ে গেলে ৫–১০A কমান; কিনারায় খাঁজ থাকলে গতি কিছুটা কমিয়ে পাডলকে বসতে দিন।"
                )
            )
        )
    )

    val defectGuides: List<DefectGuide> = listOf(
        DefectGuide(
            id = "def_undercut",
            title = BiText("Undercut", "আন্ডারকাট (Undercut)"),
            visualCue = BiText(
                "Groove or melted-out notch along the weld toe where base metal wasn't filled back.",
                "ওয়েল্ডের কিনারায় বেস মেটাল গলে খাঁজ তৈরি হওয়া কিন্তু মেটাল দিয়ে ভরাট না হওয়া।"
            ),
            causes = BiText(
                "Amperage too high, travel speed too fast, arc length too long, or wrong work angle on Tee/2G/3G.",
                "অ্যাম্পিয়ার বেশি হওয়া, খুব দ্রুত টানা, লম্বা আর্ক অথবা ভুল ওয়ার্ক অ্যাঙ্গেল।"
            ),
            fixes = BiText(
                "Lower amps 5–15A, hold a tight short arc, pause 0.5s at each weave edge to let filler wet out.",
                "৫–১৫A কমান, ছোট আর্ক রাখুন এবং উইভের দুই পাশে ক্ষণিক থেমে কিনারা ভরে দিন।"
            )
        ),
        DefectGuide(
            id = "def_porosity",
            title = BiText("Porosity (Gas Pockets / Pinholes)", "পোরোসিটি (ছিদ্র / গ্যাস পকেট)"),
            visualCue = BiText(
                "Small round pinholes or wormholes on surface or inside weld bead.",
                "ওয়েল্ডের গায়ে বা ভেতরে ছোট ছোট গোল ছিদ্র বা পিনহোল।"
            ),
            causes = BiText(
                "Damp E7018/LB52 rods, oil/rust/paint on bevel, wind blowing away Argon/CO2, clogged TIG cup, or long arc.",
                "ভেজা রড, বেভেলে তেল/মরিচা/রং, বাতাসে আর্গন সরে যাওয়া, ময়লা কাপ বা লম্বা আর্ক।"
            ),
            fixes = BiText(
                "Bake/store rods dry, grind base metal bright 25mm each side, check 10–15 L/min gas & block drafts.",
                "ওভেনে শুকনো রড ব্যবহার করুন, জয়েন্টের ২৫ মিমি পরিষ্কার ঘষুন, গ্যাস ফ্লো চেক ও বাতাস আড়াল করুন।"
            )
        ),
        DefectGuide(
            id = "def_slag",
            title = BiText("Slag Inclusion", "স্ল্যাগ ইনক্লুশন (Slag Trap)"),
            visualCue = BiText(
                "Non-metallic dark slag trapped between passes or along undercut pockets.",
                "দুই পাসের মাঝে বা খাঁজের ভেতর কালো ফ্লাক্স/স্ল্যাগ আটকে থাকা।"
            ),
            causes = BiText(
                "Incomplete wire-brushing/grinding between passes, tight bevel edges, amps too low, or weaving too wide.",
                "পাসের মাঝে স্ল্যাগ ভালোভাবে পরিষ্কার না করা, কম অ্যাম্পিয়ার বা অতিরিক্ত চওড়া উইভ।"
            ),
            fixes = BiText(
                "Feather-grind starts/stops & clean every pass with chipping hammer + power wire wheel; keep amps adequate.",
                "প্রতি পাসের পর চিপিং হ্যামার ও ওয়্যার ব্রাশ/গ্রাইন্ডার দিয়ে নিখুঁত পরিষ্কার করুন।"
            )
        ),
        DefectGuide(
            id = "def_spatter",
            title = BiText("Excessive Spatter", "অতিরিক্ত স্প্যাটার (ছিটা)"),
            visualCue = BiText(
                "Small solidified metal droplets stuck around the weld bead.",
                "ওয়েল্ড বিডের আশপাশে ছোট ছোট ধাতব দানা ছড়িয়ে থাকা।"
            ),
            causes = BiText(
                "Arc length too long, amps too high, damp electrodes, arc blow (magnetism), or wrong MIG wire feed/voltage balance.",
                "লম্বা আর্ক, বেশি অ্যাম্পিয়ার, ভেজা রড, আর্ক ব্লো বা MIG ভোল্টেজ-ওয়্যার স্পিড অমিল।"
            ),
            fixes = BiText(
                "Shorten arc length, move ground clamp closer/opposite arc blow, use dry rods, dial down voltage/amps slightly.",
                "আর্ক ছোট রাখুন, আর্থ ক্ল্যাম্প কাছাকাছি/বিপরীত দিকে লাগান ও শুকনো রড ব্যবহার করুন।"
            )
        ),
        DefectGuide(
            id = "def_distortion",
            title = BiText("Distortion / Warping", "ডিস্টর্শন / বেঁকে যাওয়া"),
            visualCue = BiText(
                "Plates/pipes pulling out of alignment or closing the root gap unevenly.",
                "ওয়েল্ডিংয়ের তাপে প্লেট বা পাইপ বেঁকে যাওয়া বা রুট গ্যাপ চেপে আসা।"
            ),
            causes = BiText(
                "Weak or uneven tack welds, excessive heat input, too many slow passes, no clamping/pre-set.",
                "দুর্বল ট্যাক ওয়েল্ড, অতিরিক্ত তাপ, বা ক্ল্যাম্প ছাড়া ওয়েল্ডিং।"
            ),
            fixes = BiText(
                "Use strong 4-quadrant tacks (12, 3, 6, 9 o'clock), alternate weld sequence, keep interpass temp controlled.",
                "পাইপের ৪ দিকে (১২, ৩, ৬, ৯টা) শক্ত ট্যাক দিন ও পালাক্রমে ওয়েল্ড করে তাপ নিয়ন্ত্রণ করুন।"
            )
        ),
        DefectGuide(
            id = "def_lof",
            title = BiText("Lack of Fusion / Cold Lap", "ফিউশনের অভাব (Lack of Fusion)"),
            visualCue = BiText(
                "Weld metal sits on top of base metal without melting smoothly into bevel wall.",
                "ওয়েল্ড ধাতু বেস মেটালের সাথে না গলে শুধু ওপরে লেগে থাকা।"
            ),
            causes = BiText(
                "Amperage too low, travel too fast, puddle running ahead of arc, dirty oxide layer.",
                "অ্যাম্পিয়ার খুব কম, খুব দ্রুত টানা, বা অক্সাইড/মরিচা পরিষ্কার না করা।"
            ),
            fixes = BiText(
                "Increase amps 10–15A, point arc into the bevel corner/root wall, watch base metal visibly melt before feeding.",
                "১০–১৫A বাড়ান, বেভেলের দেয়ালে আর্ক ধরে আগে বেস মেটাল গলতে দেখুন।"
            )
        ),
        DefectGuide(
            id = "def_burn",
            title = BiText("Burn-Through", "বার্ন-থ্রু (ছিদ্র হয়ে গলে যাওয়া)"),
            visualCue = BiText(
                "Large blow-out hole on root pass or thin plate.",
                "রুট পাসে বা পাতলা প্লেটে বড় গর্ত হয়ে যাওয়া।"
            ),
            causes = BiText(
                "Root gap too wide, root face (land) too thin, amps too high, slow travel at 6 o'clock.",
                "রুট গ্যাপ বেশি, রুট ফেস খুব পাতলা, বেশি অ্যাম্পিয়ার বা ৬টার পজিশনে ধীরগতি।"
            ),
            fixes = BiText(
                "Drop amps 10–15A, whip slightly or add extra filler wire to cool puddle, check 1.2–1.5mm root face.",
                "১০–১৫A কমান, রুট পাসে পর্যাপ্ত ফিলার দিয়ে পাডল ঠান্ডা রাখুন ও রুট ফেস সঠিক রাখুন।"
            )
        )
    )

    val paramReferenceRows: List<ParamRefRow> = listOf(
        ParamRefRow("SMAW", "E6013", "2.5 mm", "1G–4G", "60–90 A", "Flux Shield", "AC / DC±"),
        ParamRefRow("SMAW", "E6013", "3.2 mm", "1G–4G", "85–125 A", "Flux Shield", "AC / DC±"),
        ParamRefRow("SMAW", "LB-52 (E7016)", "2.5 mm", "Root / 5G / 6G", "60–90 A", "Flux (Oven)", "DCEP (+) / AC"),
        ParamRefRow("SMAW", "LB-52 (E7016)", "3.2 mm", "Fill / 3G / 6G", "90–125 A", "Flux (Oven)", "DCEP (+) / AC"),
        ParamRefRow("SMAW", "E7018", "2.5 mm", "3G / 4G / 6G", "65–95 A", "Flux (Oven)", "DCEP (+)"),
        ParamRefRow("SMAW", "E7018", "3.2 mm", "1G–6G Fill/Cap", "95–130 A", "Flux (Oven)", "DCEP (+)"),
        ParamRefRow("GTAW/TIG", "ER70S-6 (CS)", "2.4 mm", "6G Root/Hot", "85–125 A", "Argon 10–14 L/m", "DCEN (-)"),
        ParamRefRow("GTAW/TIG", "ER316L (SS)", "1.6 / 2.4 mm", "6G Root + Purge", "65–105 A", "Argon 10–14 + Purge", "DCEN (-)"),
        ParamRefRow("FCAW", "E71T-1", "1.2 mm", "2G / 3G / 4G", "150–210A / 22–26V", "CO2 15–20 L/m", "DCEP (+)"),
        ParamRefRow("MIG/MAG", "ER70S-6 Wire", "0.8 / 1.0 mm", "1G–3G", "110–185A / 18–23V", "75Ar/25CO2 14–18 L/m", "DCEP (+)")
    )

    val stanceChecklist: List<ChecklistTemplate> = listOf(
        ChecklistTemplate(
            id = "stance_1",
            group = "STANCE",
            title = BiText("Stable Tripod Stance & Foot Placement", "স্থির পায়ের অবস্থান ও ব্যালেন্স"),
            detail = BiText(
                "Feet shoulder-width apart, knees slightly bent; ensure you can pivot hips across the full 6G pipe quadrant without shifting feet mid-bead.",
                "দুই পা কাঁধ বরাবর রেখে কোমর ঘোরানোর জায়গা রাখুন যেন ওয়েল্ডিংয়ের মাঝপথে পা সরাতে না হয়।"
            )
        ),
        ChecklistTemplate(
            id = "stance_2",
            group = "STANCE",
            title = BiText("Forearm / Wrist Anchor & Cable Relief", "কনুই/কব্জির সাপোর্ট ও ক্যাবল ফ্রি রাখা"),
            detail = BiText(
                "Rest forearm or non-dominant palm on a rigid support; loop TIG/SMAW lead over shoulder or clamp so cable weight never pulls your hand.",
                "হাত বা কব্জিকে শক্ত অবলম্বনে রাখুন এবং ক্যাবল কাঁধে বা হুকে ঝুলিয়ে ওজন কমান।"
            )
        ),
        ChecklistTemplate(
            id = "stance_3",
            group = "STANCE",
            title = BiText("Work Angle & Travel Angle Dry-Run", "ওয়ার্ক অ্যাঙ্গেল ও ট্রাভেল অ্যাঙ্গেল ড্রাই-রান"),
            detail = BiText(
                "Simulate the arc path from 6 o'clock to 12 o'clock before striking; check 90° radial work angle and 10°–15° travel tilt.",
                "আর্ক ধরার আগে ৬টা থেকে ১২টা পজিশন পর্যন্ত হাত ঘুরিয়ে অ্যাঙ্গেল পরীক্ষা করে নিন।"
            )
        ),
        ChecklistTemplate(
            id = "stance_4",
            group = "STANCE",
            title = BiText("4G Overhead & 6G+ Mirror / Vision Check", "4G ওভারহেড ও 6G+ মিরর/ভিশন সেটআপ"),
            detail = BiText(
                "Position head slightly to the side of falling sparks (4G) or verify inspection mirror angle & helmet auto-darkening sensitivity for restricted 6G+.",
                "4G-তে স্পার্ক থেকে মাথা সামান্য পাশে রাখুন; 6G+ রেস্ট্রিকশনে মিরর কোণ ও হেলমেটের সেন্সর চেক করুন।"
            )
        ),
        ChecklistTemplate(
            id = "stance_5",
            group = "STANCE",
            title = BiText("Clear Hood Lens & Breathing Rhythm", "পরিষ্কার হেলমেট গ্লাস ও শ্বাস নিয়ন্ত্রণ"),
            detail = BiText(
                "Wipe inner/outer cover lens clean for crisp puddle visibility and take a calm breath before striking.",
                "হেলমেটের গ্লাস মুছে পরিষ্কার করুন এবং শান্তভাবে শ্বাস নিয়ে আর্ক ধরুন।"
            )
        )
    )

    val tradeTestChecklist: List<ChecklistTemplate> = listOf(
        ChecklistTemplate(
            id = "trade_1",
            group = "TRADE_TEST",
            title = BiText("Coupon Fit-Up Measured (Gap, Face, Bevel)", "কুপন ফিট-আপ মাপা (গ্যাপ, ফেস, বেভেল)"),
            detail = BiText(
                "Verified 30°–37.5° bevel, 1.0–1.5mm root face, 2.5–3.2mm root gap, and zero hi-lo misalignment using caliper & angle gauge.",
                "ভার্নিয়ার ক্যালিপার ও অ্যাঙ্গেল গেজ দিয়ে বেভেল, রুট ফেস, গ্যাপ এবং হাই-লো সমান আছে কি না যাচাই করা।"
            )
        ),
        ChecklistTemplate(
            id = "trade_2",
            group = "TRADE_TEST",
            title = BiText("Root Pass Full Penetration Self-Check", "রুট পাস ফুল পেনিট্রেশন নিজে যাচাই"),
            detail = BiText(
                "Inspect pipe/plate back side: uniform 1–2mm root reinforcement, no suck-back, no incomplete penetration, clean restarts.",
                "পেছন দিক দেখে নিশ্চিত হোন: সমান রুট পেনিট্রেশন, কোনো সাক-ব্যাক বা খালি জায়গা নেই।"
            )
        ),
        ChecklistTemplate(
            id = "trade_3",
            group = "TRADE_TEST",
            title = BiText("Hot, Fill & Cap Inter-Pass Cleaning", "হট, ফিল ও ক্যাপ পাসের মাঝে পরিষ্কার"),
            detail = BiText(
                "100% slag removed, tie-ins feathered by grinder, cap height controlled (1–2.5mm max) with zero undercut or overlap.",
                "প্রতি পাসে ১০০% স্ল্যাগ তোলা, স্টার্ট-স্টপ গ্রাইন্ডিং এবং ক্যাপের উচ্চতা ও প্রান্ত নিখুঁত রাখা।"
            )
        ),
        ChecklistTemplate(
            id = "trade_4",
            group = "TRADE_TEST",
            title = BiText("PPE & Workshop HSE Discipline", "PPE ও ওয়ার্কশপ সেফটি শৃঙ্খলা"),
            detail = BiText(
                "Safety boots, FR jacket/apron, welding gloves, shade 10–12 hood, safety glasses for grinding, ear plugs, and tidy cable routing.",
                "সেফটি বুট, এ্প্রোন, গ্লাভস, হেলমেট, গ্রাইন্ডিং গগলস, ইয়ার প্লাগ ও গোছানো ক্যাবল।"
            )
        ),
        ChecklistTemplate(
            id = "trade_5",
            group = "TRADE_TEST",
            title = BiText("Overseas Documents & Viva Readiness", "বিদেশি চাকরির ডকুমেন্ট ও ভাইভা প্রস্তুতি"),
            detail = BiText(
                "Valid passport, trade certificates, CV with 1G–6G+ processes listed, and fluent answers on E7018/LB52/ER70S-6/316L & PPE viva questions.",
                "বৈধ পাসপোর্ট, ট্রেড সার্টিফিকেট, 1G–6G+ সিভি এবং ইলেকট্রোড কোড ও PPE ভাইভার প্রশ্নের চটপটে উত্তর।"
            )
        )
    )
}
