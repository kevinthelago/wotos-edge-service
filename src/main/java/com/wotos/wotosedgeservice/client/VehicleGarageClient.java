package com.wotos.wotosedgeservice.client;

import com.wotos.wotosedgeservice.util.model.WotVehicle;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Feign client for the vehicle-service endpoints used by the garage fan-out.
 *
 * <p>Three parallel calls are made per request — tankopedia, armor, and 3-D
 * model — and merged into a single {@link com.wotos.wotosedgeservice.viewmodel.GarageVehicle}
 * response. Any 404 from any call is tolerated: the corresponding field is
 * nulled and the failure is reported in the {@code errors} list.
 */
@FeignClient(name = "wotos-vehicle-service", contextId = "vehicleGarageClient")
@RequestMapping("/api/vehicles")
public interface VehicleGarageClient {

    /** Tankopedia entry for the requested vehicle. */
    @GetMapping("/{id}")
    ResponseEntity<WotVehicle> getVehicle(@PathVariable("id") Integer id);

    /** Armour hit-zone data for the requested vehicle. */
    @GetMapping("/{id}/armor")
    ResponseEntity<Object> getVehicleArmor(@PathVariable("id") Integer id);

    /** 3-D model metadata for the requested vehicle. */
    @GetMapping("/{id}/model")
    ResponseEntity<Object> getVehicleModel(@PathVariable("id") Integer id);
}
