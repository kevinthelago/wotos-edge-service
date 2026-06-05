package com.wotos.wotosedgeservice.viewmodel;

import com.wotos.wotosedgeservice.util.model.WotVehicle;

import java.util.List;

/**
 * Composite response for {@code GET /api/vehicles/{id}/garage}.
 *
 * <p>The four data sections (tankopedia, armor, model, shellTypes) are fetched
 * in parallel from the vehicle-service. Any section that could not be retrieved
 * is {@code null} and its failure reason is appended to {@link #errors}. A
 * partially-filled response is still returned with HTTP 200 so the frontend
 * can render what is available while showing inline error states for the rest.
 *
 * <p>Note: {@code shellTypes} is reserved for data from the vehicle-service
 * {@code shell_types} payload; it is populated from the tankopedia response
 * once the vehicle-service contract is finalised.
 */
public class GarageVehicle {

    private final WotVehicle tankopedia;
    private final Object armor;
    private final Object model;
    private final Object shellTypes;
    private final List<String> errors;

    public GarageVehicle(
            WotVehicle tankopedia,
            Object armor,
            Object model,
            Object shellTypes,
            List<String> errors
    ) {
        this.tankopedia = tankopedia;
        this.armor = armor;
        this.model = model;
        this.shellTypes = shellTypes;
        this.errors = errors;
    }

    public WotVehicle getTankopedia() { return tankopedia; }
    public Object getArmor() { return armor; }
    public Object getModel() { return model; }
    public Object getShellTypes() { return shellTypes; }
    public List<String> getErrors() { return errors; }
}
