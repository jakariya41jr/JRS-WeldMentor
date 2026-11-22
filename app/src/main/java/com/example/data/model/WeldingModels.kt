package com.example.data.model

import com.example.i18n.BiText

enum class WeldProcess(val label: String, val fullBi: BiText) {
    SMAW("SMAW", BiText("SMAW / Stick Arc", "SMAW / স্টিক আর্ক")),
    GTAW("GTAW/TIG", BiText("GTAW / TIG", "GTAW / টিগ")),
    FCAW("FCAW", BiText("FCAW / Flux-Cored", "FCAW / ফ্লাক্স-কোরড")),
    MIG("MIG/MAG", BiText("MIG / MAG (GMAW)", "MIG / MAG (GMAW)"))
}

enum class WeldPosition(val code: String, val descBi: BiText) {
    G1("1G", BiText("1G (Flat)", "1G (ফ্ল্যাট)")),
    G2("2G", BiText("2G (Horizontal)", "2G (হরাইজন্টাল)")),
    G3("3G", BiText("3G (Vertical)", "3G (ভার্টিক্যাল)")),
    G4("4G", BiText("4G (Overhead)", "4G (ওভারহেড)")),
    G5("5G", BiText("5G (Pipe Fixed Horiz.)", "5G (পাইপ ফিক্সড হরাইজন্টাল)")),
    G6("6G", BiText("6G (Pipe 45° Fixed)", "6G (পাইপ ৪৫° ফিক্সড)")),
    G6_6_SMAW("6G+6\"SMAW", BiText("6G+ 6\" Pipe SMAW", "6G+ ৬\" পাইপ SMAW")),
    G6_6_GTAW("6G+6\"GTAW", BiText("6G+ 6\" Pipe GTAW", "6G+ ৬\" পাইপ GTAW")),
    G6_12_GTAW("6G+12\"GTAW", BiText("6G+ 12\" Pipe GTAW", "6G+ ১২\" পাইপ GTAW"))
}

enum class BaseMaterial(val code: String, val bi: BiText) {
    CS("CS", BiText("Carbon Steel (CS)", "কার্বন স্টিল (CS)")),
    SS("SS", BiText("Stainless Steel (SS)", "স্টেইনলেস স্টিল (SS)"))
}

enum class JointType(val code: String, val bi: BiText) {
    BUTT("Butt", BiText("Butt Joint", "বাট জয়েন্ট")),
    LAP("Lap", BiText("Lap Joint", "ল্যাপ জয়েন্ট")),
    TEE("Tee", BiText("Tee / Fillet Joint", "টি / ফিলেট জয়েন্ট")),
    CORNER("Corner", BiText("Corner Joint", "কর্নার জয়েন্ট")),
    EDGE("Edge", BiText("Edge Joint", "এজ জয়েন্ট"))
}

enum class JobStatus(val code: String, val bi: BiText) {
    PASS("Pass", BiText("Pass", "পাস")),
    NEEDS_IMPROVEMENT("Needs Improvement", BiText("Needs Improvement", "উন্নতি প্রয়োজন"))
}

enum class DefectTag(val code: String, val bi: BiText) {
    CLEAN_PASS("Clean/Pass", BiText("Clean / Pass", "ক্লিন / পাস")),
    UNDERCUT("Undercut", BiText("Undercut", "আন্ডারকাট")),
    POROSITY("Porosity", BiText("Porosity", "পোরোসিটি / ছিদ্র")),
    SLAG_INCLUSION("Slag Inclusion", BiText("Slag Inclusion", "স্ল্যাগ ইনক্লুশন")),
    LACK_OF_FUSION("Lack of Fusion", BiText("Lack of Fusion", "ফিউশনের অভাব")),
    BURN_THROUGH("Burn-through", BiText("Burn-through", "বার্ন-থ্রু / গলে যাওয়া"))
}

data class QuizQuestion(
    val id: String,
    val topic: BiText,
    val question: BiText,
    val options: List<BiText>,
    val correctIndex: Int,
    val explanation: BiText
)

data class FlashCard(
    val id: String,
    val category: BiText,
    val codeTitle: String,
    val frontSubtitle: BiText,
    val backSpecs: BiText,
    val backTip: BiText
)

data class DefectGuide(
    val id: String,
    val title: BiText,
    val visualCue: BiText,
    val causes: BiText,
    val fixes: BiText
)

data class HandsOnTip(
    val id: String,
    val title: BiText,
    val category: BiText,
    val summary: BiText,
    val steps: List<BiText>
)

data class ParamRefRow(
    val process: String,
    val rodOrWire: String,
    val diameter: String,
    val position: String,
    val ampsOrVolts: String,
    val gasFlow: String,
    val polarity: String
)

data class ChecklistTemplate(
    val id: String,
    val group: String, // "STANCE" or "TRADE_TEST"
    val title: BiText,
    val detail: BiText
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
