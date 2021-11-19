/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-present eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.adblockplussbrowser.settings.data.local

internal class HardcodedSubscriptions {

    val easylist = subscription {
        url = "https://filter-list-downloads.eyeo.com/easylist.txt"
        languages("en")
    }

    val acceptableAds = subscription {
        title = "Acceptable Ads"
        url = "https://filter-list-downloads.eyeo.com/exceptionrules.txt"
    }

    // Based on: https://gitlab.com/eyeo/adblockplus/adblockpluscore/-/blob/next/data/subscriptions.json
    val defaultPrimarySubscriptions = listOf(easylist) + subscriptions {
        subscription {
            url = "https://filter-list-downloads.eyeo.com/abpindo.txt"
            languages("id", "ms")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/abpvn.txt"
            languages("vi")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/bulgarian_list.txt"
            languages("bg")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/dandelion_sprouts_nordic_filters.txt"
            languages("no", "nb", "nn", "da", "is", "fo", "kl")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/easylistchina.txt"
            languages("zh")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/easylistczechslovak.txt"
            languages("cs", "sk")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/easylistdutch.txt"
            languages("nl")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/easylistgermany.txt"
            languages("de")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/israellist.txt"
            languages("he")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/easylistitaly.txt"
            languages("it")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/easylistlithuania.txt"
            languages("lt")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/easylistpolish.txt"
            languages("pl")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/easylistportuguese.txt"
            languages("pt")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/easylistspanish.txt"
            languages("es")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/indianlist.txt"
            languages("bn", "gu", "hi", "pa", "as", "mr", "ml", "te", "kn", "or", "ne", "si")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/koreanlist.txt"
            languages("ko")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/latvianlist.txt"
            languages("lv")
        }
        subscription {
            // FIXME - using combined list, since only liste_ar+liste_fr.txt is not available
            url = "https://filter-list-downloads.eyeo.com/liste_ar+liste_fr+easylist.txt"
            languages("ar")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/liste_fr.txt"
            languages("fr")
        }
        subscription {
            url = "https://filter-list-downloads.eyeo.com/rolist.txt"
            languages("ro")
        }
        subscription {
            // FIXME - using combined list, since only ruadlist.txt is not available
            url = "https://filter-list-downloads.eyeo.com/ruadlist+easylist.txt"
            languages("ru", "uk")
        }
    }

    // TODO: Decide which title we want for the following and if we want to localize them:
    val additionalTracking = subscription {
        title = "Block additional tracking"
        url = "https://filter-list-downloads.eyeo.com/easyprivacy.txt"
    }

    val socialMediaTracking = subscription {
        title = "Block social media icons tracking"
        url = "https://filter-list-downloads.eyeo.com/fanboy-social.txt"
    }
    val defaultOtherSubscriptions = listOf(additionalTracking, socialMediaTracking)

    // Based on: https://gitlab.com/eyeo/adblockplus/abpui/adblockplusui/-/blob/master/data/locales.json
    companion object {
        val LANGUAGE_DESCRIPTION_MAP = mutableMapOf(
            "af" to "Afrikaans",
            "am" to "ኣማርኛ",
            "ar" to "العربية",
            "as" to "অসমীয়া",
            "ast" to "Asturianu",
            "az" to "Azərbaycan",
            "be" to "Беларуская мова",
            "bg" to "български",
            "bn" to "বাংলা (ভারত)",
            "br" to "ar brezhoneg",
            "bs" to "bosanski",
            "ca" to "català",
            "cs" to "čeština",
            "cy" to "Cymraeg",
            "da" to "dansk",
            "de" to "Deutsch",
            "dsb" to "dolnoserbski",
            "el" to "ελληνικά",
            "en" to "English",
            "eo" to "Esperanto",
            "es" to "español",
            "et" to "eesti keel",
            "eu" to "euskara",
            "fa" to "فارسى",
            "fi" to "suomi",
            "fil" to "Filipino",
            "fo" to "føroyskt",
            "fr" to "français",
            "fy" to "Frysk",
            "gl" to "Galego",
            "gu" to "ગુજરાતી (ભારત)",
            "he" to "עברית",
            "hi" to "भारतीय",
            "hr" to "Hrvatski",
            "hsb" to "hornjoserbsce",
            "hu" to "magyar",
            "hy" to "Հայերեն",
            "id" to "Bahasa Indonesia",
            "is" to "íslenska",
            "it" to "italiano",
            "ja" to "日本語",
            "ka" to "ქართული",
            "kab" to "Taqbaylit",
            "kk" to "Қазақ тілі",
            "kl" to "kalaallisut",
            "kn" to "ಕನ್ನಡ",
            "ko" to "한국어",
            "lt" to "lietuvių kalba",
            "lv" to "latviešu valoda",
            "mg" to "Malagasy",
            "mk" to "македонски",
            "ml" to "മലയാളം",
            "mr" to "मराठी",
            "ms" to "Melayu",
            "nb" to "norsk",
            "ne" to "नेपाली",
            "nl" to "Nederlands",
            "nn" to "norsk",
            "no" to "norsk",
            "or" to "ଓଡ଼ିଆ",
            "pa" to "ਪੰਜਾਬੀ (ਭਾਰਤ)",
            "pl" to "polski",
            "pt" to "português",
            "rm" to "rumantsch",
            "ro" to "română",
            "ru" to "Русский",
            "si" to "සිංහල",
            "sk" to "slovenčina",
            "sl" to "slovenščina",
            "sq" to "shqip",
            "sr" to "српски",
            "sv" to "svenska",
            "sw" to "Kiswahili",
            "ta" to "தமிழ்",
            "te" to "తెలుగు",
            "th" to "ภาษาไทย",
            "tr" to "Türkçe",
            "uk" to "українська",
            "ur" to "اردو",
            "uz" to "o’zbek",
            "vi" to "Tiếng Việt",
            "zh" to "中文"
        )
    }
}