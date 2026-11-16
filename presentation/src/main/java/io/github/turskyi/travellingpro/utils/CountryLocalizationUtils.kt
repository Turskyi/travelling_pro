package io.github.turskyi.travellingpro.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import io.github.turskyi.travellingpro.entities.Country
import io.github.turskyi.travellingpro.entities.VisitedCountryNode
import java.util.Locale

object CountryLocalizationUtils {

    private val ukLocale: Locale = Locale.forLanguageTag("uk")

    private val ukrainianOverrides = mapOf(
        "United States" to "США",
        "United States of America" to "США",
        "United Kingdom" to "Велика Британія",
        "Czechia" to "Чехія",
        "Czech Republic" to "Чехія",
        "South Korea" to "Південна Корея",
        "North Korea" to "Північна Корея",
        "Russia" to "Росія",
        "Syria" to "Сирія",
        "Moldova" to "Молдова",
        "Vietnam" to "В'єтнам",
        "Laos" to "Лаос",
        "Vatican City" to "Ватикан",
        "Taiwan" to "Тайвань",
        "Kosovo" to "Косово",
        "Bolivia" to "Болівія",
        "Venezuela" to "Венесуела",
        "Tanzania" to "Танзанія",
        "Iran" to "Іран",
        "Ivory Coast" to "Кот-д'Івуар",
        "Côte d'Ivoire" to "Кот-д'Івуар",
        "Cape Verde" to "Кабо-Верде",
        "Cabo Verde" to "Кабо-Верде",
        "East Timor" to "Східний Тимор",
        "Timor-Leste" to "Тимор-Лешті",
        "Micronesia" to "Мікронезія",
        "Eswatini" to "Есватіні",
        "Swaziland" to "Есватіні",
        "Brunei" to "Бруней",
        "Myanmar" to "М'янма",
        "Burma" to "М'янма",
        "Congo" to "Конго",
        "DR Congo" to "ДР Конго",
        "Republic of the Congo" to "Республіка Конго",
        "Democratic Republic of the Congo" to "Демократична Республіка Конго"
    )

    private val englishToIsoCodeMap: Map<String, String> by lazy {
        val map = mutableMapOf<String, String>()
        for (code in Locale.getISOCountries()) {
            val locale = getLocaleForRegion(code)
            val englishName = locale.getDisplayCountry(Locale.ENGLISH)
            if (englishName.isNotBlank()) {
                map[englishName.lowercase()] = code
            }
        }
        val customMap = mapOf(
            "united states" to "US",
            "united kingdom" to "GB",
            "czechia" to "CZ",
            "south korea" to "KR",
            "north korea" to "KP",
            "vatican city" to "VA",
            "taiwan" to "TW",
            "russia" to "RU",
            "syria" to "SY",
            "moldova" to "MD",
            "laos" to "LA",
            "vietnam" to "VN",
            "bolivia" to "BO",
            "venezuela" to "VE",
            "tanzania" to "TZ",
            "iran" to "IR",
            "ivory coast" to "CI",
            "cape verde" to "CV",
            "east timor" to "TL",
            "micronesia" to "FM",
            "eswatini" to "SZ",
            "kosovo" to "XK",
            "côte d'ivoire" to "CI",
            "cabo verde" to "CV",
            "timor-leste" to "TL",
            "myanmar" to "MM",
            "dr congo" to "CD"
        )
        for ((name, code) in customMap) {
            map[name] = code
        }
        map
    }

    private data class CountryEntry(val code: String, val englishName: String, val ukrainianName: String)

    private val countryIndex: List<CountryEntry> by lazy {
        val list = mutableListOf<CountryEntry>()
        for (code in Locale.getISOCountries()) {
            val locale = getLocaleForRegion(code)
            val en = locale.getDisplayCountry(Locale.ENGLISH)
            val uk = ukrainianOverrides[en] ?: locale.getDisplayCountry(ukLocale)
            if (en.isNotBlank() && uk.isNotBlank()) {
                list.add(CountryEntry(code, en, uk))
            }
        }
        for ((en, uk) in ukrainianOverrides) {
            val code = englishToIsoCodeMap[en.lowercase()] ?: ""
            if (!list.any { it.englishName.equals(en, ignoreCase = true) }) {
                list.add(CountryEntry(code, en, uk))
            }
        }
        list
    }

    private fun getLocaleForRegion(code: String): Locale {
        return try {
            Locale.Builder().setRegion(code).build()
        } catch (_: Exception) {
            Locale.forLanguageTag("und-$code")
        }
    }

    fun isUkrainianSelected(context: Context?): Boolean {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty && locales[0] != null) {
            locales[0]!!.language == "uk"
        } else {
            val current = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                context?.resources?.configuration?.locales?.get(0) ?: Locale.getDefault()
            } else {
                @Suppress("DEPRECATION")
                context?.resources?.configuration?.locale ?: Locale.getDefault()
            }
            current.language == "uk"
        }
    }

    fun extractIsoCode(name: String, flag: String): String? {
        if (flag.contains("flagcdn.com/")) {
            val candidate = flag.substringAfterLast("/").substringBefore(".").uppercase()
            if (candidate.length == 2) {
                return candidate
            }
        }
        return englishToIsoCodeMap[name.lowercase()]
    }

    fun getDisplayName(name: String, flag: String, context: Context?): String {
        if (!isUkrainianSelected(context)) {
            return name
        }
        ukrainianOverrides[name]?.let { return it }

        val isoCode = extractIsoCode(name, flag)
        if (isoCode != null) {
            val ukrainianName = getLocaleForRegion(isoCode).getDisplayCountry(ukLocale)
            if (ukrainianName.isNotBlank() && !ukrainianName.equals(isoCode, ignoreCase = true)) {
                return ukrainianName
            }
        }
        return name
    }

    fun findMatchingEnglishNames(query: String): List<String> {
        val cleanQuery = query.trim().lowercase()
        if (cleanQuery.isEmpty()) return emptyList()

        val matching = mutableSetOf<String>()

        for ((englishName, ukrainianName) in ukrainianOverrides) {
            if (ukrainianName.lowercase().contains(cleanQuery) || englishName.lowercase().contains(cleanQuery)) {
                matching.add(englishName)
            }
        }

        for ((_, englishName, ukrainianName) in countryIndex) {
            if (ukrainianName.lowercase().contains(cleanQuery) || englishName.lowercase().contains(cleanQuery)) {
                matching.add(englishName)
            }
        }

        return matching.toList()
    }
}

fun Country.getDisplayName(context: Context?): String =
    CountryLocalizationUtils.getDisplayName(name, flag, context)

fun VisitedCountryNode.getDisplayName(context: Context?): String =
    CountryLocalizationUtils.getDisplayName(title, flag, context)
