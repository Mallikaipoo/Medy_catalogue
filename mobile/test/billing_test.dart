import 'package:flutter_test/flutter_test.dart';
import 'package:medycatalog/features/billing/data/billing_repository.dart';

void main() {
  test('ads config never exposes a player placement', () {
    final config = AdsConfig.fromJson({
      'showAds': true,
      'placements': [
        {'placement': 'HOME_BANNER', 'unitId': 'banner'},
        {'placement': 'PLAYER', 'unitId': 'must-not-show'},
      ],
    });
    expect(config.placement('HOME_BANNER')?.unitId, 'banner');
    expect(config.placement('PLAYER'), isNull);
  });

  test('yearly is the commercial default in parsed plans', () {
    final me = SubscriptionMe.fromJson({
      'entitlement': {'planCode': 'FREE', 'planName': 'Free'},
      'plans': [
        {
          'code': 'PREMIUM',
          'name': 'Premium',
          'highlight': true,
          'prices': [
            {
              'period': 'YEAR',
              'amount': 1499,
              'highlighted': true,
              'storeProductId': 'in.claris.medycatalog.premium.yearly',
              'platform': 'GOOGLE_PLAY',
              'currency': 'INR',
            }
          ]
        }
      ]
    });
    expect(me.plans.first.prices.first.highlighted, isTrue);
    expect(me.plans.first.prices.first.amount, 1499);
  });
}
