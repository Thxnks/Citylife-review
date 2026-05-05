# Seckill Order Reliability

This document describes the reliability flow for asynchronous seckill orders.

## Order States

`tb_voucher_order.status` is used by the asynchronous order flow:

| Value | Status | Meaning |
| --- | --- | --- |
| 0 | PROCESSING | Redis pre-deduction succeeded and the order is waiting for MQ/DB completion. |
| 1 | SUCCESS | The consumer deducted MySQL stock and finalized the order. |
| 7 | FAILED | The order failed and Redis pre-deducted state has been rolled back. |

`tb_voucher_order.fail_reason` records why an asynchronous order failed.

## Flow

```text
HTTP request
  |
  v
Redis Lua
  - check stock
  - check one-user-one-order
  - pre-deduct Redis stock
  - record user qualification
  |
  v
Create PROCESSING order in MySQL
  |
  v
Publish VoucherOrderMessage to RabbitMQ
  |
  v
Consumer creates order
  - user-level Redisson lock
  - verify PROCESSING order
  - deduct MySQL stock
  - update order status to SUCCESS
```

## Compensation

The flow has three compensation paths:

- MQ publish failure: mark the `PROCESSING` order as `FAILED`, increment Redis stock, and remove the user from `seckill:order:{voucherId}`.
- Consumer failure: mark the order as `FAILED`, roll back Redis pre-deducted state, and reject the message to the dead-letter queue.
- Stale order timeout: a scheduled task scans `PROCESSING` orders older than 5 minutes and rolls them back.

The scheduled task runs every 60 seconds by default. It can be configured with:

```yaml
citylife:
  seckill:
    compensation-delay-ms: 60000
```

## Manual Checks

Find stale or failed orders:

```sql
SELECT id, user_id, voucher_id, status, fail_reason, update_time
FROM tb_voucher_order
WHERE status IN (0, 7)
ORDER BY update_time DESC;
```

Check duplicate orders:

```sql
SELECT user_id, voucher_id, COUNT(*)
FROM tb_voucher_order
WHERE status = 1
GROUP BY user_id, voucher_id
HAVING COUNT(*) > 1;
```
