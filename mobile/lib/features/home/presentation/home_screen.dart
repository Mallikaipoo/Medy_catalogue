import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/features/catalog/data/catalog_repository.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('MedyCatalog'),
        actions: [
          IconButton(
            tooltip: 'Profile',
            onPressed: () => context.push('/profile'),
            icon: const Icon(Icons.person_outline),
          ),
        ],
      ),
      body: FutureBuilder<HomeData>(
        future: ref.read(catalogRepositoryProvider).home(),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            return Center(child: Text(snapshot.error.toString()));
          }
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final home = snapshot.data!;
          final hour = DateTime.now().hour;
          final greeting = hour < 12
              ? 'Good morning'
              : hour < 17
                  ? 'Good afternoon'
                  : 'Good evening';
          return ListView(
            padding: const EdgeInsets.all(20),
            children: [
              Text('$greeting, ${home.studentName}', style: Theme.of(context).textTheme.headlineSmall),
              const SizedBox(height: 4),
              Text('${home.examCode} · ${home.examName}'),
              const SizedBox(height: 20),
              Text('Continue learning', style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 8),
              for (final subject in home.subjects)
                Card(
                  child: ListTile(
                    title: Text(subject.name),
                    subtitle: Text('${subject.publishedQuestions} published questions'),
                    trailing: const Icon(Icons.chevron_right),
                    onTap: () => context.push(
                      '/chapters?examId=${home.examId}&subjectId=${subject.id}&subjectName=${Uri.encodeComponent(subject.name)}',
                    ),
                  ),
                ),
              const SizedBox(height: 16),
              FilledButton(
                onPressed: home.subjects.isEmpty
                    ? null
                    : () => context.push('/practice/setup?examId=${home.examId}'),
                child: const Text('Start practice'),
              ),
            ],
          );
        },
      ),
    );
  }
}

class ChaptersScreen extends ConsumerWidget {
  const ChaptersScreen({
    super.key,
    required this.examId,
    required this.subjectId,
    required this.subjectName,
  });

  final String examId;
  final String subjectId;
  final String subjectName;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: Text(subjectName)),
      body: FutureBuilder<List<ChapterCard>>(
        future: ref.read(catalogRepositoryProvider).chapters(examId: examId, subjectId: subjectId),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            return Center(child: Text(snapshot.error.toString()));
          }
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final chapters = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              FilledButton(
                onPressed: () => context.push('/practice/setup?examId=$examId&subjectId=$subjectId'),
                child: const Text('Practice this subject'),
              ),
              const SizedBox(height: 12),
              for (final chapter in chapters)
                Card(
                  child: ListTile(
                    title: Text(chapter.name),
                    subtitle: Text('${chapter.publishedQuestions} questions'),
                    onTap: () => context.push(
                      '/practice/setup?examId=$examId&subjectId=$subjectId&chapterId=${chapter.id}',
                    ),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }
}
