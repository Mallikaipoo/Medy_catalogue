import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/features/auth/domain/user_profile.dart';
import 'package:medycatalog/features/auth/presentation/session_controller.dart';
import 'package:medycatalog/features/learning/data/learning_repository.dart';
import 'package:medycatalog/features/learning/presentation/locale_controller.dart';

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
          const SizedBox(height: 12),
          Card(
            child: ListTile(
              leading: const Icon(Icons.workspace_premium_outlined),
              title: const Text('Premium'),
              subtitle: const Text('Yearly ₹1,499 · full bank · syllabus notes · audio doubts'),
              trailing: const Icon(Icons.chevron_right),
              onTap: () => context.push('/premium'),
            ),
          ),
          const SizedBox(height: 12),
          const _LanguageCard(),
          const SizedBox(height: 24),
          const Text('Account, privacy, language, and examination selection.'),
        ],
      ),
    );
  }
}

class _LanguageCard extends ConsumerWidget {
  const _LanguageCard();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final selected = ref.watch(localeControllerProvider);
    return FutureBuilder<List<AppSpeechLocale>>(
      future: ref.read(learningRepositoryProvider).locales(),
      builder: (context, snapshot) {
        final locales = snapshot.data ?? const [AppSpeechLocale.english];
        return Card(
          child: ListTile(
            leading: const Icon(Icons.translate),
            title: const Text('Explanation language'),
            subtitle: Text('${selected.nativeName} · 11 classical Indian languages plus Hindi, English, Urdu'),
            trailing: const Icon(Icons.chevron_right),
            onTap: () async {
              final picked = await showModalBottomSheet<AppSpeechLocale>(
                context: context,
                builder: (context) => ListView(
                  children: [
                    for (final locale in locales)
                      ListTile(
                        title: Text(locale.nativeName),
                        subtitle: Text(locale.classical ? '${locale.englishName} · classical' : locale.englishName),
                        selected: locale.code == selected.code,
                        onTap: () => Navigator.pop(context, locale),
                      ),
                  ],
                ),
              );
              if (picked != null) {
                await ref.read(localeControllerProvider.notifier).select(picked);
              }
            },
          ),
        );
      },
    );
  }
}
