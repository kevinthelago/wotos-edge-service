package com.wotos.wotosedgeservice.controller;

import com.wotos.wotosedgeservice.client.StatisticsTrendClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TrendControllerTest {

    private StatisticsTrendClient trendClient;
    private TrendController controller;

    @Before
    public void setUp() {
        trendClient = mock(StatisticsTrendClient.class);
        controller = new TrendController(trendClient);
    }

    @Test
    public void delegates_to_statistics_client() {
        ResponseEntity<Object> upstreamResponse =
                ResponseEntity.ok((Object) "trend-data");
        when(trendClient.getPlayerTrend(42, 1000L, 2000L, "day"))
                .thenReturn(upstreamResponse);

        ResponseEntity<Object> result = controller.getPlayerTrend(42, 1000L, 2000L, "day");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(trendClient).getPlayerTrend(42, 1000L, 2000L, "day");
    }

    @Test
    public void passes_null_optional_params() {
        when(trendClient.getPlayerTrend(7, null, null, null))
                .thenReturn(ResponseEntity.ok(null));

        controller.getPlayerTrend(7, null, null, null);

        verify(trendClient).getPlayerTrend(7, null, null, null);
    }
}
