# Performance Test Guide

This document describes how to generate real performance-test results for the flash-sale order flow.

Do not fabricate screenshots. Run the test in your own environment and place screenshots under:

```text
docs/screenshots/
```

Recommended screenshots:

```text
docs/screenshots/jmeter-summary-report.png
docs/screenshots/rabbitmq-queue.png
docs/screenshots/redis-seckill-keys.png
docs/screenshots/mysql-voucher-orders.png
```

## Target Scenario

Endpoint:

```http
POST /voucher-order/seckill/{voucherId}
```

Headers:

```http
authorization: {token}
```

Goal:

- Verify that stock is not oversold.
- Verify that one user cannot create duplicate orders.
- Verify that RabbitMQ can absorb order creation pressure.
- Verify that failed consumer messages go to the dead-letter queue.
- Verify that timeout or failed `PROCESSING` orders are marked `FAILED` and Redis pre-deducted state is rolled back.

## Environment Checklist

- MySQL is running and `citylife_review.sql` has been imported.
- Redis is running.
- RabbitMQ is running.
- Application is running on port `8081`.
- A valid login token has been generated.
- Redis seckill stock has been initialized:

```text
seckill:stock:{voucherId}
```

## Suggested JMeter Plan

Thread Group:

```text
Number of Threads: 200
Ramp-up Period: 5 seconds
Loop Count: 1
```

HTTP Request:

```text
Method: POST
Path: /voucher-order/seckill/{voucherId}
```

HTTP Header Manager:

```text
authorization: ${token}
Content-Type: application/json
```

Listeners:

- Summary Report
- Aggregate Report
- View Results Tree, only for debugging

## Expected Checks

After the test:

```sql
SELECT voucher_id, COUNT(*) FROM tb_voucher_order GROUP BY voucher_id;

SELECT user_id, voucher_id, COUNT(*)
FROM tb_voucher_order
WHERE status = 1
GROUP BY user_id, voucher_id
HAVING COUNT(*) > 1;

SELECT id, user_id, voucher_id, status, fail_reason
FROM tb_voucher_order
WHERE status IN (0, 7);
```

Expected:

- Order count must not exceed the configured stock.
- Duplicate order query should return no rows.

Redis checks:

```text
GET seckill:stock:{voucherId}
SCARD seckill:order:{voucherId}
```

RabbitMQ checks:

- Main queue should drain after consumers process messages.
- Failed business messages should appear in the dead-letter queue.

## Result Template

Fill this after running the test:

| Metric | Value |
| --- | --- |
| Threads | TBD |
| Ramp-up | TBD |
| Voucher stock | TBD |
| Total requests | TBD |
| Successful responses | TBD |
| Failed responses | TBD |
| Average latency | TBD |
| P95 latency | TBD |
| Throughput | TBD |
| Orders created | TBD |
| Duplicate orders | TBD |
| Oversold | TBD |
