import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/features/billing/data/billing_repository.dart';
import 'package:medycatalog/features/billing/presentation/ad_banner_slot.dart';
import 'package:medycatalog/features/catalog/data/catalog_repository.dart';
import 'package:medycatalog/features/learning/data/learning_repository.dart';

class HomeScreen extends ConsumerWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('MedyCatalog'),
        actions: [
          IconButton(
            tooltip: 'Premium',
            onPressed: () => context.push('/premium'),
            icon: const Icon(Icons.workspace_premium_outlined),
          ),
        ],
      ),
      body: FutureBuilder<_HomeBundle>(
        future: _load(ref),
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            return Center(child: Text(snapshot.error.toString()));
          }
          if (!snapshot.hasData) {
            return const Center(child: CircularProgressIndicator());
          }
          final bundle = snapshot.data!;
          final home = bundle.home;
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
              if (bundle.me != null) ...[
                const SizedBox(height: 8),
                Text(
                  bundle.me!.entitlement.premium
                      ? 'Premium · unlimited practice, ads off'
                      : '${bundle.me!.entitlement.practiceRemainingToday} practice starts left today',
                ),
              ],
              const SizedBox(height: 20),
              Text('Continue learning', style: Theme.of(context).textTheme.titleMedium),
              const SizedBox(height: 8),
              if (bundle.resume != null)
                Card(
                  color: Theme.of(context).colorScheme.secondaryContainer,
                  child: ListTile(
                    leading: const Icon(Icons.play_circle_outline),
                    title: const Text('Pick up where you left'),
                    subtitle: Text(bundle.resume!.title),
                    onTap: () => context.go(bundle.resume!.route),
                  ),
                ),
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
                    : () => context.go('/practice/hub?examId=${home.examId}'),
                child: const Text('Start practice'),
              ),
              if (bundle.me != null && !bundle.me!.entitlement.premium)
                TextButton(
                  onPressed: () => context.push('/premium'),
                  child: const Text('Go Premium — yearly ₹1,499'),
                ),
              AdBannerSlot(config: bundle.ads, placement: 'HOME_BANNER'),
            ],
          );
        },
      ),
    );
  }

  Future<_HomeBundle> _load(WidgetRef ref) async {
    final home = await ref.read(catalogRepositoryProvider).home();
    SubscriptionMe? me;
    AdsConfig? ads;
    try {
      me = await ref.read(billingRepositoryProvider).me();
    } catch (_) {}
    try {
      ads = await ref.read(billingRepositoryProvider).ads();
    } catch (_) {}
    ResumeState? resume;
    try {
      resume = await ref.read(learningRepositoryProvider).resume();
    } catch (_) {}
    return _HomeBundle(home, me, ads, resume);
  }
}

class _HomeBundle {
  const _HomeBundle(this.home, this.me, this.ads, this.resume);

  final HomeData home;
  final SubscriptionMe? me;
  final AdsConfig? ads;
  final ResumeState? resume;
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
                onPressed: () => context.go('/practice/hub?examId=$examId&subjectId=$subjectId'),
                child: const Text('Practice this subject'),
              ),
              const SizedBox(height: 12),
              for (final chapter in chapters)
                Card(
                  child: ListTile(
                    title: Text(chapter.name),
                    subtitle: Text('${chapter.publishedQuestions} questions'),
                    onTap: () => context.push(
                      '/topics?examId=$examId&subjectId=$subjectId&chapterId=${chapter.id}&chapterName=${Uri.encodeComponent(chapter.name)}',
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
