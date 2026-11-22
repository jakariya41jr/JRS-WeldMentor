package com.example.i18n

enum class AppLanguage(val code: String, val displayLabel: String) {
    EN("en", "EN"),
    BN("bn", "বাংলা");

    fun toggled(): AppLanguage = if (this == EN) BN else EN
}

data class BiText(val en: String, val bn: String) {
    fun get(lang: AppLanguage): String = if (lang == AppLanguage.BN) bn else en
}
