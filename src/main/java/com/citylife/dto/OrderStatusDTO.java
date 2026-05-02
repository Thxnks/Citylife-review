package com.citylife.dto;

import lombok.Data;

@Data
public class OrderStatusDTO {

    private Long orderId;
    private String status;
    private VoucherOrderMessage message;
}
