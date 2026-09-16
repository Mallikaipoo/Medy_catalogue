import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/features/auth/presentation/session_controller.dart';

class LoginScreen extends ConsumerStatefulWidget {
  const LoginScreen({super.key});

  @override
  ConsumerState<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends ConsumerState<LoginScreen> {
  final _email = TextEditingController();
  final _password = TextEditingController();
  var _busy = false;
  String? _error;

  @override
  void dispose() {
    _email.dispose();
    _password.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    setState(() {
      _busy = true;
      _error = null;
    });
    await ref.read(sessionControllerProvider.notifier).login(_email.text.trim(), _password.text);
    final next = ref.read(sessionControllerProvider);
    setState(() {
      _busy = false;
      _error = next.hasError ? next.error.toString() : null;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: ListView(
          padding: const EdgeInsets.all(24),
          children: [
            const Text('MedyCatalog', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w600)),
            const SizedBox(height: 8),
            const Text('Sign in to continue your preparation.'),
            const SizedBox(height: 24),
            TextField(
              controller: _email,
              keyboardType: TextInputType.emailAddress,
              autofillHints: const [AutofillHints.email],
              decoration: const InputDecoration(labelText: 'Email'),
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _password,
              obscureText: true,
              autofillHints: const [AutofillHints.password],
              decoration: const InputDecoration(labelText: 'Password'),
            ),
            if (_error != null) ...[
              const SizedBox(height: 12),
              Text(_error!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
            ],
            const SizedBox(height: 24),
            FilledButton(
              onPressed: _busy ? null : _submit,
              child: Text(_busy ? 'Signing in…' : 'Sign in'),
            ),
            const SizedBox(height: 8),
            OutlinedButton(
              onPressed: _busy
                  ? null
                  : () async {
                      setState(() => _busy = true);
                      await ref.read(sessionControllerProvider.notifier).guest();
                      setState(() => _busy = false);
                    },
              child: const Text('Continue as guest'),
            ),
            TextButton(
              onPressed: () => context.go('/register'),
              child: const Text('Create an account'),
            ),
          ],
        ),
      ),
    );
  }
}
