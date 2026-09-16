import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/features/learning/data/learning_repository.dart';
import 'package:medycatalog/features/practice/data/practice_repository.dart';

class QuestionPlayerScreen extends ConsumerStatefulWidget {
  const QuestionPlayerScreen({super.key, required this.sessionId});

  final String sessionId;

  @override
  ConsumerState<QuestionPlayerScreen> createState() => _QuestionPlayerScreenState();
}

class _QuestionPlayerScreenState extends ConsumerState<QuestionPlayerScreen> {
  PracticeSession? _session;
  int _index = 0;
  String? _error;
  Timer? _timer;
  int _remaining = 0;
  var _finishing = false;
  final _numerical = TextEditingController();

  @override
  void initState() {
    super.initState();
    _load();
  }

  @override
  void dispose() {
    _timer?.cancel();
    _numerical.dispose();
    super.dispose();
  }

  Future<void> _load() async {
    try {
      final session = await ref.read(practiceRepositoryProvider).get(widget.sessionId);
      if (!mounted) {
        return;
      }
      if (session.status == 'COMPLETED') {
        context.go('/practice/${session.id}/result');
        return;
      }
      setState(() {
        _session = session;
        _remaining = session.remainingSeconds;
        _syncNumerical();
      });
      await ref.read(learningRepositoryProvider).saveResume(
            route: '/practice/${session.id}',
            title: 'Continue question ${_index + 1}',
          );
      _timer?.cancel();
      _timer = Timer.periodic(const Duration(seconds: 1), (_) {
        if (_remaining <= 1) {
          _finish();
        } else {
          setState(() => _remaining--);
        }
      });
    } catch (error) {
      setState(() => _error = error.toString());
    }
  }

  void _syncNumerical() {
    final item = _current;
    _numerical.text = item?.numericalAnswer ?? '';
  }

  SessionItem? get _current {
    final session = _session;
    if (session == null || session.items.isEmpty) {
      return null;
    }
    return session.items[_index.clamp(0, session.items.length - 1)];
  }

  Future<void> _save({bool clear = false}) async {
    final item = _current;
    if (item == null) {
      return;
    }
    final session = await ref.read(practiceRepositoryProvider).answer(
          sessionId: widget.sessionId,
          questionId: item.question.id,
          optionIds: item.selectedOptionIds,
          numericalAnswer: item.question.requiresNumerical ? _numerical.text : null,
          markedForReview: item.markedForReview,
          clear: clear,
        );
    setState(() => _session = session);
  }

  Future<void> _toggleOption(String optionId) async {
    final item = _current;
    if (item == null) {
      return;
    }
    final selected = [...item.selectedOptionIds];
    if (item.question.allowsMultipleOptions) {
      if (selected.contains(optionId)) {
        selected.remove(optionId);
      } else {
        selected.add(optionId);
      }
    } else {
      selected
        ..clear()
        ..add(optionId);
    }
    final session = _session!;
    final items = [...session.items];
    items[_index] = SessionItem(
      order: item.order,
      question: item.question,
      selectedOptionIds: selected,
      numericalAnswer: item.numericalAnswer,
      markedForReview: item.markedForReview,
    );
    setState(() => _session = PracticeSession(
          id: session.id,
          status: session.status,
          remainingSeconds: _remaining,
          items: items,
        ));
    await _save();
  }

  Future<void> _finish() async {
    if (_finishing) {
      return;
    }
    _finishing = true;
    _timer?.cancel();
    await _save();
    final result = await ref.read(practiceRepositoryProvider).complete(widget.sessionId);
    if (!mounted) {
      return;
    }
    context.go('/practice/${result.sessionId}/result');
  }

  @override
  Widget build(BuildContext context) {
    final item = _current;
    if (_error != null) {
      return Scaffold(body: Center(child: Text(_error!)));
    }
    if (item == null) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }
    final total = _session!.items.length;
    final minutes = (_remaining ~/ 60).toString().padLeft(2, '0');
    final seconds = (_remaining % 60).toString().padLeft(2, '0');
    return Scaffold(
      appBar: AppBar(
        title: Text('Question ${item.order} / $total'),
        actions: [
          Center(child: Padding(padding: const EdgeInsets.only(right: 16), child: Text('$minutes:$seconds'))),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(item.question.difficulty, style: Theme.of(context).textTheme.labelLarge),
          const SizedBox(height: 8),
          Text(item.question.questionText, style: Theme.of(context).textTheme.titleLarge),
          const SizedBox(height: 20),
          if (item.question.requiresNumerical)
            TextField(
              controller: _numerical,
              keyboardType: const TextInputType.numberWithOptions(decimal: true, signed: true),
              decoration: const InputDecoration(labelText: 'Your answer'),
              onEditingComplete: _save,
            )
          else
            for (final option in item.question.options)
              Padding(
                padding: const EdgeInsets.only(bottom: 8),
                child: OutlinedButton(
                  style: OutlinedButton.styleFrom(
                    alignment: Alignment.centerLeft,
                    backgroundColor: item.selectedOptionIds.contains(option.id)
                        ? Theme.of(context).colorScheme.primaryContainer
                        : null,
                  ),
                  onPressed: () => _toggleOption(option.id),
                  child: Text('${String.fromCharCode(64 + option.order)}. ${option.text}'),
                ),
              ),
        ],
      ),
      bottomNavigationBar: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(12),
          child: Row(
            children: [
              TextButton(
                onPressed: _index == 0
                    ? null
                    : () {
                        setState(() {
                          _index--;
                          _syncNumerical();
                        });
                      },
                child: const Text('Previous'),
              ),
              TextButton(
                onPressed: () async {
                  await _save(clear: true);
                },
                child: const Text('Clear'),
              ),
              const Spacer(),
              if (_index < total - 1)
                FilledButton(
                  onPressed: () async {
                    await _save();
                    setState(() {
                      _index++;
                      _syncNumerical();
                    });
                  },
                  child: const Text('Next'),
                )
              else
                FilledButton(onPressed: _finish, child: const Text('Submit')),
            ],
          ),
        ),
      ),
    );
  }
}
