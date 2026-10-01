package com.logstream.logstreambackend.alert;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = {
        "http://localhost:5175",
        "http://localhost:5176",
        "http://localhost:5177"
})
public class AlertController {

    private final AlertingService alertingService;

    public AlertController(AlertingService alertingService) {
        this.alertingService = alertingService;
    }

    @GetMapping("/status")
    public AlertStatus getStatus() {
        return alertingService.getAlertStatus();
    }

    @GetMapping("/history")
    public List<AlertHistory> getHistory() {
        return alertingService.getAlertHistory();
    }
}