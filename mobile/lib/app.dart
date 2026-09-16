import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:medycatalog/core/theme/app_theme.dart';
import 'package:medycatalog/features/auth/presentation/exam_picker_screen.dart';
import 'package:medycatalog/features/auth/presentation/login_screen.dart';
import 'package:medycatalog/features/auth/presentation/register_screen.dart';
import 'package:medycatalog/features/auth/presentation/session_controller.dart';
import 'package:medycatalog/features/auth/presentation/splash_screen.dart';
import 'package:medycatalog/features/billing/presentation/paywall_screen.dart';
import 'package:medycatalog/features/catalog/presentation/topics_screen.dart';
import 'package:medycatalog/features/home/presentation/home_screen.dart';
import 'package:medycatalog/features/practice/presentation/practice_setup_screen.dart';
import 'package:medycatalog/features/practice/presentation/question_player_screen.dart';
import 'package:medycatalog/features/practice/presentation/result_screen.dart';
import 'package:medycatalog/features/profile/presentation/profile_screen.dart';

class MedyCatalogApp extends ConsumerWidget {
  const MedyCatalogApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final router = ref.watch(routerProvider);
    return MaterialApp.router(
      title: 'MedyCatalog',
      theme: AppTheme.light,
      darkTheme: AppTheme.dark,
      routerConfig: router,
    );
  }
}

final routerProvider = Provider<GoRouter>((ref) {
  final session = ref.watch(sessionControllerProvider);
  return GoRouter(
    initialLocation: '/splash',
    redirect: (context, state) {
      final public = state.matchedLocation == '/login' ||
          state.matchedLocation == '/register' ||
          state.matchedLocation == '/splash';
      return session.when(
        data: (user) {
          if (user == null) {
            return public ? null : '/login';
          }
          if (user.exams.isEmpty && state.matchedLocation != '/exams') {
            return '/exams';
          }
          if (public) {
            return user.exams.isEmpty ? '/exams' : '/home';
          }
          return null;
        },
        loading: () => state.matchedLocation == '/splash' ? null : '/splash',
        error: (_, __) => public ? null : '/login',
      );
    },
    routes: [
      GoRoute(path: '/splash', builder: (context, state) => const SplashScreen()),
      GoRoute(path: '/login', builder: (context, state) => const LoginScreen()),
      GoRoute(path: '/register', builder: (context, state) => const RegisterScreen()),
      GoRoute(path: '/exams', builder: (context, state) => const ExamPickerScreen()),
      GoRoute(path: '/home', builder: (context, state) => const HomeScreen()),
      GoRoute(path: '/premium', builder: (context, state) => const PaywallScreen()),
      GoRoute(path: '/profile', builder: (context, state) => const ProfileScreen()),
      GoRoute(
        path: '/chapters',
        builder: (context, state) => ChaptersScreen(
          examId: state.uri.queryParameters['examId'] ?? '',
          subjectId: state.uri.queryParameters['subjectId'] ?? '',
          subjectName: state.uri.queryParameters['subjectName'] ?? 'Subject',
        ),
      ),
      GoRoute(
        path: '/topics',
        builder: (context, state) => TopicsScreen(
          examId: state.uri.queryParameters['examId'] ?? '',
          subjectId: state.uri.queryParameters['subjectId'] ?? '',
          chapterId: state.uri.queryParameters['chapterId'] ?? '',
          chapterName: state.uri.queryParameters['chapterName'] ?? 'Chapter',
        ),
      ),
      GoRoute(
        path: '/syllabus',
        builder: (context, state) => SyllabusNotesScreen(
          examId: state.uri.queryParameters['examId'] ?? '',
          subjectId: state.uri.queryParameters['subjectId'] ?? '',
          chapterId: state.uri.queryParameters['chapterId'] ?? '',
          topicId: state.uri.queryParameters['topicId'] ?? '',
        ),
      ),
      GoRoute(
        path: '/practice/setup',
        builder: (context, state) => PracticeSetupScreen(
          examId: state.uri.queryParameters['examId'] ?? '',
          subjectId: state.uri.queryParameters['subjectId'],
          chapterId: state.uri.queryParameters['chapterId'],
        ),
      ),
      GoRoute(
        path: '/practice/:sessionId',
        builder: (context, state) => QuestionPlayerScreen(sessionId: state.pathParameters['sessionId']!),
      ),
      GoRoute(
        path: '/practice/:sessionId/result',
        builder: (context, state) => ResultScreen(sessionId: state.pathParameters['sessionId']!),
      ),
      GoRoute(
        path: '/practice/:sessionId/review',
        builder: (context, state) => ReviewScreen(sessionId: state.pathParameters['sessionId']!),
      ),
    ],
  );
});
