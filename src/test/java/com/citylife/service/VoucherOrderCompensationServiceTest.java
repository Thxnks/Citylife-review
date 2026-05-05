package com.citylife.service;

import com.citylife.entity.VoucherOrder;
import com.citylife.mapper.VoucherOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherOrderCompensationServiceTest {

    @Mock
    private VoucherOrderMapper voucherOrderMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    private VoucherOrderCompensationService compensationService;

    @BeforeEach
    void setUp() {
        compensationService = new VoucherOrderCompensationService();
        ReflectionTestUtils.setField(compensationService, "voucherOrderMapper", voucherOrderMapper);
        ReflectionTestUtils.setField(compensationService, "stringRedisTemplate", stringRedisTemplate);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    void shouldMarkOrderFailedAndRollbackRedisState() {
        when(voucherOrderMapper.update(isNull(), any())).thenReturn(1);

        compensationService.failAndRollback(1001L, 10L, 20L, "MQ publish failed");

        verify(voucherOrderMapper).update(isNull(), any());
        verify(valueOperations).increment("seckill:stock:20");
        verify(setOperations).remove("seckill:order:20", "10");
    }
}
