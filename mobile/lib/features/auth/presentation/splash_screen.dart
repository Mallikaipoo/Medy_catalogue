import 'package:flutter/material.dart';

class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return const Scaffold(
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text('MedyCatalog', style: TextStyle(fontSize: 28, fontWeight: FontWeight.w600)),
            SizedBox(height: 8),
            Text('Learn. Practice. Compete. Crack Your Exam.'),
            SizedBox(height: 24),
            CircularProgressIndicator(),
          ],
        ),
      ),
    );
  }
}
