import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:medycatalog/core/error/failure.dart';
import 'package:medycatalog/core/network/api_client.dart';
import 'package:medycatalog/core/network/token_store.dart';
import 'package:medycatalog/features/auth/domain/user_profile.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository(ref.watch(dioProvider), ref.watch(tokenStoreProvider));
});

class AuthRepository {
  AuthRepository(this._dio, this._tokens);

  final Dio _dio;
  final TokenStore _tokens;

  Future<UserProfile?> restore() async {
    final access = await _tokens.readAccess();
    if (access == null) {
      return null;
    }
    try {
      final response = await _dio.get<Map<String, dynamic>>('/me');
      return UserProfile.fromJson(response.data!);
    } catch (_) {
      await _tokens.clear();
      return null;
    }
  }

  Future<UserProfile> register({
    required String name,
    required String email,
    required String password,
  }) {
    return _auth('/auth/register', {'name': name, 'email': email, 'password': password});
  }

  Future<UserProfile> login({required String email, required String password}) {
    return _auth('/auth/login', {'email': email, 'password': password});
  }

  Future<UserProfile> guest() => _auth('/auth/guest', {});

  Future<UserProfile> google(String idToken) => _auth('/auth/google', {'idToken': idToken});

  Future<UserProfile> apple(String idToken) => _auth('/auth/apple', {'idToken': idToken});

  Future<List<ExamOption>> exams() async {
    final response = await _dio.get<List<dynamic>>('/exams');
    return (response.data ?? const [])
        .map((item) => ExamOption.fromJson(item as Map<String, dynamic>))
        .toList();
  }

  Future<UserProfile> selectExams({required List<String> examIds, required String primaryExamId}) async {
    final response = await _dio.patch<Map<String, dynamic>>(
      '/me/exams',
      data: {'examIds': examIds, 'primaryExamId': primaryExamId},
    );
    return UserProfile.fromJson(response.data!);
  }

  Future<UserProfile> updateProfile({required String name}) async {
    final response = await _dio.patch<Map<String, dynamic>>('/me', data: {'name': name});
    return UserProfile.fromJson(response.data!);
  }

  Future<void> logout() async {
    final refresh = await _tokens.readRefresh();
    try {
      await _dio.post<void>('/auth/logout', data: {'refreshToken': refresh});
    } catch (_) {
      // Still clear local tokens.
    }
    await _tokens.clear();
  }

  Future<UserProfile> _auth(String path, Map<String, dynamic> body) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(path, data: body);
      final data = response.data!;
      await _tokens.save(
        accessToken: data['accessToken'] as String,
        refreshToken: data['refreshToken'] as String,
      );
      return UserProfile.fromJson(data['user'] as Map<String, dynamic>);
    } on DioException catch (error) {
      final failure = error.error is Failure ? error.error as Failure : Failure.offline;
      throw failure;
    }
  }
}
