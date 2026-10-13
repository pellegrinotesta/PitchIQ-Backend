package com.pitchiq.bff.PitchIQ.tracking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pitchiq.bff.PitchIQ.tracking.service.StatsBombService.TiroDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class StatsBombService {

    private final RestClient restClient;
    private final ObjectMapper mapper;

    private static final String RAW = "https://raw.githubusercontent.com/statsbomb/open-data/master/data";

    public StatsBombService(RestClient.Builder builder, ObjectMapper mapper) {
        this.restClient = builder
                .defaultHeader("User-Agent", "PitchIQ/1.0")
                .build();
        this.mapper = mapper;
    }

    public JsonNode competizioni() {
        return fetchJson(RAW + "/competitions.json");
    }

    public JsonNode partite(int competitionId, int seasonId) {
        return fetchJson(RAW + "/matches/" + competitionId + "/" + seasonId + ".json");
    }

    public JsonNode eventi(int matchId) {
        return fetchJson(RAW + "/events/" + matchId + ".json");
    }

    public JsonNode lineups(int matchId) {
        return fetchJson(RAW + "/lineups/" + matchId + ".json");
    }

    public List<TiroDto> tiri(int matchId) {
        JsonNode eventi = eventi(matchId);
        List<TiroDto> tiri = new ArrayList<>();

        for (JsonNode e : eventi) {
            if (!"Shot".equals(e.path("type").path("name").asText())) continue;

            JsonNode shot = e.path("shot");
            JsonNode loc  = e.path("location");

            tiri.add(new TiroDto(
                    e.path("id").asText(),
                    e.path("team").path("name").asText(),
                    e.path("player").path("name").asText(),
                    e.path("minute").asInt(),
                    e.path("second").asInt(),
                    loc.get(0).asDouble(),
                    loc.get(1).asDouble(),
                    shot.path("statsbomb_xg").asDouble(0),
                    shot.path("outcome").path("name").asText(),
                    shot.path("technique").path("name").asText(),
                    shot.path("body_part").path("name").asText(),
                    "Goal".equals(shot.path("outcome").path("name").asText())
            ));
        }
        return tiri;
    }

    private JsonNode fetchJson(String url) {
        String json = restClient.get()
                .uri(url)
                .header("Accept", "application/json, text/plain, */*")
                .retrieve()
                .body(String.class);
        try {
            return mapper.readTree(json);
        } catch (Exception e) {
            log.error("Errore parsing JSON da {}: {}", url, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Errore nel parsing della risposta da StatsBomb");
        }
    }

    public record TiroDto(
            String id,
            String squadra,
            String giocatore,
            int minuto,
            int secondo,
            double x,
            double y,
            double xg,
            String esito,
            String tecnica,
            String parteCorpo,
            boolean gol
    ) {}
}