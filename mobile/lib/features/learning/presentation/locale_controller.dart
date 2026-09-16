import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

const _kLocale = 'medycatalog.locale';

final localeControllerProvider = StateNotifierProvider<LocaleController, AppSpeechLocale>((ref) {
  return LocaleController();
});

class AppSpeechLocale {
  const AppSpeechLocale({
    required this.code,
    required this.englishName,
    required this.nativeName,
    required this.ttsCode,
    required this.classical,
  });

  final String code;
  final String englishName;
  final String nativeName;
  final String ttsCode;
  final bool classical;

  factory AppSpeechLocale.fromJson(Map<String, dynamic> json) {
    return AppSpeechLocale(
      code: json['code'] as String? ?? 'en',
      englishName: json['englishName'] as String? ?? 'English',
      nativeName: json['nativeName'] as String? ?? 'English',
      ttsCode: json['ttsCode'] as String? ?? 'en-IN',
      classical: json['classical'] as bool? ?? false,
    );
  }

  static const english = AppSpeechLocale(
    code: 'en',
    englishName: 'English',
    nativeName: 'English',
    ttsCode: 'en-IN',
    classical: false,
  );
}

class LocaleController extends StateNotifier<AppSpeechLocale> {
  LocaleController() : super(AppSpeechLocale.english) {
    _restore();
  }

  Future<void> _restore() async {
    final prefs = await SharedPreferences.getInstance();
    final code = prefs.getString(_kLocale);
    if (code != null && code.isNotEmpty) {
      state = state.copyWith(code: code);
    }
  }

  Future<void> select(AppSpeechLocale locale) async {
    state = locale;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_kLocale, locale.code);
  }
}

extension on AppSpeechLocale {
  AppSpeechLocale copyWith({String? code}) {
    return AppSpeechLocale(
      code: code ?? this.code,
      englishName: englishName,
      nativeName: nativeName,
      ttsCode: ttsCode,
      classical: classical,
    );
  }
}
