import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:medycatalog/core/error/failure.dart';
import 'package:medycatalog/core/network/api_client.dart';
import 'package:medycatalog/features/learning/presentation/locale_controller.dart';

final learningRepositoryProvider = Provider<LearningRepository>((ref) {
  return LearningRepository(ref.watch(dioProvider));
});

class LearningRepository {
  LearningRepository(this._dio);

  final Dio _dio;

  Future<List<AppSpeechLocale>> locales() async {
    try {
      final response = await _dio.get<List<dynamic>>('/learning/locales');
      return (response.data ?? const [])
          .map((item) => AppSpeechLocale.fromJson(item as Map<String, dynamic>))
          .toList();
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<FreePreview> freePreview({required String examId, required String locale}) async {
    try {
      final response = await _dio.get<Map<String, dynamic>>(
        '/learning/exams/$examId/free-preview',
        queryParameters: {'locale': locale},
      );
      return FreePreview.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<ResumeState?> resume() async {
    try {
      final response = await _dio.get<Map<String, dynamic>>('/learning/resume');
      if (response.data == null || response.data!.isEmpty) {
        return null;
      }
      return ResumeState.fromJson(response.data!);
    } on DioException catch (error) {
      if (error.response?.statusCode == 200) {
        return null;
      }
      throw _wrap(error);
    }
  }

  Future<void> saveResume({
    required String route,
    required String title,
    String? examId,
    String payload = '{}',
  }) async {
    try {
      await _dio.put<Map<String, dynamic>>(
        '/learning/resume',
        data: {
          'route': route,
          'title': title,
          if (examId != null) 'examId': examId,
          'payload': payload,
        },
      );
    } on DioException {
      // Resume is best-effort so navigation never blocks.
    }
  }

  Future<DoubtAnswer> askDoubt({
    required String text,
    required String locale,
    required String source,
    String? examId,
    String? topicId,
    String? questionId,
  }) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(
        '/learning/doubts',
        data: {
          'text': text,
          'locale': locale,
          'source': source,
          if (examId != null) 'examId': examId,
          if (topicId != null) 'topicId': topicId,
          if (questionId != null) 'questionId': questionId,
        },
      );
      return DoubtAnswer.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<LearningLibrary> library(String locale) async {
    try {
      final response = await _dio.get<Map<String, dynamic>>(
        '/learning/library',
        queryParameters: {'locale': locale},
      );
      return LearningLibrary.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Object _wrap(DioException error) => error.error is Failure ? error.error as Failure : Failure.offline;
}

class FreePreview {
  const FreePreview({
    required this.questionId,
    required this.examCode,
    required this.examYear,
    required this.questionText,
    required this.options,
    this.simpleExplanation,
    this.detailedExplanation,
    this.methodScript,
    this.trapWording,
    required this.spokenScript,
    required this.ttsCode,
    required this.variants,
    this.topicName,
    required this.topicHitCount,
    required this.focusLabel,
  });

  final String questionId;
  final String examCode;
  final int examYear;
  final String questionText;
  final List<PreviewOption> options;
  final String? simpleExplanation;
  final String? detailedExplanation;
  final String? methodScript;
  final String? trapWording;
  final String spokenScript;
  final String ttsCode;
  final List<QuestionVariant> variants;
  final String? topicName;
  final int topicHitCount;
  final String focusLabel;

  factory FreePreview.fromJson(Map<String, dynamic> json) {
    return FreePreview(
      questionId: json['questionId'] as String? ?? '',
      examCode: json['examCode'] as String? ?? '',
      examYear: (json['examYear'] as num?)?.toInt() ?? 2025,
      questionText: json['questionText'] as String? ?? '',
      options: (json['options'] as List<dynamic>? ?? const [])
          .map((item) => PreviewOption.fromJson(item as Map<String, dynamic>))
          .toList(),
      simpleExplanation: json['simpleExplanation'] as String?,
      detailedExplanation: json['detailedExplanation'] as String?,
      methodScript: json['methodScript'] as String?,
      trapWording: json['trapWording'] as String?,
      spokenScript: json['spokenScript'] as String? ?? '',
      ttsCode: json['ttsCode'] as String? ?? 'en-IN',
      variants: (json['variants'] as List<dynamic>? ?? const [])
          .map((item) => QuestionVariant.fromJson(item as Map<String, dynamic>))
          .toList(),
      topicName: json['topicName'] as String?,
      topicHitCount: (json['topicHitCount'] as num?)?.toInt() ?? 1,
      focusLabel: json['focusLabel'] as String? ?? '',
    );
  }
}

class PreviewOption {
  const PreviewOption({required this.text, required this.order, required this.correct});

  final String text;
  final int order;
  final bool correct;

  factory PreviewOption.fromJson(Map<String, dynamic> json) {
    return PreviewOption(
      text: json['text'] as String? ?? '',
      order: (json['order'] as num?)?.toInt() ?? 0,
      correct: json['correct'] as bool? ?? false,
    );
  }
}

class QuestionVariant {
  const QuestionVariant({required this.variantText, required this.trapNote});

  final String variantText;
  final String trapNote;

  factory QuestionVariant.fromJson(Map<String, dynamic> json) {
    return QuestionVariant(
      variantText: json['variantText'] as String? ?? '',
      trapNote: json['trapNote'] as String? ?? '',
    );
  }
}

class ResumeState {
  const ResumeState({required this.route, required this.title, this.examId});

  final String route;
  final String title;
  final String? examId;

  factory ResumeState.fromJson(Map<String, dynamic> json) {
    return ResumeState(
      route: json['route'] as String? ?? '/home',
      title: json['title'] as String? ?? 'Continue',
      examId: json['examId'] as String?,
    );
  }
}

class DoubtAnswer {
  const DoubtAnswer({
    required this.id,
    required this.ttsCode,
    required this.queryText,
    required this.answerShort,
    required this.spokenScript,
    this.topicName,
    this.focusLabel,
  });

  final String id;
  final String ttsCode;
  final String queryText;
  final String answerShort;
  final String spokenScript;
  final String? topicName;
  final String? focusLabel;

  factory DoubtAnswer.fromJson(Map<String, dynamic> json) {
    return DoubtAnswer(
      id: json['id'] as String? ?? '',
      ttsCode: json['ttsCode'] as String? ?? 'en-IN',
      queryText: json['queryText'] as String? ?? '',
      answerShort: json['answerShort'] as String? ?? '',
      spokenScript: json['spokenScript'] as String? ?? '',
      topicName: json['topicName'] as String?,
      focusLabel: json['focusLabel'] as String?,
    );
  }
}

class LearningLibrary {
  const LearningLibrary({required this.answers, required this.queries});

  final List<SavedPracticeAnswer> answers;
  final List<DoubtAnswer> queries;

  factory LearningLibrary.fromJson(Map<String, dynamic> json) {
    return LearningLibrary(
      answers: (json['answers'] as List<dynamic>? ?? const [])
          .map((item) => SavedPracticeAnswer.fromJson(item as Map<String, dynamic>))
          .toList(),
      queries: (json['queries'] as List<dynamic>? ?? const [])
          .map((item) => DoubtAnswer.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class SavedPracticeAnswer {
  const SavedPracticeAnswer({
    required this.sessionId,
    required this.questionId,
    required this.questionText,
    required this.correct,
    required this.explanation,
    required this.spokenScript,
  });

  final String sessionId;
  final String questionId;
  final String questionText;
  final bool correct;
  final String explanation;
  final String spokenScript;

  factory SavedPracticeAnswer.fromJson(Map<String, dynamic> json) {
    return SavedPracticeAnswer(
      sessionId: json['sessionId'] as String? ?? '',
      questionId: json['questionId'] as String? ?? '',
      questionText: json['questionText'] as String? ?? '',
      correct: json['correct'] as bool? ?? false,
      explanation: json['explanation'] as String? ?? '',
      spokenScript: json['spokenScript'] as String? ?? '',
    );
  }
}
