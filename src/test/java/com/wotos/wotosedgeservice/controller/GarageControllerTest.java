package com.wotos.wotosedgeservice.controller;

import com.wotos.wotosedgeservice.client.VehicleGarageClient;
import com.wotos.wotosedgeservice.util.model.WotVehicle;
import com.wotos.wotosedgeservice.viewmodel.GarageVehicle;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GarageControllerTest {

    private VehicleGarageClient garageClient;
    private GarageController controller;

    @Before
    public void setUp() {
        garageClient = mock(VehicleGarageClient.class);
        controller = new GarageController(garageClient);

        // GarageController reads RequestAttributes to propagate context to futures
        MockHttpServletRequest mockReq = new MockHttpServletRequest();
        mockReq.addHeader("Authorization", "Bearer test-token");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockReq));
    }

    @After
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void returns_full_response_when_all_calls_succeed() {
        WotVehicle vehicle = mock(WotVehicle.class);
        when(garageClient.getVehicle(10)).thenReturn(ResponseEntity.ok(vehicle));
        when(garageClient.getVehicleArmor(10)).thenReturn(ResponseEntity.ok("armor-data"));
        when(garageClient.getVehicleModel(10)).thenReturn(ResponseEntity.ok("model-data"));

        ResponseEntity<GarageVehicle> response = controller.getVehicleGarage(10);

        GarageVehicle body = response.getBody();
        assertNotNull(body);
        assertSame(vehicle, body.getTankopedia());
        assertEquals("armor-data", body.getArmor());
        assertEquals("model-data", body.getModel());
        assertTrue("no errors expected", body.getErrors().isEmpty());
    }

    @Test
    public void returns_partial_response_when_armor_returns_404() {
        WotVehicle vehicle = mock(WotVehicle.class);
        when(garageClient.getVehicle(20)).thenReturn(ResponseEntity.ok(vehicle));
        when(garageClient.getVehicleArmor(20)).thenThrow(feignNotFound());
        when(garageClient.getVehicleModel(20)).thenReturn(ResponseEntity.ok("model-data"));

        ResponseEntity<GarageVehicle> response = controller.getVehicleGarage(20);

        GarageVehicle body = response.getBody();
        assertNotNull(body);
        assertSame(vehicle, body.getTankopedia());
        assertNull("armor should be null on 404", body.getArmor());
        assertEquals("model-data", body.getModel());
        assertFalse("errors should be non-empty", body.getErrors().isEmpty());
    }

    @Test
    public void returns_partial_response_when_tankopedia_returns_404() {
        when(garageClient.getVehicle(30)).thenThrow(feignNotFound());
        when(garageClient.getVehicleArmor(30)).thenReturn(ResponseEntity.ok("armor"));
        when(garageClient.getVehicleModel(30)).thenReturn(ResponseEntity.ok("model"));

        ResponseEntity<GarageVehicle> response = controller.getVehicleGarage(30);

        GarageVehicle body = response.getBody();
        assertNotNull(body);
        assertNull(body.getTankopedia());
        assertFalse(body.getErrors().isEmpty());
        assertTrue(body.getErrors().stream().anyMatch(e -> e.contains("tankopedia")));
    }

    @Test
    public void errors_list_empty_when_all_succeed() {
        when(garageClient.getVehicle(5)).thenReturn(ResponseEntity.ok(mock(WotVehicle.class)));
        when(garageClient.getVehicleArmor(5)).thenReturn(ResponseEntity.ok("a"));
        when(garageClient.getVehicleModel(5)).thenReturn(ResponseEntity.ok("m"));

        GarageVehicle body = controller.getVehicleGarage(5).getBody();

        assertNotNull(body);
        assertTrue(body.getErrors().isEmpty());
    }

    // --- helpers ---

    private static FeignException feignNotFound() {
        Map<String, Collection<String>> headers = Collections.emptyMap();
        Request req = Request.create(
                HttpMethod.GET, "http://localhost/api/vehicles/0", headers, null, StandardCharsets.UTF_8);
        return FeignException.errorStatus("getVehicle",
                feign.Response.builder()
                        .status(404)
                        .reason("Not Found")
                        .request(req)
                        .headers(headers)
                        .build());
    }
}
