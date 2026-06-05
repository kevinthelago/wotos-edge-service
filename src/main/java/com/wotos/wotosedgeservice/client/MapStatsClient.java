package com.wotos.wotosedgeservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Feign client for the map-service per-player map-statistics endpoint.
 */
@FeignClient(name = "wotos-map-service", contextId = "mapStatsClient")
@RequestMapping("/api/maps")
public interface MapStatsClient {

    /**
     * Returns map performance statistics for a single player.
     *
     * @param accountId WoT account ID
     */
    @GetMapping("/players/{accountId}")
    ResponseEntity<Object> getPlayerMapStats(@PathVariable("accountId") Integer accountId);
}
