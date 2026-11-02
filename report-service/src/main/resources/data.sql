INSERT INTO management_reports
    (period_from, period_to, total_revenue, currency, breakdown_json, narrative, created_at)
VALUES
    (DATE '2026-01-01', DATE '2026-01-31', 1500.00, 'USD',
     '[{"category":"ads","amount":300.00},{"category":"subscriptions","amount":1200.00}]',
     'Management report 2026-01-01 to 2026-01-31: total 1500.00 USD. ads 300.00, subscriptions 1200.00.',
     TIMESTAMP '2026-03-01 09:00:00'),
    (DATE '2026-02-01', DATE '2026-02-28', 2300.00, 'USD',
     '[{"category":"services","amount":800.00},{"category":"subscriptions","amount":1500.00}]',
     'Management report 2026-02-01 to 2026-02-28: total 2300.00 USD. services 800.00, subscriptions 1500.00.',
     TIMESTAMP '2026-03-02 09:00:00');
