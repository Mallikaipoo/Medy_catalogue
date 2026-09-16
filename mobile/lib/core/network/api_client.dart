import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:medycatalog/core/config/app_config.dart';
import 'package:medycatalog/core/error/failure.dart';
import 'package:medycatalog/core/network/token_store.dart';

final tokenStoreProvider = Provider<TokenStore>((ref) => TokenStore(const FlutterSecureStorage()));

final dioProvider = Provider<Dio>((ref) {
  final tokenStore = ref.watch(tokenStoreProvider);
  final dio = Dio(
    BaseOptions(
      baseUrl: '${AppConfig.apiBaseUrl}/api/v1',
      connectTimeout: const Duration(seconds: 15),
      receiveTimeout: const Duration(seconds: 20),
      headers: {'Content-Type': 'application/json'},
    ),
  );
  dio.interceptors.add(
    InterceptorsWrapper(
      onRequest: (options, handler) async {
        final access = await tokenStore.readAccess();
        if (access != null) {
          options.headers['Authorization'] = 'Bearer $access';
        }
        handler.next(options);
      },
      onError: (error, handler) async {
        if (error.type == DioExceptionType.connectionError ||
            error.type == DioExceptionType.connectionTimeout) {
          handler.reject(
            DioException(
              requestOptions: error.requestOptions,
              error: Failure.offline,
              type: error.type,
            ),
          );
          return;
        }
        if (error.response?.statusCode == 401 && !error.requestOptions.path.contains('/auth/refresh')) {
          final refreshed = await _refresh(dio, tokenStore);
          if (refreshed) {
            final retry = await dio.fetch(error.requestOptions);
            handler.resolve(retry);
            return;
          }
        }
        final data = error.response?.data;
        String? detail;
        if (data is Map && data['detail'] is String) {
          detail = data['detail'] as String;
        }
        handler.reject(
          DioException(
            requestOptions: error.requestOptions,
            response: error.response,
            error: Failure.fromStatus(error.response?.statusCode, detail),
            type: error.type,
          ),
        );
      },
    ),
  );
  return dio;
});

Future<bool> _refresh(Dio dio, TokenStore store) async {
  final refresh = await store.readRefresh();
  if (refresh == null) {
    return false;
  }
  try {
    final response = await dio.post<Map<String, dynamic>>(
      '/auth/refresh',
      data: {'refreshToken': refresh},
    );
    final access = response.data?['accessToken'] as String?;
    final nextRefresh = response.data?['refreshToken'] as String?;
    if (access == null || nextRefresh == null) {
      return false;
    }
    await store.save(accessToken: access, refreshToken: nextRefresh);
    return true;
  } catch (_) {
    await store.clear();
    return false;
  }
}
