package com.logstream.controller;

import com.logstream.lucene.LogIndexService;
import org.apache.lucene.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LogSearchController {

    private final LogIndexService logIndexService;

    public LogSearchController(LogIndexService logIndexService) {
        this.logIndexService = logIndexService;
    }

    @GetMapping("/search")
    public List<LogResult> searchLogs(
            @RequestParam String query,
            @RequestParam(defaultValue = "50") int limit) {

        try {

            List<Document> documents =
                    logIndexService.searchLogs(query, limit);

            List<LogResult> results =
                    new ArrayList<>();

            for (Document document : documents) {

                results.add(
                        new LogResult(
                                document.get("level"),
                                document.get("service"),
                                document.get("message"),
                                document.get("trace_id"),
                                document.get("response_time"),
                                document.get("timestamp")
                        )
                );
            }

            return results;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Search failed: " + e.getMessage()
            );
        }
    }

    public record LogResult(
            String level,
            String service,
            String message,
            String traceId,
            String responseTime,
            String timestamp
    ) {
    }
}