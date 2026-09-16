import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/core/error/failure.dart';
import 'package:medycatalog/features/learning/data/learning_repository.dart';
import 'package:medycatalog/features/learning/presentation/audio_button.dart';
import 'package:medycatalog/features/learning/presentation/locale_controller.dart';

class LibraryScreen extends ConsumerWidget {
  const LibraryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final locale = ref.watch(localeControllerProvider);
    return Scaffold(
      appBar: AppBar(title: const Text('Replay')),
      body: FutureBuilder<LearningLibrary>(
        future: ref.read(learningRepositoryProvider).library(locale.code),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            final message = snapshot.error is Failure
                ? (snapshot.error as Failure).message
                : snapshot.error.toString();
            return Center(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(message, textAlign: TextAlign.center),
                    const SizedBox(height: 12),
                    FilledButton(onPressed: () => context.push('/premium'), child: const Text('Go Premium')),
                  ],
                ),
              ),
            );
          }
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final library = snapshot.data!;
          if (library.answers.isEmpty && library.queries.isEmpty) {
            return const Center(child: Text('Your answers and spoken doubts will collect here.'));
          }
          return ListView(
            padding: const EdgeInsets.all(16),
            children: [
              Text('Your answers', style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 8),
              for (final answer in library.answers)
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(answer.correct ? 'Correct' : 'Review again',
                            style: TextStyle(
                              color: answer.correct
                                  ? Theme.of(context).colorScheme.primary
                                  : Theme.of(context).colorScheme.error,
                            )),
                        const SizedBox(height: 6),
                        Text(answer.questionText),
                        const SizedBox(height: 6),
                        Text(answer.explanation),
                        const SizedBox(height: 8),
                        AudioButton(script: answer.spokenScript, ttsCode: locale.ttsCode),
                      ],
                    ),
                  ),
                ),
              const SizedBox(height: 16),
              Text('Your doubts', style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 8),
              for (final query in library.queries)
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text('You asked: ${query.queryText}'),
                        const SizedBox(height: 6),
                        Text(query.answerShort),
                        if (query.focusLabel != null) Text(query.focusLabel!),
                        const SizedBox(height: 8),
                        AudioButton(script: query.spokenScript, ttsCode: query.ttsCode),
                      ],
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
