import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:medycatalog/features/auth/domain/user_profile.dart';
import 'package:medycatalog/features/auth/presentation/session_controller.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final session = ref.watch(sessionControllerProvider).valueOrNull;
    ExamOption? exam;
    if (session != null && session.exams.isNotEmpty) {
      exam = session.exams.where((item) => item.primary).isEmpty
          ? session.exams.first
          : session.exams.where((item) => item.primary).first;
    }
    return Scaffold(
      appBar: AppBar(
        title: const Text('MedyCatalog'),
        actions: [
          IconButton(
            onPressed: () => ref.read(sessionControllerProvider.notifier).logout(),
            icon: const Icon(Icons.logout),
            tooltip: 'Sign out',
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(24),
        children: [
          Text(
            'Hello, ${session?.name ?? 'Student'}',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 8),
          Text(session?.email ?? (session?.guest == true ? 'Guest account — upgrade anytime from settings.' : '')),
          const SizedBox(height: 16),
          if (exam != null)
            Card(
              child: ListTile(
                title: Text(exam.code),
                subtitle: Text(exam.name),
              ),
            ),
          const SizedBox(height: 24),
          const Text('Account, privacy, and examination selection. Practice lives on Home.'),
        ],
      ),
    );
  }
}
