// java
package com.example.tesi_app.controller;

import com.example.tesi_app.dto.HeartRateDto;
import com.example.tesi_app.service.HeartRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/hr")
public class HeartRateController {
    private final HeartRateService service;

    public HeartRateController(HeartRateService service) {
        this.service = service;
    }

    // Receive heart-rate readings (called by the bluetooth client)
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Void> receive(@RequestBody HeartRateDto dto) {
        service.publish(dto);
        return ResponseEntity.ok().build();
    }

    // SSE stream for web clients (index.html)
    @GetMapping(path = "/stream", produces = "text/event-stream")
    public SseEmitter stream() {
        return service.registerClient();
    }

    // optional: get latest value via REST
    @GetMapping("/latest")
    public ResponseEntity<HeartRateDto> latest() {
        HeartRateDto latest = service.getLatest();
        if (latest == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(latest);
    }
}
