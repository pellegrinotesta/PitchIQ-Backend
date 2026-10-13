package com.pitchiq.bff.PitchIQ.tracking.controller;

import com.pitchiq.bff.PitchIQ.tracking.dto.ImportDto;
import com.pitchiq.bff.PitchIQ.tracking.dto.PartitaGithubDto;
import com.pitchiq.bff.PitchIQ.tracking.service.GithubTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tracking/github")
@RequiredArgsConstructor
public class GithubTrackingController {

    private final GithubTrackingService githubService;

    @GetMapping("/partite")
    public List<PartitaGithubDto> listaPartite() {
        return githubService.listaPartite();
    }

    @PostMapping("/importa/{matchId}")
    public ResponseEntity<ImportDto> importa(@PathVariable String matchId) {
        return ResponseEntity.accepted().body(githubService.importa(matchId));
    }
}
