-- Store application ID is in.claris.medycatalog (not in.techgeneza.medycatalog).

UPDATE plan_prices
SET store_product_id = replace(store_product_id, 'in.techgeneza.medycatalog', 'in.claris.medycatalog')
WHERE store_product_id LIKE 'in.techgeneza.medycatalog%';

UPDATE user_subscriptions
SET store_product_id = replace(store_product_id, 'in.techgeneza.medycatalog', 'in.claris.medycatalog')
WHERE store_product_id LIKE 'in.techgeneza.medycatalog%';
