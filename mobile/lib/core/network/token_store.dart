import 'package:flutter_secure_storage/flutter_secure_storage.dart';

class TokenStore {
  TokenStore(this._storage);

  final FlutterSecureStorage _storage;
  static const _access = 'access_token';
  static const _refresh = 'refresh_token';

  Future<void> save({required String accessToken, required String refreshToken}) async {
    await _storage.write(key: _access, value: accessToken);
    await _storage.write(key: _refresh, value: refreshToken);
  }

  Future<String?> readAccess() => _storage.read(key: _access);

  Future<String?> readRefresh() => _storage.read(key: _refresh);

  Future<void> clear() async {
    await _storage.delete(key: _access);
    await _storage.delete(key: _refresh);
  }
}
