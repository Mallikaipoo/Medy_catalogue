class Failure implements Exception {
  const Failure(this.message);

  final String message;

  @override
  String toString() => message;

  static Failure fromStatus(int? status, [String? serverDetail]) {
    switch (status) {
      case 401:
        return Failure(serverDetail ?? 'Please sign in again to continue.');
      case 409:
        return Failure(serverDetail ?? 'An account with this email already exists.');
      case 429:
        return Failure('Too many attempts. Please wait a few minutes and try again.');
      default:
        if (status != null && status >= 500) {
          return const Failure('Something went wrong on our side. Please try again in a moment.');
        }
        return Failure(serverDetail ?? 'We could not complete that request. Please try again.');
    }
  }

  static const offline = Failure(
    "We couldn't connect right now. Please check your internet connection and try again.",
  );
}
