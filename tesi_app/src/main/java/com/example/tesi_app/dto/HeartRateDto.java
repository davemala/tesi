// java
package com.example.tesi_app.dto;

public class HeartRateDto {
    private int bpm;
    private long timestamp;

    public HeartRateDto() {}

    public HeartRateDto(int bpm, long timestamp) {
        this.bpm = bpm;
        this.timestamp = timestamp;
    }

    public int getBpm() {
        return bpm;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
