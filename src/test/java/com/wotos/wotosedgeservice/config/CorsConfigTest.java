package com.wotos.wotosedgeservice.config;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CorsConfigTest {

    @Test
    public void sanitizeOriginsTrimsEntriesAndDropsBlanks() {
        String[] result = CorsConfig.sanitizeOrigins(
                new String[]{" http://localhost:3000 ", "", "  ", "https://wotos.example"}
        );

        assertArrayEquals(new String[]{"http://localhost:3000", "https://wotos.example"}, result);
    }

    @Test
    public void sanitizeOriginsHandlesNull() {
        assertEquals(0, CorsConfig.sanitizeOrigins(null).length);
    }
}
