package in.techgeneza.medycatalog.modules.learning.application;

import in.techgeneza.medycatalog.modules.learning.domain.AppLocale;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;

@Service
public class SpokenScriptService {

    private static final Map<AppLocale, String> INTRO = new EnumMap<>(AppLocale.class);

    static {
        INTRO.put(AppLocale.EN, "Listen slowly. We will walk the method, then the answer.");
        INTRO.put(AppLocale.HI, "धीरे सुनिए। पहले तरीका, फिर उत्तर।");
        INTRO.put(AppLocale.TA, "மெதுவாகக் கேளுங்கள். முதலில் முறை, பிறகு விடை.");
        INTRO.put(AppLocale.SA, "शनैः शृणुत। प्रथमं विधिः, ततः उत्तरम्।");
        INTRO.put(AppLocale.KN, "ನಿಧಾನವಾಗಿ ಕೇಳಿ. ಮೊದಲು ವಿಧಾನ, ನಂತರ ಉತ್ತರ.");
        INTRO.put(AppLocale.TE, "నిదానంగా వినండి. ముందు పద్ధతి, తర్వాత సమాధానం.");
        INTRO.put(AppLocale.ML, "സാവധാനം കേൾക്കുക. ആദ്യം രീതി, പിന്നെ ഉത്തരം.");
        INTRO.put(AppLocale.OR, "ଧୀରେ ଶୁଣନ୍ତୁ। ପ୍ରଥମେ ପଦ୍ଧତି, ପରେ ଉତ୍ତର।");
        INTRO.put(AppLocale.MR, "हळू ऐका. आधी पद्धत, नंतर उत्तर.");
        INTRO.put(AppLocale.BN, "আস্তে শুনুন। আগে পদ্ধতি, তারপর উত্তর।");
        INTRO.put(AppLocale.AS, "লাহে শুনক। প্ৰথমে পদ্ধতি, তাৰ পাছত উত্তৰ।");
        INTRO.put(AppLocale.PI, "सणिकं सुणाथ। पठमं विधि, ततो उत्तर।");
        INTRO.put(AppLocale.PRA, "सणिए सुणेह। पढमं विधि, ता उत्तर।");
        INTRO.put(AppLocale.UR, "آہستہ سنیے۔ پہلے طریقہ، پھر جواب۔");
    }

    public String wrap(AppLocale locale, String body) {
        String text = body == null || body.isBlank() ? "No explanation is stored for this item yet." : body.trim();
        return INTRO.getOrDefault(locale, INTRO.get(AppLocale.EN)) + " " + text;
    }

    public String shortAnswer(AppLocale locale, String english) {
        if (locale == AppLocale.HI) {
            return "संक्षेप: " + english;
        }
        if (locale == AppLocale.UR) {
            return "مختصر: " + english;
        }
        return english;
    }
}
