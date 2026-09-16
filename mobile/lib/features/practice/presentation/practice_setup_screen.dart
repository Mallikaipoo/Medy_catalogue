import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/features/auth/presentation/session_controller.dart';
import 'package:medycatalog/features/billing/data/billing_repository.dart';
import 'package:medycatalog/features/learning/data/learning_repository.dart';
import 'package:medycatalog/features/practice/data/practice_repository.dart';

class PracticeSetupScreen extends ConsumerStatefulWidget {
  const PracticeSetupScreen({
    super.key,
    required this.examId,
    this.subjectId,
    this.chapterId,
  });

  final String examId;
  final String? subjectId;
  final String? chapterId;

  @override
  ConsumerState<PracticeSetupScreen> createState() => _PracticeSetupScreenState();
}

class _PracticeSetupScreenState extends ConsumerState<PracticeSetupScreen> {
  int _count = 1;
  var _busy = false;
  String? _error;
  Entitlement? _entitlement;

  String get _examId {
    if (widget.examId.isNotEmpty) {
      return widget.examId;
    }
    final exams = ref.read(sessionControllerProvider).valueOrNull?.exams ?? const [];
    if (exams.isEmpty) {
      return '';
    }
    return exams.where((item) => item.primary).isEmpty ? exams.first.id : exams.where((item) => item.primary).first.id;
  }

  @override
  void initState() {
    super.initState();
    _loadBilling();
  }

  Future<void> _loadBilling() async {
    try {
      final me = await ref.read(billingRepositoryProvider).me();
      if (!mounted) {
        return;
      }
      setState(() {
        _entitlement = me.entitlement;
        _count = me.entitlement.fullQuestionBank ? 5 : 1;
      });
    } catch (_) {}
  }

  Future<void> _watchAd() async {
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      final me = await ref.read(billingRepositoryProvider).rewarded();
      if (!mounted) {
        return;
      }
      setState(() {
        _entitlement = me.entitlement;
        _busy = false;
      });
    } catch (error) {
      setState(() {
        _busy = false;
        _error = error.toString();
      });
    }
  }

  Future<void> _start() async {
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      final session = await ref.read(practiceRepositoryProvider).start(
            examId: _examId,
            subjectId: widget.subjectId,
            chapterId: widget.chapterId,
            questionCount: _entitlement?.fullQuestionBank == true ? _count : 1,
          );
      if (!mounted) {
        return;
      }
      await ref.read(learningRepositoryProvider).saveResume(
            route: '/practice/${session.id}',
            title: 'Continue practice',
            examId: _examId,
          );
      if (!mounted) {
        return;
      }
      context.go('/practice/${session.id}');
    } catch (error) {
      setState(() {
        _busy = false;
        _error = error.toString();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final free = _entitlement != null && !_entitlement!.fullQuestionBank;
    return Scaffold(
      appBar: AppBar(title: const Text('Practice')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(free
              ? 'Free includes one last-year question with answer, explanation, and audio.'
              : 'How many questions?'),
          if (!free) ...[
            const SizedBox(height: 12),
            Wrap(
              spacing: 8,
              children: [
                for (final count in [5, 10, 20])
                  ChoiceChip(
                    label: Text('$count'),
                    selected: _count == count,
                    onSelected: (_) => setState(() => _count = count),
                  ),
              ],
            ),
          ],
          if (_entitlement != null) ...[
            const SizedBox(height: 16),
            Text(
              _entitlement!.unlimitedPractice
                  ? 'Premium · full question bank'
                  : '${_entitlement!.practiceRemainingToday} starts left today',
            ),
          ],
          if (_error != null) ...[
            const SizedBox(height: 16),
            Text(_error!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
          ],
          const SizedBox(height: 24),
          FilledButton(
            onPressed: _busy || _examId.isEmpty ? null : _start,
            child: Text(_busy ? 'Starting…' : free ? 'Try the free last-year question' : 'Start practice'),
          ),
          if (free) ...[
            const SizedBox(height: 8),
            TextButton(
              onPressed: () => context.push('/premium'),
              child: const Text('Unlock the full bank after this question'),
            ),
          ],
          if (_entitlement != null && !_entitlement!.unlimitedPractice) ...[
            const SizedBox(height: 8),
            OutlinedButton(
              onPressed: _busy || !_entitlement!.rewardedExtraAttempts ? null : _watchAd,
              child: const Text('Watch ad for one extra start'),
            ),
          ],
        ],
      ),
    );
  }
}
