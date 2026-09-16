import 'package:flutter/material.dart';
import 'package:medycatalog/features/billing/data/billing_repository.dart';

/// Non-blocking ad slot. Never used on the question player.
class AdBannerSlot extends StatelessWidget {
  const AdBannerSlot({super.key, required this.config, required this.placement});

  final AdsConfig? config;
  final String placement;

  @override
  Widget build(BuildContext context) {
    if (config == null || !config!.showAds) {
      return const SizedBox.shrink();
    }
    final unit = config!.placement(placement);
    if (unit == null || placement == 'PLAYER') {
      return const SizedBox.shrink();
    }
    return Semantics(
      label: 'Advertisement',
      child: Container(
        margin: const EdgeInsets.only(top: 16),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          border: Border.all(color: Theme.of(context).colorScheme.outlineVariant),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          children: [
            Icon(Icons.campaign_outlined, color: Theme.of(context).colorScheme.primary),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                'Sponsored — Premium removes ads. This never appears during a test.',
                style: Theme.of(context).textTheme.bodySmall,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
