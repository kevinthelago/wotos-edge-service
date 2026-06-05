package com.wotos.wotosedgeservice.controller;

import com.wotos.wotosedgeservice.client.VehicleGarageClient;
import com.wotos.wotosedgeservice.util.model.WotVehicle;
import com.wotos.wotosedgeservice.viewmodel.GarageVehicle;
import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Edge fan-out that assembles a full vehicle garage entry from three parallel
 * calls to the vehicle-service.
 *
 * <p>All three downstream calls (tankopedia, armor, model) are dispatched
 * concurrently using {@link CompletableFuture}. A 404 or other error from any
 * single call does not fail the whole response: the affected field is left
 * {@code null} and an entry is added to the {@code errors} list so the
 * frontend can render partial state.
 *
 * <p>The incoming {@code RequestAttributes} are propagated into each future
 * so that {@link com.wotos.wotosedgeservice.security.AuthorizationRelayInterceptor}
 * can read the {@code Authorization} header from the child threads.
 *
 * <p>Requires a valid RS256 bearer JWT (enforced by JwtAuthFilter).
 */
@RestController
@RequestMapping("/api/vehicles")
public class GarageController {

    private final VehicleGarageClient vehicleGarageClient;

    public GarageController(VehicleGarageClient vehicleGarageClient) {
        this.vehicleGarageClient = vehicleGarageClient;
    }

    /**
     * Returns a merged garage view for the requested vehicle.
     * Always HTTP 200; failed sections are {@code null} with reasons in
     * {@code errors}.
     *
     * @param id vehicle (tank) ID
     */
    @GetMapping("/{id}/garage")
    public ResponseEntity<GarageVehicle> getVehicleGarage(@PathVariable Integer id) {
        // Capture request context before spawning futures so the relay interceptor
        // can access the Authorization header from child threads.
        RequestAttributes originalAttrs = RequestContextHolder.getRequestAttributes();

        CompletableFuture<WotVehicle> tankFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(originalAttrs);
            try {
                return vehicleGarageClient.getVehicle(id).getBody();
            } catch (FeignException e) {
                return null;
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });

        CompletableFuture<Object> armorFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(originalAttrs);
            try {
                return vehicleGarageClient.getVehicleArmor(id).getBody();
            } catch (FeignException e) {
                return null;
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });

        CompletableFuture<Object> modelFuture = CompletableFuture.supplyAsync(() -> {
            RequestContextHolder.setRequestAttributes(originalAttrs);
            try {
                return vehicleGarageClient.getVehicleModel(id).getBody();
            } catch (FeignException e) {
                return null;
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        });

        CompletableFuture.allOf(tankFuture, armorFuture, modelFuture).join();

        List<String> errors = new ArrayList<>();
        WotVehicle tankopedia = safeJoin(tankFuture, errors, "tankopedia unavailable for vehicle " + id);
        Object armor = safeJoin(armorFuture, errors, "armor unavailable for vehicle " + id);
        Object model = safeJoin(modelFuture, errors, "model unavailable for vehicle " + id);

        if (tankopedia == null && !hasErrorFor(errors, "tankopedia")) {
            errors.add("tankopedia not found for vehicle " + id);
        }
        if (armor == null && !hasErrorFor(errors, "armor")) {
            errors.add("armor not found for vehicle " + id);
        }
        if (model == null && !hasErrorFor(errors, "model")) {
            errors.add("model not found for vehicle " + id);
        }

        return ResponseEntity.ok(new GarageVehicle(tankopedia, armor, model, null, errors));
    }

    private static <T> T safeJoin(CompletableFuture<T> future, List<String> errors, String errorMessage) {
        try {
            return future.join();
        } catch (CompletionException e) {
            errors.add(errorMessage + ": " + e.getCause().getMessage());
            return null;
        }
    }

    private static boolean hasErrorFor(List<String> errors, String prefix) {
        for (String err : errors) {
            if (err.startsWith(prefix)) return true;
        }
        return false;
    }
}
