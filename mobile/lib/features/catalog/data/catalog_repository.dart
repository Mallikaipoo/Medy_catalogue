import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:medycatalog/core/error/failure.dart';
import 'package:medycatalog/core/network/api_client.dart';

final catalogRepositoryProvider = Provider<CatalogRepository>((ref) {
  return CatalogRepository(ref.watch(dioProvider));
});

class CatalogRepository {
  CatalogRepository(this._dio);

  final Dio _dio;

  Future<HomeData> home() async {
    try {
      final response = await _dio.get<Map<String, dynamic>>('/home');
      return HomeData.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<List<SubjectCard>> subjects(String examId) async {
    try {
      final response = await _dio.get<List<dynamic>>('/exams/$examId/subjects');
      return (response.data ?? const [])
          .map((item) => SubjectCard.fromJson(item as Map<String, dynamic>))
          .toList();
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<List<ChapterCard>> chapters({required String examId, required String subjectId}) async {
    try {
      final response = await _dio.get<List<dynamic>>(
        '/subjects/$subjectId/chapters',
        queryParameters: {'examId': examId},
      );
      return (response.data ?? const [])
          .map((item) => ChapterCard.fromJson(item as Map<String, dynamic>))
          .toList();
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<List<TopicCard>> topics(String chapterId) async {
    try {
      final response = await _dio.get<List<dynamic>>('/chapters/$chapterId/topics');
      return (response.data ?? const [])
          .map((item) => TopicCard.fromJson(item as Map<String, dynamic>))
          .toList();
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<TopicNotes> topicNotes(String topicId) async {
    try {
      final response = await _dio.get<Map<String, dynamic>>('/topics/$topicId');
      return TopicNotes.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Object _wrap(DioException error) => error.error is Failure ? error.error as Failure : Failure.offline;
}

class HomeData {
  const HomeData({
    required this.studentName,
    required this.examId,
    required this.examCode,
    required this.examName,
    required this.subjects,
  });

  final String studentName;
  final String examId;
  final String examCode;
  final String examName;
  final List<SubjectCard> subjects;

  factory HomeData.fromJson(Map<String, dynamic> json) {
    return HomeData(
      studentName: json['studentName'] as String? ?? 'Student',
      examId: json['examId'] as String,
      examCode: json['examCode'] as String,
      examName: json['examName'] as String,
      subjects: (json['subjects'] as List<dynamic>? ?? const [])
          .map((item) => SubjectCard.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class SubjectCard {
  const SubjectCard({
    required this.id,
    required this.code,
    required this.name,
    required this.publishedQuestions,
  });

  final String id;
  final String code;
  final String name;
  final int publishedQuestions;

  factory SubjectCard.fromJson(Map<String, dynamic> json) {
    return SubjectCard(
      id: json['id'] as String,
      code: json['code'] as String,
      name: json['name'] as String,
      publishedQuestions: (json['publishedQuestions'] as num?)?.toInt() ?? 0,
    );
  }
}

class ChapterCard {
  const ChapterCard({
    required this.id,
    required this.name,
    required this.publishedQuestions,
  });

  final String id;
  final String name;
  final int publishedQuestions;

  factory ChapterCard.fromJson(Map<String, dynamic> json) {
    return ChapterCard(
      id: json['id'] as String,
      name: json['name'] as String,
      publishedQuestions: (json['publishedQuestions'] as num?)?.toInt() ?? 0,
    );
  }
}

class TopicCard {
  const TopicCard({required this.id, required this.name, required this.publishedQuestions, this.keyPoints});

  final String id;
  final String name;
  final int publishedQuestions;
  final String? keyPoints;

  factory TopicCard.fromJson(Map<String, dynamic> json) {
    return TopicCard(
      id: json['id'] as String,
      name: json['name'] as String,
      publishedQuestions: (json['publishedQuestions'] as num?)?.toInt() ?? 0,
      keyPoints: json['keyPoints'] as String?,
    );
  }
}

class TopicNotes {
  const TopicNotes({
    required this.id,
    required this.name,
    required this.syllabusYear,
    this.keyPoints,
    this.detailedExplanation,
    required this.publishedQuestions,
  });

  final String id;
  final String name;
  final int syllabusYear;
  final String? keyPoints;
  final String? detailedExplanation;
  final int publishedQuestions;

  factory TopicNotes.fromJson(Map<String, dynamic> json) {
    return TopicNotes(
      id: json['id'] as String,
      name: json['name'] as String,
      syllabusYear: (json['syllabusYear'] as num?)?.toInt() ?? 2025,
      keyPoints: json['keyPoints'] as String?,
      detailedExplanation: json['detailedExplanation'] as String?,
      publishedQuestions: (json['publishedQuestions'] as num?)?.toInt() ?? 0,
    );
  }
}
