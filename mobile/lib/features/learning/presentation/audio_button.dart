import 'package:flutter/material.dart';
import 'package:flutter_tts/flutter_tts.dart';

class AudioButton extends StatefulWidget {
  const AudioButton({super.key, required this.script, required this.ttsCode, this.label = 'Play audio'});

  final String script;
  final String ttsCode;
  final String label;

  @override
  State<AudioButton> createState() => _AudioButtonState();
}

class _AudioButtonState extends State<AudioButton> {
  final _tts = FlutterTts();
  var _playing = false;

  @override
  void dispose() {
    _tts.stop();
    super.dispose();
  }

  Future<void> _toggle() async {
    if (_playing) {
      await _tts.stop();
      setState(() => _playing = false);
      return;
    }
    if (widget.script.trim().isEmpty) {
      return;
    }
    setState(() => _playing = true);
    await _tts.setSpeechRate(0.42);
    await _tts.setPitch(1.0);
    await _tts.setLanguage(widget.ttsCode);
    _tts.setCompletionHandler(() {
      if (mounted) {
        setState(() => _playing = false);
      }
    });
    await _tts.speak(widget.script);
  }

  @override
  Widget build(BuildContext context) {
    return FilledButton.tonalIcon(
      onPressed: widget.script.trim().isEmpty ? null : _toggle,
      icon: Icon(_playing ? Icons.stop_circle_outlined : Icons.volume_up_outlined),
      label: Text(_playing ? 'Stop' : widget.label),
    );
  }
}
