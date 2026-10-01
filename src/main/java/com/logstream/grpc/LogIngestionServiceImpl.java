package com.logstream.logstreambackend;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogIngestionServiceImpl {

    @PostMapping("/api/test-live-log")
    public String sendTestLog() {

        String liveLog =
                "[ERROR] billing-api - Database connection failed";

        LogWebSocketHandler.broadcastLog(liveLog);

        return "Test log sent to Live Tail";
    }
}