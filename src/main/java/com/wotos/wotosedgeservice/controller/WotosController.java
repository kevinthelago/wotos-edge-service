package com.wotos.wotosedgeservice.controller;

import com.wotos.wotosedgeservice.service.WotosService;
import com.wotos.wotosedgeservice.util.model.WotPlayer;
import com.wotos.wotosedgeservice.util.model.WotVehicle;
import com.wotos.wotosedgeservice.util.model.statistics.PlayerStatisticsSnapshot;
import com.wotos.wotosedgeservice.util.model.statistics.VehicleStatisticsSnapshot;
import com.wotos.wotosedgeservice.validation.constraints.Language;
import com.wotos.wotosedgeservice.validation.constraints.PlayerSearch;
import com.wotos.wotosedgeservice.validation.constraints.VehicleType;
import com.wotos.wotosedgeservice.viewmodel.FullPlayerDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Max;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class WotosController {

    @Autowired
    WotosService wotosService;

    @GetMapping("/players")
    public Map<Integer, FullPlayerDetails> getFullPlayerDetailsByAccountIds(
            @RequestParam("accountIds") Integer[] accountIds
    ) {
        return wotosService.getFullPlayerDetailsByAccountIds(accountIds);
    }

    @GetMapping("/players/list")
    @PlayerSearch
    public List<WotPlayer> getPlayersByNickname(
            @RequestParam(value = "nicknames") String[] nicknames,
            @RequestParam(value = "language", required = false, defaultValue = "en") @Language String language,
            @RequestParam(value = "limit", required = false) @Max(100) Integer limit,
            @RequestParam(value = "searchType") String searchType
    ) {
        return wotosService.getPlayersByNickname(nicknames, language, limit, searchType);
    }

    @GetMapping("/vehicles")
    public Map<Integer, WotVehicle> getVehicles(
            @RequestParam(value = "fields", required = false) String[] fields,
            @RequestParam(value = "language", required = false, defaultValue = "en") @Language String language,
            @RequestParam(value = "limit", required = false) Integer limit,
            @RequestParam(value = "nations", required = false) String[] nations,
            @RequestParam(value = "page", required = false) Integer pageNumber,
            @RequestParam(value = "vehicleIds", required = false) Integer[] vehicleIds,
            @RequestParam(value = "vehicleTiers", required = false) Integer[] vehicleTiers,
            @RequestParam(value = "types", required = false) @VehicleType String[] vehicleTypes
    ) {
        return wotosService.getVehicles(fields, language, limit, nations, pageNumber, vehicleIds, vehicleTiers, vehicleTypes);
    }

}
