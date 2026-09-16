package in.techgeneza.medycatalog.modules.learning.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum AppLocale {
    EN("en", "English", "English", "en-IN", false),
    HI("hi", "Hindi", "हिन्दी", "hi-IN", false),
    TA("ta", "Tamil", "தமிழ்", "ta-IN", true),
    SA("sa", "Sanskrit", "संस्कृतम्", "sa-IN", true),
    KN("kn", "Kannada", "ಕನ್ನಡ", "kn-IN", true),
    TE("te", "Telugu", "తెలుగు", "te-IN", true),
    ML("ml", "Malayalam", "മലയാളം", "ml-IN", true),
    OR("or", "Odia", "ଓଡ଼ିଆ", "or-IN", true),
    MR("mr", "Marathi", "मराठी", "mr-IN", true),
    BN("bn", "Bengali", "বাংলা", "bn-IN", true),
    AS("as", "Assamese", "অসমীয়া", "as-IN", true),
    PI("pi", "Pali", "पालि", "sa-IN", true),
    PRA("pra", "Prakrit", "प्राकृत", "sa-IN", true),
    UR("ur", "Urdu", "اردو", "ur-IN", false);

    private final String code;
    private final String englishName;
    private final String nativeName;
    private final String ttsCode;
    private final boolean classical;

    AppLocale(String code, String englishName, String nativeName, String ttsCode, boolean classical) {
        this.code = code;
        this.englishName = englishName;
        this.nativeName = nativeName;
        this.ttsCode = ttsCode;
        this.classical = classical;
    }

    public String code() {
        return code;
    }

    public String englishName() {
        return englishName;
    }

    public String nativeName() {
        return nativeName;
    }

    public String ttsCode() {
        return ttsCode;
    }

    public boolean classical() {
        return classical;
    }

    public static List<AppLocale> supported() {
        return Arrays.asList(values());
    }

    public static AppLocale fromCode(String raw) {
        if (raw == null || raw.isBlank()) {
            return EN;
        }
        String code = raw.trim().toLowerCase(Locale.ROOT);
        if (code.startsWith("pa") && code.contains("prak")) {
            return PRA;
        }
        for (AppLocale locale : values()) {
            if (locale.code.equals(code) || locale.ttsCode.toLowerCase(Locale.ROOT).startsWith(code)) {
                return locale;
            }
        }
        return EN;
    }
}
