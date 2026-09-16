class UserProfile {
  const UserProfile({
    required this.id,
    required this.name,
    required this.guest,
    required this.roles,
    required this.exams,
    this.email,
    this.preferredLanguage,
    this.accountStatus,
  });

  final String id;
  final String name;
  final String? email;
  final String? preferredLanguage;
  final String? accountStatus;
  final bool guest;
  final List<String> roles;
  final List<ExamOption> exams;

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    return UserProfile(
      id: json['id'] as String,
      name: json['name'] as String? ?? 'Student',
      email: json['email'] as String?,
      preferredLanguage: json['preferredLanguage'] as String?,
      accountStatus: json['accountStatus'] as String?,
      guest: json['guest'] as bool? ?? false,
      roles: (json['roles'] as List<dynamic>? ?? const []).cast<String>(),
      exams: (json['exams'] as List<dynamic>? ?? const [])
          .map((item) => ExamOption.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class ExamOption {
  const ExamOption({
    required this.id,
    required this.code,
    required this.name,
    this.primary = false,
  });

  final String id;
  final String code;
  final String name;
  final bool primary;

  factory ExamOption.fromJson(Map<String, dynamic> json) {
    return ExamOption(
      id: json['id'] as String,
      code: json['code'] as String,
      name: json['name'] as String,
      primary: json['primary'] as bool? ?? false,
    );
  }
}

class AuthTokens {
  const AuthTokens({required this.accessToken, required this.refreshToken, required this.user});

  final String accessToken;
  final String refreshToken;
  final UserProfile user;
}
