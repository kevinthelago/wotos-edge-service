package com.wotos.wotosedgeservice.controller;

import com.wotos.wotosedgeservice.client.MapStatsClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class MapsControllerTest {

    private MapStatsClient mapStatsClient;
    private MapsController controller;

    @Before
    public void setUp() {
        mapStatsClient = mock(MapStatsClient.class);
        controller = new MapsController(mapStatsClient);
    }

    @Test
    public void delegates_to_map_client() {
        ResponseEntity<Object> upstreamResponse =
                ResponseEntity.ok((Object) "map-data");
        when(mapStatsClient.getPlayerMapStats(123)).thenReturn(upstreamResponse);

        ResponseEntity<Object> result = controller.getPlayerMapStats(123);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(mapStatsClient).getPlayerMapStats(123);
    }

    @Test
    public void propagates_upstream_status_code() {
        when(mapStatsClient.getPlayerMapStats(999))
                .thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<Object> result = controller.getPlayerMapStats(999);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
}
