import 'package:flutter_test/flutter_test.dart';
import 'package:medycatalog/features/learning/data/learning_repository.dart';
import 'package:medycatalog/features/learning/presentation/locale_controller.dart';

void main() {
  test('parses fourteen Indian explanation locales', () {
    final locales = [
      {'code': 'en', 'englishName': 'English', 'nativeName': 'English', 'ttsCode': 'en-IN', 'classical': false},
      {'code': 'hi', 'englishName': 'Hindi', 'nativeName': 'हिन्दी', 'ttsCode': 'hi-IN', 'classical': false},
      {'code': 'ta', 'englishName': 'Tamil', 'nativeName': 'தமிழ்', 'ttsCode': 'ta-IN', 'classical': true},
      {'code': 'sa', 'englishName': 'Sanskrit', 'nativeName': 'संस्कृतम्', 'ttsCode': 'sa-IN', 'classical': true},
      {'code': 'kn', 'englishName': 'Kannada', 'nativeName': 'ಕನ್ನಡ', 'ttsCode': 'kn-IN', 'classical': true},
      {'code': 'te', 'englishName': 'Telugu', 'nativeName': 'తెలుగు', 'ttsCode': 'te-IN', 'classical': true},
      {'code': 'ml', 'englishName': 'Malayalam', 'nativeName': 'മലയാളം', 'ttsCode': 'ml-IN', 'classical': true},
      {'code': 'or', 'englishName': 'Odia', 'nativeName': 'ଓଡ଼ିଆ', 'ttsCode': 'or-IN', 'classical': true},
      {'code': 'mr', 'englishName': 'Marathi', 'nativeName': 'मराठी', 'ttsCode': 'mr-IN', 'classical': true},
      {'code': 'bn', 'englishName': 'Bengali', 'nativeName': 'বাংলা', 'ttsCode': 'bn-IN', 'classical': true},
      {'code': 'as', 'englishName': 'Assamese', 'nativeName': 'অসমীয়া', 'ttsCode': 'as-IN', 'classical': true},
      {'code': 'pi', 'englishName': 'Pali', 'nativeName': 'पालि', 'ttsCode': 'sa-IN', 'classical': true},
      {'code': 'pra', 'englishName': 'Prakrit', 'nativeName': 'प्राकृत', 'ttsCode': 'sa-IN', 'classical': true},
      {'code': 'ur', 'englishName': 'Urdu', 'nativeName': 'اردو', 'ttsCode': 'ur-IN', 'classical': false},
    ].map(AppSpeechLocale.fromJson).toList();
    expect(locales, hasLength(14));
    expect(locales.where((item) => item.classical), hasLength(11));
  });

  test('free preview keeps answer and explanation fields', () {
    final preview = FreePreview.fromJson({
      'questionId': 'q1',
      'examCode': 'JEE',
      'examYear': 2025,
      'questionText': 'Two like charges repel because:',
      'options': [
        {'text': 'Coulomb force', 'order': 1, 'correct': true},
      ],
      'simpleExplanation': 'Like charges repel.',
      'detailedExplanation': 'Coulomb’s law.',
      'spokenScript': 'Listen slowly.',
      'ttsCode': 'hi-IN',
      'variants': [
        {'variantText': 'Two charges of the same sign', 'trapNote': 'Same idea'},
      ],
      'focusLabel': 'This topic area has come 1 time — focus here.',
      'topicHitCount': 1,
    });
    expect(preview.examYear, 2025);
    expect(preview.options.first.correct, isTrue);
    expect(preview.variants, hasLength(1));
  });
}
