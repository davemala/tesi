// java
package com.example.tesi_app.service;

import com.example.tesi_app.dto.HeartRateDto;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class HeartRateService {
    private final CopyOnWriteArrayList<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private volatile HeartRateDto latest;

    public SseEmitter registerClient() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        // send current value immediately if present
        if (latest != null) {
            try {
                emitter.send(SseEmitter.event().name("hr").data(latest));
            } catch (IOException ignored) {}
        }

        return emitter;
    }

    public void publish(HeartRateDto dto) {
        latest = dto;
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().name("hr").data(dto));
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
    }

    public HeartRateDto getLatest() {
        return latest;
    }
}
