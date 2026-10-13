package com.pitchiq.bff.PitchIQ.tracking.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.pitchiq.bff.PitchIQ.tracking.service.StatsBombService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking/statsbomb")
@RequiredArgsConstructor
public class StatsBombController {

    private final StatsBombService service;

    @GetMapping("/competizioni")
    public JsonNode competizioni() {
        return service.competizioni();
    }

    @GetMapping("/partite/{competitionId}/{seasonId}")
    public JsonNode partite(@PathVariable int competitionId,
                            @PathVariable int seasonId) {
        return service.partite(competitionId, seasonId);
    }

    @GetMapping("/eventi/{matchId}")
    public JsonNode eventi(@PathVariable int matchId) {
        return service.eventi(matchId);
    }

    @GetMapping("/tiri/{matchId}")
    public List<StatsBombService.TiroDto> tiri(@PathVariable int matchId) {
        return service.tiri(matchId);
    }

    @GetMapping("/lineups/{matchId}")
    public JsonNode lineups(@PathVariable int matchId) {
        return service.lineups(matchId);
    }
}