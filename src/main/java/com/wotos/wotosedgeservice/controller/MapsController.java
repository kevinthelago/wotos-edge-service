package com.wotos.wotosedgeservice.controller;

import com.wotos.wotosedgeservice.client.MapStatsClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Edge fan-out for map-service per-player statistics.
 * Requires a valid RS256 bearer JWT (enforced by JwtAuthFilter).
 *
 * <p>The {@code Authorization} header is forwarded to map-service automatically
 * via {@link com.wotos.wotosedgeservice.security.AuthorizationRelayInterceptor}.
 */
@RestController
@RequestMapping("/api/maps/players")
public class MapsController {

    private final MapStatsClient mapStatsClient;

    public MapsController(MapStatsClient mapStatsClient) {
        this.mapStatsClient = mapStatsClient;
    }

    /**
     * Returns map performance statistics for a single player.
     *
     * @param id WoT account ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getPlayerMapStats(@PathVariable Integer id) {
        return mapStatsClient.getPlayerMapStats(id);
    }
}
