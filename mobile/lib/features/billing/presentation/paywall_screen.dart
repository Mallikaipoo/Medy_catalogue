import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:medycatalog/features/billing/data/billing_repository.dart';

class PaywallScreen extends ConsumerStatefulWidget {
  const PaywallScreen({super.key});

  @override
  ConsumerState<PaywallScreen> createState() => _PaywallScreenState();
}

class _PaywallScreenState extends ConsumerState<PaywallScreen> {
  SubscriptionMe? _me;
  String? _error;
  var _busy = false;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final me = await ref.read(billingRepositoryProvider).me();
      if (!mounted) {
        return;
      }
      setState(() => _me = me);
    } catch (error) {
      if (!mounted) {
        return;
      }
      setState(() => _error = error.toString());
    }
  }

  Future<void> _subscribe(PriceCard price) async {
    setState(() {
      _busy = true;
      _error = null;
    });
    try {
      final token = 'sandbox-${DateTime.now().millisecondsSinceEpoch}';
      final me = await ref.read(billingRepositoryProvider).verify(
            productId: price.storeProductId,
            purchaseToken: token,
          );
      if (!mounted) {
        return;
      }
      setState(() {
        _me = me;
        _busy = false;
      });
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Premium is active. Practice will not be interrupted.')),
      );
    } catch (error) {
      if (!mounted) {
        return;
      }
      setState(() {
        _busy = false;
        _error = error.toString();
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final me = _me;
    final prices = me == null
        ? const <PriceCard>[]
        : me.plans
            .expand((plan) => plan.prices)
            .where((price) => price.platform == ref.read(billingRepositoryProvider).storePlatform)
            .toList()
          ..sort((a, b) => (b.highlighted ? 1 : 0) - (a.highlighted ? 1 : 0));
    return Scaffold(
      appBar: AppBar(title: const Text('Premium')),
      body: me == null
          ? Center(child: _error == null ? const CircularProgressIndicator() : Text(_error!))
          : ListView(
              padding: const EdgeInsets.all(20),
              children: [
                Text(
                  me.entitlement.premium ? 'You are on Premium' : 'Keep studying without interruption',
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
                const SizedBox(height: 8),
                const Text(
                  'Yearly ₹1,499 is the default. One payment a year, 3-day billing grace, and a test already started is never cut off.',
                ),
                const SizedBox(height: 16),
                if (me.entitlement.premium)
                  Card(
                    child: ListTile(
                      leading: const Icon(Icons.verified_outlined),
                      title: Text(me.entitlement.planName),
                      subtitle: Text('Status ${me.entitlement.status} · ads off · unlimited practice'),
                    ),
                  ),
                for (final price in prices)
                  Card(
                    color: price.highlighted
                        ? Theme.of(context).colorScheme.primaryContainer
                        : null,
                    child: ListTile(
                      title: Text(
                        price.period == 'YEAR' ? 'Yearly ₹${price.amount.toStringAsFixed(0)}' : 'Monthly ₹${price.amount.toStringAsFixed(0)}',
                      ),
                      subtitle: Text(price.operationalNote),
                      trailing: price.highlighted ? const Text('Best') : null,
                      onTap: _busy || me.entitlement.premium ? null : () => _subscribe(price),
                    ),
                  ),
                if (_error != null) ...[
                  const SizedBox(height: 12),
                  Text(_error!, style: TextStyle(color: Theme.of(context).colorScheme.error)),
                ],
                const SizedBox(height: 12),
                if (!me.entitlement.premium)
                  FilledButton(
                    onPressed: _busy || prices.isEmpty ? null : () => _subscribe(prices.first),
                    child: Text(_busy ? 'Activating…' : 'Continue with yearly Premium'),
                  ),
                TextButton(
                  onPressed: _busy
                      ? null
                      : () async {
                          final restored = await ref.read(billingRepositoryProvider).restore();
                          if (!mounted) {
                            return;
                          }
                          setState(() => _me = restored);
                        },
                  child: const Text('Restore purchase'),
                ),
              ],
            ),
    );
  }
}
