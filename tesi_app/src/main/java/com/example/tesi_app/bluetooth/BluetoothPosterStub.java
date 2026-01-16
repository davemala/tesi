// java
package com.example.tesi_app.bluetooth;

import com.example.tesi_app.dto.HeartRateDto;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/*
 Simple HTTP poster to send heart rate readings to the Spring Boot server.
 Replace the stubbed bpm reading logic with actual Bluetooth-reading code (BlueCove, TinyB, or an external script).
 On Windows, Bluetooth Low Energy Java support is limited; consider using a small native/ Python helper (bleak) and POST to the server.
*/
public class BluetoothPosterStub {

    public static void postHeartRate(int bpm) throws Exception {
        HeartRateDto dto = new HeartRateDto(bpm, System.currentTimeMillis());
        URL url = new URL("http://localhost:8080/api/hr");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        String json = String.format("{\"bpm\":%d,\"timestamp\":%d}", dto.getBpm(), dto.getTimestamp());
        byte[] out = json.getBytes(StandardCharsets.UTF_8);

        con.setFixedLengthStreamingMode(out.length);
        try (OutputStream os = con.getOutputStream()) {
            os.write(out);
        }

        int resp = con.getResponseCode();
        if (resp != 200) {
            throw new RuntimeException("Failed to post HR: " + resp);
        }
        con.disconnect();
    }

    // Example usage
    public static void main(String[] args) throws Exception {
        // Replace this loop with actual Bluetooth data reads
        for (int i = 0; i < 5; i++) {
            postHeartRate(60 + i);
            Thread.sleep(2000);
        }
    }
}
