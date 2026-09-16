import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:medycatalog/features/auth/data/auth_repository.dart';
import 'package:medycatalog/features/auth/domain/user_profile.dart';
import 'package:medycatalog/features/auth/presentation/session_controller.dart';

class ExamPickerScreen extends ConsumerWidget {
  const ExamPickerScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('Choose your examination')),
      body: FutureBuilder<List<ExamOption>>(
        future: ref.read(authRepositoryProvider).exams(),
        builder: (context, snapshot) {
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final exams = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              const Text('You can add more examinations later from your profile.'),
              const SizedBox(height: 12),
              for (final exam in exams)
                Card(
                  child: ListTile(
                    title: Text(exam.code),
                    subtitle: Text(exam.name),
                    onTap: () => ref.read(sessionControllerProvider.notifier).selectExam(exam),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }
}
