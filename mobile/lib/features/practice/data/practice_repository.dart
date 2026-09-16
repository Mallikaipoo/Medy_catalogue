import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:medycatalog/core/error/failure.dart';
import 'package:medycatalog/core/network/api_client.dart';

final practiceRepositoryProvider = Provider<PracticeRepository>((ref) {
  return PracticeRepository(ref.watch(dioProvider));
});

class PracticeRepository {
  PracticeRepository(this._dio);

  final Dio _dio;

  Future<PracticeSession> start({
    required String examId,
    String? subjectId,
    String? chapterId,
    required int questionCount,
  }) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(
        '/practice/start',
        data: {
          'examId': examId,
          if (subjectId != null) 'subjectId': subjectId,
          if (chapterId != null) 'chapterId': chapterId,
          'questionCount': questionCount,
        },
      );
      return PracticeSession.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<PracticeSession> get(String sessionId) async {
    try {
      final response = await _dio.get<Map<String, dynamic>>('/practice/$sessionId');
      return PracticeSession.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<PracticeSession> answer({
    required String sessionId,
    required String questionId,
    List<String> optionIds = const [],
    String? numericalAnswer,
    bool markedForReview = false,
    bool clear = false,
  }) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(
        '/practice/$sessionId/answer',
        data: {
          'questionId': questionId,
          'optionIds': optionIds,
          'numericalAnswer': numericalAnswer,
          'markedForReview': markedForReview,
          'clear': clear,
        },
      );
      return PracticeSession.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<PracticeResult> complete(String sessionId) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>('/practice/$sessionId/complete');
      return PracticeResult.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<PracticeReview> review(String sessionId) async {
    try {
      final response = await _dio.get<Map<String, dynamic>>('/practice/$sessionId/review');
      return PracticeReview.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Object _wrap(DioException error) => error.error is Failure ? error.error as Failure : Failure.offline;
}

class PracticeSession {
  const PracticeSession({
    required this.id,
    required this.status,
    required this.remainingSeconds,
    required this.items,
  });

  final String id;
  final String status;
  final int remainingSeconds;
  final List<SessionItem> items;

  factory PracticeSession.fromJson(Map<String, dynamic> json) {
    return PracticeSession(
      id: json['id'] as String,
      status: json['status'] as String,
      remainingSeconds: (json['remainingSeconds'] as num?)?.toInt() ?? 0,
      items: (json['items'] as List<dynamic>? ?? const [])
          .map((item) => SessionItem.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class SessionItem {
  const SessionItem({
    required this.order,
    required this.question,
    required this.selectedOptionIds,
    required this.markedForReview,
    this.numericalAnswer,
  });

  final int order;
  final StudentQuestion question;
  final List<String> selectedOptionIds;
  final String? numericalAnswer;
  final bool markedForReview;

  factory SessionItem.fromJson(Map<String, dynamic> json) {
    return SessionItem(
      order: json['order'] as int,
      question: StudentQuestion.fromJson(json['question'] as Map<String, dynamic>),
      selectedOptionIds: (json['selectedOptionIds'] as List<dynamic>? ?? const []).cast<String>(),
      numericalAnswer: json['numericalAnswer'] as String?,
      markedForReview: json['markedForReview'] as bool? ?? false,
    );
  }
}

class StudentQuestion {
  const StudentQuestion({
    required this.id,
    required this.questionText,
    required this.questionType,
    required this.allowsMultipleOptions,
    required this.requiresNumerical,
    required this.difficulty,
    required this.options,
  });

  final String id;
  final String questionText;
  final String questionType;
  final bool allowsMultipleOptions;
  final bool requiresNumerical;
  final String difficulty;
  final List<StudentOption> options;

  factory StudentQuestion.fromJson(Map<String, dynamic> json) {
    return StudentQuestion(
      id: json['id'] as String,
      questionText: json['questionText'] as String,
      questionType: json['questionType'] as String,
      allowsMultipleOptions: json['allowsMultipleOptions'] as bool? ?? false,
      requiresNumerical: json['requiresNumerical'] as bool? ?? false,
      difficulty: json['difficulty'] as String? ?? 'EASY',
      options: (json['options'] as List<dynamic>? ?? const [])
          .map((item) => StudentOption.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class StudentOption {
  const StudentOption({required this.id, required this.text, required this.order});

  final String id;
  final String text;
  final int order;

  factory StudentOption.fromJson(Map<String, dynamic> json) {
    return StudentOption(
      id: json['id'] as String,
      text: json['text'] as String,
      order: json['order'] as int? ?? 0,
    );
  }
}

class PracticeResult {
  const PracticeResult({
    required this.sessionId,
    required this.score,
    required this.totalMarks,
    required this.correct,
    required this.wrong,
    required this.skipped,
    required this.timeTakenSeconds,
    required this.accuracyPercent,
  });

  final String sessionId;
  final double score;
  final double totalMarks;
  final int correct;
  final int wrong;
  final int skipped;
  final int timeTakenSeconds;
  final double accuracyPercent;

  factory PracticeResult.fromJson(Map<String, dynamic> json) {
    return PracticeResult(
      sessionId: json['sessionId'] as String,
      score: (json['score'] as num?)?.toDouble() ?? 0,
      totalMarks: (json['totalMarks'] as num?)?.toDouble() ?? 0,
      correct: json['correct'] as int? ?? 0,
      wrong: json['wrong'] as int? ?? 0,
      skipped: json['skipped'] as int? ?? 0,
      timeTakenSeconds: json['timeTakenSeconds'] as int? ?? 0,
      accuracyPercent: (json['accuracyPercent'] as num?)?.toDouble() ?? 0,
    );
  }
}

class PracticeReview {
  const PracticeReview({required this.result, required this.items});

  final PracticeResult result;
  final List<ReviewItem> items;

  factory PracticeReview.fromJson(Map<String, dynamic> json) {
    return PracticeReview(
      result: PracticeResult.fromJson(json['result'] as Map<String, dynamic>),
      items: (json['items'] as List<dynamic>? ?? const [])
          .map((item) => ReviewItem.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class ReviewItem {
  const ReviewItem({
    required this.order,
    required this.question,
    required this.selectedOptionIds,
    required this.correctOptionIds,
    required this.correct,
    this.simpleExplanation,
    this.detailedExplanation,
    this.examTip,
    this.steps = const [],
  });

  final int order;
  final StudentQuestion question;
  final List<String> selectedOptionIds;
  final List<String> correctOptionIds;
  final bool correct;
  final String? simpleExplanation;
  final String? detailedExplanation;
  final String? examTip;
  final List<ReviewStep> steps;

  factory ReviewItem.fromJson(Map<String, dynamic> json) {
    return ReviewItem(
      order: json['order'] as int,
      question: StudentQuestion.fromJson(json['question'] as Map<String, dynamic>),
      selectedOptionIds: (json['selectedOptionIds'] as List<dynamic>? ?? const []).cast<String>(),
      correctOptionIds: (json['correctOptionIds'] as List<dynamic>? ?? const []).cast<String>(),
      correct: json['correct'] as bool? ?? false,
      simpleExplanation: json['simpleExplanation'] as String?,
      detailedExplanation: json['detailedExplanation'] as String?,
      examTip: json['examTip'] as String?,
      steps: (json['steps'] as List<dynamic>? ?? const [])
          .map((item) => ReviewStep.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class ReviewStep {
  const ReviewStep({required this.order, this.title, required this.body});

  final int order;
  final String? title;
  final String body;

  factory ReviewStep.fromJson(Map<String, dynamic> json) {
    return ReviewStep(
      order: json['order'] as int,
      title: json['title'] as String?,
      body: json['body'] as String,
    );
  }
}
