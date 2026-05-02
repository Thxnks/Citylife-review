package com.citylife.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class VoucherOrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private Long voucherId;
}
