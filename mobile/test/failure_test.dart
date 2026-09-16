import 'package:flutter_test/flutter_test.dart';
import 'package:medycatalog/core/error/failure.dart';

void main() {
  test('maps connectivity errors to a friendly message', () {
    expect(Failure.offline.message, contains('internet connection'));
  });

  test('does not expose raw server status in 5xx mapping', () {
    expect(Failure.fromStatus(500).message, isNot(contains('NullPointer')));
    expect(Failure.fromStatus(500).message, contains('try again'));
  });
}
