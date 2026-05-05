ALTER TABLE tb_voucher_order
ADD COLUMN fail_reason varchar(255) NULL DEFAULT NULL COMMENT 'async order failure reason'
AFTER status;
