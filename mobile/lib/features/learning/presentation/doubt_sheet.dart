import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:medycatalog/core/error/failure.dart';
import 'package:medycatalog/features/learning/data/learning_repository.dart';
import 'package:medycatalog/features/learning/presentation/audio_button.dart';
import 'package:medycatalog/features/learning/presentation/locale_controller.dart';
import 'package:speech_to_text/speech_to_text.dart';

Future<void> showDoubtSheet(
  BuildContext context, {
  required WidgetRef ref,
  String? examId,
  String? topicId,
  String? questionId,
}) {
  return showModalBottomSheet<void>(
    context: context,
    isScrollControlled: true,
    builder: (context) => DoubtSheet(
      examId: examId,
      topicId: topicId,
      questionId: questionId,
    ),
  );
}

class DoubtSheet extends ConsumerStatefulWidget {
  const DoubtSheet({super.key, this.examId, this.topicId, this.questionId});

  final String? examId;
  final String? topicId;
  final String? questionId;

  @override
  ConsumerState<DoubtSheet> createState() => _DoubtSheetState();
}

class _DoubtSheetState extends ConsumerState<DoubtSheet> {
  final _text = TextEditingController();
  final _speech = SpeechToText();
  DoubtAnswer? _answer;
  String? _error;
  var _busy = false;
  var _listening = false;

  @override
  void dispose() {
    _text.dispose();
    _speech.stop();
    super.dispose();
  }

  Future<void> _listen() async {
    final available = await _speech.initialize();
    if (!available) {
      setState(() => _error = 'Microphone is not available on this phone.');
      return;
    }
    setState(() {
      _listening = true;
      _error = null;
    });
    await _speech.listen(
      onResult: (result) {
        _text.text = result.recognizedWords;
      },
    );
  }

  Future<void> _stopListen() async {
    await _speech.stop();
    setState(() => _listening = false);
  }

  Future<void> _ask({required String source}) async {
    if (_text.text.trim().isEmpty) {
      return;
    }
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      final locale = ref.read(localeControllerProvider);
      final answer = await ref.read(learningRepositoryProvider).askDoubt(
            text: _text.text.trim(),
            locale: locale.code,
            source: source,
            examId: widget.examId,
            topicId: widget.topicId,
            questionId: widget.questionId,
          );
      setState(() => _answer = answer);
    } catch (error) {
      setState(() => _error = error is Failure ? error.message : error.toString());
    } finally {
      if (mounted) {
        setState(() => _busy = false);
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final locale = ref.watch(localeControllerProvider);
    return Padding(
      padding: EdgeInsets.only(
        left: 20,
        right: 20,
        top: 20,
        bottom: 20 + MediaQuery.viewInsetsOf(context).bottom,
      ),
      child: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('Ask a doubt', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 4),
            Text('Type or speak. You get a short written answer plus audio in ${locale.nativeName}.'),
            const SizedBox(height: 12),
            TextField(
              controller: _text,
              minLines: 2,
              maxLines: 4,
              decoration: const InputDecoration(
                border: OutlineInputBorder(),
                hintText: 'Example: why do like charges repel?',
              ),
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                IconButton.filledTonal(
                  onPressed: _listening ? _stopListen : _listen,
                  icon: Icon(_listening ? Icons.stop : Icons.mic_none),
                ),
                const SizedBox(width: 8),
                FilledButton(
                  onPressed: _busy ? null : () => _ask(source: _listening ? 'VOICE' : 'TEXT'),
                  child: Text(_busy ? 'Thinking…' : 'Explain'),
                ),
              ],
            ),
            if (_error != null) ...[
              const SizedBox(height: 8),
              Text(_error!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
            ],
            if (_answer != null) ...[
              const SizedBox(height: 16),
              Text(_answer!.answerShort),
              if (_answer!.focusLabel != null) Text(_answer!.focusLabel!),
              const SizedBox(height: 8),
              AudioButton(script: _answer!.spokenScript, ttsCode: _answer!.ttsCode),
            ],
          ],
        ),
      ),
    );
  }
}
