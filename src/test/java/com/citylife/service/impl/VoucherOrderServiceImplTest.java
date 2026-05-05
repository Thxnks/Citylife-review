package com.citylife.service.impl;

import com.citylife.dto.OrderStatusDTO;
import com.citylife.dto.Result;
import com.citylife.dto.UserDTO;
import com.citylife.entity.VoucherOrder;
import com.citylife.enums.VoucherOrderStatus;
import com.citylife.mapper.VoucherOrderMapper;
import com.citylife.utils.UserHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherOrderServiceImplTest {

    @Mock
    private VoucherOrderMapper voucherOrderMapper;

    private VoucherOrderServiceImpl voucherOrderService;

    @BeforeEach
    void setUp() {
        voucherOrderService = new VoucherOrderServiceImpl();
        ReflectionTestUtils.setField(voucherOrderService, "baseMapper", voucherOrderMapper);

        UserDTO user = new UserDTO();
        user.setId(10L);
        UserHolder.saveUser(user);
    }

    @AfterEach
    void tearDown() {
        UserHolder.removeUser();
    }

    @Test
    void shouldReturnProcessingStatusForProcessingOrder() {
        VoucherOrder order = new VoucherOrder();
        order.setId(1001L);
        order.setUserId(10L);
        order.setVoucherId(20L);
        order.setStatus(VoucherOrderStatus.PROCESSING.getValue());
        when(voucherOrderMapper.selectById(1001L)).thenReturn(order);

        Result<?> result = voucherOrderService.queryOrderStatus(1001L);

        assertTrue(result.getSuccess());
        OrderStatusDTO status = (OrderStatusDTO) result.getData();
        assertEquals("PROCESSING", status.getStatus());
    }

    @Test
    void shouldReturnFailedStatusForFailedOrder() {
        VoucherOrder order = new VoucherOrder();
        order.setId(1001L);
        order.setUserId(10L);
        order.setVoucherId(20L);
        order.setStatus(VoucherOrderStatus.FAILED.getValue());
        when(voucherOrderMapper.selectById(1001L)).thenReturn(order);

        Result<?> result = voucherOrderService.queryOrderStatus(1001L);

        assertTrue(result.getSuccess());
        OrderStatusDTO status = (OrderStatusDTO) result.getData();
        assertEquals("FAILED", status.getStatus());
    }
}
