package com.logstream.longstreambackend.lucene;

import com.logstream.logstreambackend.lucene.LogAggregationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final LogAggregationService aggregationService;

    public AnalyticsController(LogAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @GetMapping("/logs-per-minute")
    public Map<String, Long> getLogsPerMinute() throws Exception {
        return aggregationService.getLogsPerMinute();
    }
}