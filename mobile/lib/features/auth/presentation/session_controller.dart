import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:medycatalog/features/auth/data/auth_repository.dart';
import 'package:medycatalog/features/auth/domain/user_profile.dart';

final sessionControllerProvider =
    AsyncNotifierProvider<SessionController, UserProfile?>(SessionController.new);

class SessionController extends AsyncNotifier<UserProfile?> {
  @override
  Future<UserProfile?> build() {
    return ref.read(authRepositoryProvider).restore();
  }

  Future<void> register(String name, String email, String password) async {
    state = await AsyncValue.guard(
      () => ref.read(authRepositoryProvider).register(name: name, email: email, password: password),
    );
  }

  Future<void> login(String email, String password) async {
    state = await AsyncValue.guard(
      () => ref.read(authRepositoryProvider).login(email: email, password: password),
    );
  }

  Future<void> guest() async {
    state = await AsyncValue.guard(() => ref.read(authRepositoryProvider).guest());
  }

  Future<void> selectExam(ExamOption exam) async {
    state = await AsyncValue.guard(
      () => ref.read(authRepositoryProvider).selectExams(examIds: [exam.id], primaryExamId: exam.id),
    );
  }

  Future<void> logout() async {
    await ref.read(authRepositoryProvider).logout();
    state = const AsyncData(null);
  }
}
