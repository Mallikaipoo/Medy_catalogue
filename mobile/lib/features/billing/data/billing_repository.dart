import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:medycatalog/core/error/failure.dart';
import 'package:medycatalog/core/network/api_client.dart';

final billingRepositoryProvider = Provider<BillingRepository>((ref) {
  return BillingRepository(ref.watch(dioProvider));
});

class BillingRepository {
  BillingRepository(this._dio);

  final Dio _dio;

  String get storePlatform =>
      defaultTargetPlatform == TargetPlatform.iOS ? 'APP_STORE' : 'GOOGLE_PLAY';

  String get adsPlatform => defaultTargetPlatform == TargetPlatform.iOS ? 'IOS' : 'ANDROID';

  Future<SubscriptionMe> me() async {
    try {
      final response = await _dio.get<Map<String, dynamic>>('/subscription/me');
      return SubscriptionMe.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<SubscriptionMe> verify({required String productId, required String purchaseToken}) async {
    try {
      final response = await _dio.post<Map<String, dynamic>>(
        '/subscription/verify',
        data: {
          'platform': storePlatform,
          'productId': productId,
          'purchaseToken': purchaseToken,
        },
      );
      return SubscriptionMe.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<SubscriptionMe> restore() async {
    try {
      final response = await _dio.post<Map<String, dynamic>>('/subscription/restore');
      return SubscriptionMe.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<SubscriptionMe> rewarded() async {
    try {
      final response = await _dio.post<Map<String, dynamic>>('/subscription/rewarded');
      return SubscriptionMe.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Future<AdsConfig> ads() async {
    try {
      final response = await _dio.get<Map<String, dynamic>>(
        '/ads/config',
        queryParameters: {'platform': adsPlatform},
      );
      return AdsConfig.fromJson(response.data!);
    } on DioException catch (error) {
      throw _wrap(error);
    }
  }

  Object _wrap(DioException error) => error.error is Failure ? error.error as Failure : Failure.offline;
}

class SubscriptionMe {
  const SubscriptionMe({required this.entitlement, required this.plans});

  final Entitlement entitlement;
  final List<PlanCard> plans;

  factory SubscriptionMe.fromJson(Map<String, dynamic> json) {
    return SubscriptionMe(
      entitlement: Entitlement.fromJson(json['entitlement'] as Map<String, dynamic>? ?? const {}),
      plans: (json['plans'] as List<dynamic>? ?? const [])
          .map((item) => PlanCard.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class Entitlement {
  const Entitlement({
    required this.planCode,
    required this.planName,
    required this.status,
    required this.adsOff,
    required this.unlimitedPractice,
    required this.detailedExplanations,
    required this.rewardedExtraAttempts,
    required this.dailyPracticeSessions,
    required this.extraStartsRemaining,
    required this.practiceStartedToday,
    required this.practiceRemainingToday,
  });

  final String planCode;
  final String planName;
  final String status;
  final bool adsOff;
  final bool unlimitedPractice;
  final bool detailedExplanations;
  final bool rewardedExtraAttempts;
  final int dailyPracticeSessions;
  final int extraStartsRemaining;
  final int practiceStartedToday;
  final int practiceRemainingToday;

  bool get premium => planCode == 'PREMIUM';

  factory Entitlement.fromJson(Map<String, dynamic> json) {
    return Entitlement(
      planCode: json['planCode'] as String? ?? 'FREE',
      planName: json['planName'] as String? ?? 'Free',
      status: json['status'] as String? ?? 'NONE',
      adsOff: json['adsOff'] as bool? ?? false,
      unlimitedPractice: json['unlimitedPractice'] as bool? ?? false,
      detailedExplanations: json['detailedExplanations'] as bool? ?? false,
      rewardedExtraAttempts: json['rewardedExtraAttempts'] as bool? ?? true,
      dailyPracticeSessions: (json['dailyPracticeSessions'] as num?)?.toInt() ?? 3,
      extraStartsRemaining: (json['extraStartsRemaining'] as num?)?.toInt() ?? 0,
      practiceStartedToday: (json['practiceStartedToday'] as num?)?.toInt() ?? 0,
      practiceRemainingToday: (json['practiceRemainingToday'] as num?)?.toInt() ?? 0,
    );
  }
}

class PlanCard {
  const PlanCard({
    required this.code,
    required this.name,
    required this.description,
    required this.highlight,
    required this.prices,
  });

  final String code;
  final String name;
  final String description;
  final bool highlight;
  final List<PriceCard> prices;

  factory PlanCard.fromJson(Map<String, dynamic> json) {
    return PlanCard(
      code: json['code'] as String? ?? 'PREMIUM',
      name: json['name'] as String? ?? 'Premium',
      description: json['description'] as String? ?? '',
      highlight: json['highlight'] as bool? ?? false,
      prices: (json['prices'] as List<dynamic>? ?? const [])
          .map((item) => PriceCard.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }
}

class PriceCard {
  const PriceCard({
    required this.platform,
    required this.period,
    required this.currency,
    required this.amount,
    required this.storeProductId,
    required this.highlighted,
    required this.operationalNote,
  });

  final String platform;
  final String period;
  final String currency;
  final double amount;
  final String storeProductId;
  final bool highlighted;
  final String operationalNote;

  factory PriceCard.fromJson(Map<String, dynamic> json) {
    return PriceCard(
      platform: json['platform'] as String? ?? 'GOOGLE_PLAY',
      period: json['period'] as String? ?? 'YEAR',
      currency: json['currency'] as String? ?? 'INR',
      amount: (json['amount'] as num?)?.toDouble() ?? 0,
      storeProductId: json['storeProductId'] as String? ?? '',
      highlighted: json['highlighted'] as bool? ?? false,
      operationalNote: json['operationalNote'] as String? ?? '',
    );
  }
}

class AdsConfig {
  const AdsConfig({required this.showAds, required this.placements});

  final bool showAds;
  final List<AdPlacement> placements;

  factory AdsConfig.fromJson(Map<String, dynamic> json) {
    return AdsConfig(
      showAds: json['showAds'] as bool? ?? false,
      placements: (json['placements'] as List<dynamic>? ?? const [])
          .map((item) => AdPlacement.fromJson(item as Map<String, dynamic>))
          .toList(),
    );
  }

  AdPlacement? placement(String code) {
    if (code.contains('PLAYER') || code.contains('EXAM') || code.contains('SUBMIT')) {
      return null;
    }
    for (final item in placements) {
      if (item.placement == code &&
          !item.placement.contains('PLAYER') &&
          !item.placement.contains('EXAM')) {
        return item;
      }
    }
    return null;
  }
}

class AdPlacement {
  const AdPlacement({required this.placement, required this.unitId});

  final String placement;
  final String unitId;

  factory AdPlacement.fromJson(Map<String, dynamic> json) {
    return AdPlacement(
      placement: json['placement'] as String? ?? '',
      unitId: json['unitId'] as String? ?? '',
    );
  }
}
