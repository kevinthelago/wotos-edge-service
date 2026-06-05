package com.wotos.wotosedgeservice.controller;

import com.wotos.wotosedgeservice.client.StatisticsTrendClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Edge fan-out for the statistics-service trend endpoint.
 * Requires a valid RS256 bearer JWT (enforced by JwtAuthFilter).
 *
 * <p>The {@code Authorization} header is forwarded to statistics-service
 * automatically via {@link com.wotos.wotosedgeservice.security.AuthorizationRelayInterceptor}.
 */
@RestController
@RequestMapping("/api/stats/players")
public class TrendController {

    private final StatisticsTrendClient statisticsTrendClient;

    public TrendController(StatisticsTrendClient statisticsTrendClient) {
        this.statisticsTrendClient = statisticsTrendClient;
    }

    /**
     * Returns trend snapshots for a single player over an optional time window.
     *
     * @param id     WoT account ID
     * @param from   epoch-seconds start of the window (optional)
     * @param to     epoch-seconds end of the window (optional)
     * @param bucket aggregation bucket, e.g. {@code "day"} (optional)
     */
    @GetMapping("/{id}/trend")
    public ResponseEntity<Object> getPlayerTrend(
            @PathVariable Integer id,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "to", required = false) Long to,
            @RequestParam(value = "bucket", required = false) String bucket
    ) {
        return statisticsTrendClient.getPlayerTrend(id, from, to, bucket);
    }
}
