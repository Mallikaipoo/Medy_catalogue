import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/features/catalog/data/catalog_repository.dart';
import 'package:medycatalog/features/learning/presentation/audio_button.dart';
import 'package:medycatalog/features/learning/presentation/doubt_sheet.dart';
import 'package:medycatalog/features/learning/presentation/locale_controller.dart';

class TopicsScreen extends ConsumerWidget {
  const TopicsScreen({
    super.key,
    required this.examId,
    required this.subjectId,
    required this.chapterId,
    required this.chapterName,
  });

  final String examId;
  final String subjectId;
  final String chapterId;
  final String chapterName;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: Text(chapterName)),
      body: FutureBuilder<List<TopicCard>>(
        future: ref.read(catalogRepositoryProvider).topics(chapterId),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            return Center(child: Text(snapshot.error.toString()));
          }
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final topics = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              FilledButton(
                onPressed: () => context.push(
                  '/practice/hub?examId=$examId&subjectId=$subjectId&chapterId=$chapterId',
                ),
                child: const Text('Practice this chapter'),
              ),
              const SizedBox(height: 12),
              for (final topic in topics)
                Card(
                  child: ListTile(
                    title: Text(topic.name),
                    subtitle: Text(topic.focusLabel ?? '${topic.publishedQuestions} questions · syllabus notes'),
                    onTap: () => context.push(
                      '/syllabus?examId=$examId&subjectId=$subjectId&chapterId=$chapterId&topicId=${topic.id}',
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

class SyllabusNotesScreen extends ConsumerWidget {
  const SyllabusNotesScreen({
    super.key,
    required this.examId,
    required this.subjectId,
    required this.chapterId,
    required this.topicId,
  });

  final String examId;
  final String subjectId;
  final String chapterId;
  final String topicId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(title: const Text('Syllabus notes')),
      body: FutureBuilder<TopicNotes>(
        future: ref.read(catalogRepositoryProvider).topicNotes(topicId),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            return Center(child: Text(snapshot.error.toString()));
          }
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final notes = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(20),
            children: [
              Text(notes.name, style: Theme.of(context).textTheme.headlineSmall),
              const SizedBox(height: 4),
              Text('Syllabus ${notes.syllabusYear} · ${notes.publishedQuestions} practice questions'),
              if (notes.focusLabel != null) ...[
                const SizedBox(height: 8),
                Text(notes.focusLabel!, style: Theme.of(context).textTheme.titleSmall),
              ],
              const SizedBox(height: 16),
              Text('Key points', style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 8),
              Text(notes.keyPoints ?? 'Key points will appear here.'),
              if (notes.patternNote != null) ...[
                const SizedBox(height: 16),
                Text('How the paper confuses you', style: Theme.of(context).textTheme.titleMedium),
                const SizedBox(height: 8),
                Text(notes.patternNote!),
              ],
              const SizedBox(height: 16),
              Text('Detailed explanation', style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 8),
              if (notes.studyMaterialLocked)
                Card(
                  child: ListTile(
                    title: const Text('Full notes are for Premium'),
                    subtitle: const Text('Subscribe to keep the exact syllabus explanation and audio for every topic.'),
                    onTap: () => context.push('/premium'),
                  ),
                )
              else
                Text(notes.detailedExplanation ?? 'A full explanation will appear here.'),
              if (notes.spokenScript != null && notes.spokenScript!.isNotEmpty) ...[
                const SizedBox(height: 12),
                AudioButton(
                  script: notes.spokenScript!,
                  ttsCode: ref.watch(localeControllerProvider).ttsCode,
                ),
              ],
              const SizedBox(height: 16),
              OutlinedButton.icon(
                onPressed: () => showDoubtSheet(
                  context,
                  ref: ref,
                  examId: examId,
                  topicId: topicId,
                ),
                icon: const Icon(Icons.record_voice_over_outlined),
                label: const Text('Ask a doubt'),
              ),
              const SizedBox(height: 24),
              FilledButton(
                onPressed: () => context.push(
                  '/practice/hub?examId=$examId&subjectId=$subjectId&chapterId=$chapterId',
                ),
                child: const Text('Practice this chapter'),
              ),
            ],
          );
        },
      ),
    );
  }
}
