package com.wotos.wotosedgeservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for the statistics-service trend endpoint.
 *
 * <p>The {@code contextId} avoids a registration conflict with the existing
 * {@code WotosStatisticsFeignClient}, which also targets
 * {@code wotos-statistics-service}.
 */
@FeignClient(name = "wotos-statistics-service", contextId = "statisticsTrendClient")
@RequestMapping("/api/stats")
public interface StatisticsTrendClient {

    /**
     * Fetches trend snapshots for a single player.
     *
     * @param id     WoT account ID
     * @param from   epoch-seconds start of the trend window (optional)
     * @param to     epoch-seconds end of the trend window (optional)
     * @param bucket aggregation bucket size, e.g. {@code "day"} (optional)
     */
    @GetMapping("/players/{id}/trend")
    ResponseEntity<Object> getPlayerTrend(
            @PathVariable("id") Integer id,
            @RequestParam(value = "from", required = false) Long from,
            @RequestParam(value = "to", required = false) Long to,
            @RequestParam(value = "bucket", required = false) String bucket
    );
}
