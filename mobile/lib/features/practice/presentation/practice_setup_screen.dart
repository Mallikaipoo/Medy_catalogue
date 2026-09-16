import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/features/billing/data/billing_repository.dart';
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
  int _count = 5;
  var _busy = false;
  String? _error;
  Entitlement? _entitlement;

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
      setState(() => _entitlement = me.entitlement);
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
            examId: widget.examId,
            subjectId: widget.subjectId,
            chapterId: widget.chapterId,
            questionCount: _count,
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
    return Scaffold(
      appBar: AppBar(title: const Text('Practice setup')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          const Text('How many questions?'),
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
          if (_entitlement != null) ...[
            const SizedBox(height: 16),
            Text(
              _entitlement!.unlimitedPractice
                  ? 'Premium · start anytime'
                  : '${_entitlement!.practiceRemainingToday} starts left today',
            ),
          ],
          if (_error != null) ...[
            const SizedBox(height: 16),
            Text(_error!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
          ],
          const SizedBox(height: 24),
          FilledButton(
            onPressed: _busy ? null : _start,
            child: Text(_busy ? 'Starting…' : 'Start practice'),
          ),
          if (_entitlement != null && !_entitlement!.unlimitedPractice) ...[
            const SizedBox(height: 8),
            OutlinedButton(
              onPressed: _busy || !_entitlement!.rewardedExtraAttempts ? null : _watchAd,
              child: const Text('Watch ad for one extra start'),
            ),
            TextButton(
              onPressed: () => context.push('/premium'),
              child: const Text('Unlimited with yearly Premium'),
            ),
          ],
        ],
      ),
    );
  }
}
