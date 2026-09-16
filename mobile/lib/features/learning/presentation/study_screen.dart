import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/features/auth/domain/user_profile.dart';
import 'package:medycatalog/features/auth/presentation/session_controller.dart';
import 'package:medycatalog/features/billing/data/billing_repository.dart';
import 'package:medycatalog/features/catalog/data/catalog_repository.dart';
import 'package:medycatalog/features/learning/data/learning_repository.dart';
import 'package:medycatalog/features/learning/presentation/audio_button.dart';
import 'package:medycatalog/features/learning/presentation/doubt_sheet.dart';
import 'package:medycatalog/features/learning/presentation/locale_controller.dart';

class StudyScreen extends ConsumerWidget {
  const StudyScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final exam = _primaryExam(ref);
    if (exam == null) {
      return const Scaffold(body: Center(child: Text('Choose an exam first.')));
    }
    return Scaffold(
      appBar: AppBar(title: Text('${exam.code} syllabus')),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => showDoubtSheet(context, ref: ref, examId: exam.id),
        icon: const Icon(Icons.record_voice_over_outlined),
        label: const Text('Ask'),
      ),
      body: FutureBuilder<_StudyBundle>(
        future: _load(ref, exam.id),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            return Center(child: Text(snapshot.error.toString()));
          }
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final bundle = snapshot.data!;
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              if (bundle.preview != null) _FreePreviewCard(preview: bundle.preview!),
              if (bundle.me != null && !bundle.me!.entitlement.premium)
                Card(
                  child: ListTile(
                    title: const Text('Full syllabus notes are for Premium'),
                    subtitle: const Text('Key points stay visible. Detailed notes and the full bank unlock after subscribe.'),
                    trailing: const Icon(Icons.lock_outline),
                    onTap: () => context.push('/premium'),
                  ),
                ),
              for (final chapter in bundle.chapters)
                Card(
                  child: ExpansionTile(
                    title: Text(chapter.name),
                    subtitle: Text(chapter.subjectName),
                    children: [
                      for (final topic in chapter.topics)
                        ListTile(
                          title: Text(topic.name),
                          subtitle: Text(topic.focusLabel),
                          onTap: () => context.push(
                            '/syllabus?examId=${exam.id}&subjectId=&chapterId=${chapter.id}&topicId=${topic.id}',
                          ),
                        ),
                    ],
                  ),
                ),
            ],
          );
        },
      ),
    );
  }

  Future<_StudyBundle> _load(WidgetRef ref, String examId) async {
    final locale = ref.read(localeControllerProvider).code;
    final syllabus = await ref.read(catalogRepositoryProvider).syllabus(examId);
    SubscriptionMe? me;
    FreePreview? preview;
    try {
      me = await ref.read(billingRepositoryProvider).me();
    } catch (_) {}
    try {
      preview = await ref.read(learningRepositoryProvider).freePreview(examId: examId, locale: locale);
    } catch (_) {}
    return _StudyBundle(syllabus.chapters, me, preview);
  }
}

ExamOption? _primaryExam(WidgetRef ref) {
  final user = ref.watch(sessionControllerProvider).valueOrNull;
  if (user == null || user.exams.isEmpty) {
    return null;
  }
  return user.exams.where((item) => item.primary).isEmpty ? user.exams.first : user.exams.where((item) => item.primary).first;
}

class _StudyBundle {
  const _StudyBundle(this.chapters, this.me, this.preview);

  final List<SyllabusChapter> chapters;
  final SubscriptionMe? me;
  final FreePreview? preview;
}

class _FreePreviewCard extends StatelessWidget {
  const _FreePreviewCard({required this.preview});

  final FreePreview preview;

  @override
  Widget build(BuildContext context) {
    return Card(
      color: Theme.of(context).colorScheme.primaryContainer,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Free last-year question (${preview.examYear})', style: Theme.of(context).textTheme.titleMedium),
            const SizedBox(height: 8),
            Text(preview.questionText),
            const SizedBox(height: 8),
            for (final option in preview.options)
              Text('${String.fromCharCode(64 + option.order)}. ${option.text}${option.correct ? '  ✓' : ''}'),
            if (preview.simpleExplanation != null) ...[
              const SizedBox(height: 8),
              Text(preview.simpleExplanation!),
            ],
            if (preview.detailedExplanation != null) ...[
              const SizedBox(height: 8),
              Text(preview.detailedExplanation!),
            ],
            if (preview.trapWording != null) ...[
              const SizedBox(height: 8),
              Text('How papers confuse you: ${preview.trapWording!}'),
            ],
            for (final variant in preview.variants) ...[
              const SizedBox(height: 6),
              Text('Same idea, different sentence: ${variant.variantText}'),
              Text(variant.trapNote, style: Theme.of(context).textTheme.bodySmall),
            ],
            const SizedBox(height: 8),
            Text(preview.focusLabel, style: Theme.of(context).textTheme.titleSmall),
            const SizedBox(height: 8),
            AudioButton(script: preview.spokenScript, ttsCode: preview.ttsCode),
            TextButton(
              onPressed: () => context.push('/premium'),
              child: const Text('Continue the full question bank with Premium'),
            ),
          ],
        ),
      ),
    );
  }
}
