import 'package:flutter/material.dart';

class AppTheme {
  static ThemeData get light {
    const seed = Color(0xFF0F6B5C);
    return ThemeData(
      colorScheme: ColorScheme.fromSeed(seedColor: seed),
      useMaterial3: true,
      visualDensity: VisualDensity.standard,
    );
  }

  static ThemeData get dark {
    const seed = Color(0xFF4FD1C5);
    return ThemeData(
      colorScheme: ColorScheme.fromSeed(seedColor: seed, brightness: Brightness.dark),
      useMaterial3: true,
    );
  }
}
