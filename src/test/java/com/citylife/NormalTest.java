package com.citylife;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NormalTest {

    @Test
    void shouldCountConsecutiveSignBits() {
        int i = 0b1110111111111111111111111;

        int count = 0;
        while (true) {
            if ((i & 1) == 0) {
                break;
            } else {
                count++;
            }
            i >>>= 1;
        }

        i = 0b1110111111111111111111111;
        int count2 = 0;
        while (true) {
            if (i >>> 1 << 1 == i) {
                break;
            } else {
                count2++;
            }
            i >>>= 1;
        }

        assertEquals(21, count);
        assertEquals(count, count2);
    }
}
