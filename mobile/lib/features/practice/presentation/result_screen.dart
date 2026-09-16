import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/features/billing/data/billing_repository.dart';
import 'package:medycatalog/features/billing/presentation/ad_banner_slot.dart';
import 'package:medycatalog/features/learning/presentation/audio_button.dart';
import 'package:medycatalog/features/learning/presentation/doubt_sheet.dart';
import 'package:medycatalog/features/learning/presentation/locale_controller.dart';
import 'package:medycatalog/features/practice/data/practice_repository.dart';

class ResultScreen extends ConsumerWidget {
  const ResultScreen({super.key, required this.sessionId});

  final String sessionId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return FutureBuilder<_ResultBundle>(
      future: _load(ref),
      builder: (context, snapshot) {
        if (snapshot.hasError) {
          return Scaffold(body: Center(child: Text(snapshot.error.toString())));
        }
        if (!snapshot.hasData) {
          return const Scaffold(body: Center(child: CircularProgressIndicator()));
        }
        final bundle = snapshot.data!;
        final result = bundle.result;
        return Scaffold(
          appBar: AppBar(title: const Text('Result')),
          body: ListView(
            padding: const EdgeInsets.all(24),
            children: [
              Text('Score ${result.score} / ${result.totalMarks}', style: Theme.of(context).textTheme.headlineSmall),
              const SizedBox(height: 8),
              Text('Accuracy ${result.accuracyPercent.toStringAsFixed(1)}%'),
              Text('Correct ${result.correct} · Wrong ${result.wrong} · Skipped ${result.skipped}'),
              Text('Time ${result.timeTakenSeconds}s'),
              const SizedBox(height: 24),
              FilledButton(
                onPressed: () => context.push('/practice/$sessionId/review'),
                child: const Text('Review answers'),
              ),
              const SizedBox(height: 8),
              OutlinedButton(
                onPressed: () => context.go('/home'),
                child: const Text('Continue learning'),
              ),
              AdBannerSlot(config: bundle.ads, placement: 'RESULT_INTERSTITIAL'),
            ],
          ),
        );
      },
    );
  }

  Future<_ResultBundle> _load(WidgetRef ref) async {
    final result = await ref.read(practiceRepositoryProvider).complete(sessionId);
    AdsConfig? ads;
    try {
      ads = await ref.read(billingRepositoryProvider).ads();
    } catch (_) {}
    return _ResultBundle(result, ads);
  }
}

class _ResultBundle {
  const _ResultBundle(this.result, this.ads);

  final PracticeResult result;
  final AdsConfig? ads;
}

class ReviewScreen extends ConsumerWidget {
  const ReviewScreen({super.key, required this.sessionId});

  final String sessionId;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return FutureBuilder<PracticeReview>(
      future: ref.read(practiceRepositoryProvider).review(sessionId),
      builder: (context, snapshot) {
        if (snapshot.hasError) {
          return Scaffold(body: Center(child: Text(snapshot.error.toString())));
        }
        if (!snapshot.hasData) {
          return const Scaffold(body: Center(child: CircularProgressIndicator()));
        }
        final review = snapshot.data!;
        return Scaffold(
          appBar: AppBar(title: const Text('Answer review')),
          body: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: review.items.length,
            itemBuilder: (context, index) {
              final item = review.items[index];
              return Card(
                margin: const EdgeInsets.only(bottom: 12),
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        item.correct ? 'Correct' : 'Wrong / skipped',
                        style: TextStyle(
                          color: item.correct
                              ? Theme.of(context).colorScheme.primary
                              : Theme.of(context).colorScheme.error,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(item.question.questionText, style: Theme.of(context).textTheme.titleMedium),
                      const SizedBox(height: 8),
                      for (final option in item.question.options)
                        Text(
                          '${String.fromCharCode(64 + option.order)}. ${option.text}'
                          '${item.correctOptionIds.contains(option.id) ? '  (correct)' : ''}'
                          '${item.selectedOptionIds.contains(option.id) ? '  (your answer)' : ''}',
                        ),
                      if (item.simpleExplanation != null) ...[
                        const SizedBox(height: 12),
                        Text('Short explanation', style: Theme.of(context).textTheme.titleSmall),
                        Text(item.simpleExplanation!),
                      ],
                      if (item.detailedExplanation != null) ...[
                        const SizedBox(height: 8),
                        Text('Detailed explanation', style: Theme.of(context).textTheme.titleSmall),
                        Text(item.detailedExplanation!),
                      ],
                      if (item.methodScript != null) ...[
                        const SizedBox(height: 8),
                        Text('Method: ${item.methodScript!}'),
                      ],
                      if (item.trapWording != null) ...[
                        const SizedBox(height: 8),
                        Text('Same idea, different sentence: ${item.trapWording!}'),
                      ],
                      if (item.examTip != null) ...[
                        const SizedBox(height: 8),
                        Text('Exam tip: ${item.examTip!}'),
                      ],
                      const SizedBox(height: 8),
                      AudioButton(
                        script: item.methodScript ?? item.detailedExplanation ?? item.simpleExplanation ?? '',
                        ttsCode: ref.watch(localeControllerProvider).ttsCode,
                      ),
                      TextButton(
                        onPressed: () => showDoubtSheet(
                          context,
                          ref: ref,
                          questionId: item.question.id,
                        ),
                        child: const Text('Ask a doubt about this'),
                      ),
                      for (final step in item.steps) ...[
                        const SizedBox(height: 6),
                        Text('Step ${step.order}${step.title == null ? '' : ': ${step.title}'}'),
                        Text(step.body),
                      ],
                    ],
                  ),
                ),
              );
            },
          ),
        );
      },
    );
  }
}
