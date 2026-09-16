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

  Future<ExamSyllabus> syllabus(String examId) async {
    try {
      final response = await _dio.get<Map<String, dynamic>>('/exams/$examId/syllabus');
      return ExamSyllabus.fromJson(response.data!);
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
  const TopicCard({
    required this.id,
    required this.name,
    required this.publishedQuestions,
    this.keyPoints,
    this.focusLabel,
  });

  final String id;
  final String name;
  final int publishedQuestions;
  final String? keyPoints;
  final String? focusLabel;

  factory TopicCard.fromJson(Map<String, dynamic> json) {
    return TopicCard(
      id: json['id'] as String,
      name: json['name'] as String,
      publishedQuestions: (json['publishedQuestions'] as num?)?.toInt() ?? 0,
      keyPoints: json['keyPoints'] as String?,
      focusLabel: json['focusLabel'] as String?,
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
    this.focusLabel,
    this.patternNote,
    this.spokenScript,
    this.studyMaterialLocked = false,
  });

  final String id;
  final String name;
  final int syllabusYear;
  final String? keyPoints;
  final String? detailedExplanation;
  final int publishedQuestions;
  final String? focusLabel;
  final String? patternNote;
  final String? spokenScript;
  final bool studyMaterialLocked;

  factory TopicNotes.fromJson(Map<String, dynamic> json) {
    return TopicNotes(
      id: json['id'] as String,
      name: json['name'] as String,
      syllabusYear: (json['syllabusYear'] as num?)?.toInt() ?? 2025,
      keyPoints: json['keyPoints'] as String?,
      detailedExplanation: json['detailedExplanation'] as String?,
      publishedQuestions: (json['publishedQuestions'] as num?)?.toInt() ?? 0,
      focusLabel: json['focusLabel'] as String?,
      patternNote: json['patternNote'] as String?,
      spokenScript: json['spokenScript'] as String?,
      studyMaterialLocked: json['studyMaterialLocked'] as bool? ?? false,
    );
  }
}

class ExamSyllabus {
  const ExamSyllabus({required this.chapters});

  final List<SyllabusChapter> chapters;

  factory ExamSyllabus.fromJson(Map<String, dynamic> json) {
    return ExamSyllabus(
      chapters: (json['chapters'] as List<dynamic>? ?? const [])
          .map((item) => SyllabusChapter.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class SyllabusChapter {
  const SyllabusChapter({
    required this.id,
    required this.name,
    required this.subjectName,
    required this.topics,
  });

  final String id;
  final String name;
  final String subjectName;
  final List<SyllabusTopic> topics;

  factory SyllabusChapter.fromJson(Map<String, dynamic> json) {
    return SyllabusChapter(
      id: json['id'] as String,
      name: json['name'] as String,
      subjectName: json['subjectName'] as String? ?? '',
      topics: (json['topics'] as List<dynamic>? ?? const [])
          .map((item) => SyllabusTopic.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class SyllabusTopic {
  const SyllabusTopic({required this.id, required this.name, required this.focusLabel});

  final String id;
  final String name;
  final String focusLabel;

  factory SyllabusTopic.fromJson(Map<String, dynamic> json) {
    return SyllabusTopic(
      id: json['id'] as String,
      name: json['name'] as String,
      focusLabel: json['focusLabel'] as String? ?? '',
    );
  }
}
