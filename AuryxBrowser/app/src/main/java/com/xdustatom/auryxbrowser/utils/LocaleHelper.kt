package com.xdustatom.auryxbrowser.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "auryx_locale_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    // Language codes
    const val ENGLISH = "en"
    const val ITALIAN = "it"
    const val SPANISH = "es"
    const val FRENCH = "fr"
    const val GERMAN = "de"

    data class Language(
        val code: String,
        val displayName: String,
        val nativeName: String
    )

    val supportedLanguages = listOf(
        Language(ENGLISH, "English", "English"),
        Language(ITALIAN, "Italian", "Italiano"),
        Language(SPANISH, "Spanish", "Español"),
        Language(FRENCH, "French", "Français"),
        Language(GERMAN, "German", "Deutsch")
    )

    /**
     * Get the saved language code, or default to system language
     */
    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, ENGLISH) ?: ENGLISH
    }

    /**
     * Save the selected language code
     */
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    /**
     * Apply saved locale to context
     */
    fun applyLocale(context: Context): Context {
        val languageCode = getLanguage(context)
        return setLocale(context, languageCode)
    }

    /**
     * Set a specific locale
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    /**
     * Get the display name for a language code
     */
    fun getLanguageDisplayName(code: String): String {
        return supportedLanguages.find { it.code == code }?.nativeName ?: "English"
    }

    /**
     * Get the index of current language in supported languages list
     */
    fun getCurrentLanguageIndex(context: Context): Int {
        val currentCode = getLanguage(context)
        return supportedLanguages.indexOfFirst { it.code == currentCode }.coerceAtLeast(0)
    }
}
