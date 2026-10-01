package com.logstream.logstreambackend.alert;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class AlertingService {

    private final Directory directory;

    // Stores the latest alert status
    private long latestErrorCount = 0;
    private boolean alertTriggered = false;

    // Stores recent alert checks
    private final List<AlertHistory> alertHistory = new ArrayList<>();

    // Alert configuration
    private static final int ERROR_THRESHOLD = 100;
    private static final long WINDOW_MILLIS = 5 * 60 * 1000;

    public AlertingService() throws Exception {
        directory = FSDirectory.open(
                Paths.get("data/lucene-index")
        );
    }

    @Scheduled(fixedRate = 30000)
    public void checkAlerts() {

        try (DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            long currentTime = System.currentTimeMillis();
            long fiveMinutesAgo = currentTime - WINDOW_MILLIS;

            // Condition 1: level = ERROR
            TermQuery errorQuery =
                    new TermQuery(new Term("level", "ERROR"));

            // Condition 2: timestamp is within last 5 minutes
            var timeQuery =
                    LongPoint.newRangeQuery(
                            "timestamp",
                            fiveMinutesAgo,
                            currentTime
                    );

            BooleanQuery query = new BooleanQuery.Builder()
                    .add(errorQuery, BooleanClause.Occur.MUST)
                    .add(timeQuery, BooleanClause.Occur.MUST)
                    .build();

            TopDocs results = searcher.search(query, 1000);

            long errorCount = results.totalHits.value();

            // Store latest alert information
            latestErrorCount = errorCount;
            alertTriggered = errorCount > ERROR_THRESHOLD;

            // Add alert check to history
            AlertHistory history = new AlertHistory(
                    LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("HH:mm:ss")
                    ),
                    errorCount,
                    ERROR_THRESHOLD,
                    alertTriggered
            );

            alertHistory.add(history);

            // Keep only the latest 20 checks
            if (alertHistory.size() > 20) {
                alertHistory.remove(0);
            }

            // Console output
            System.out.println();
            System.out.println("====================================");
            System.out.println("          ALERT CHECK");
            System.out.println("====================================");

            System.out.println(
                    "ERROR logs in last 5 minutes: " + errorCount
            );

            System.out.println(
                    "Alert threshold: " + ERROR_THRESHOLD
            );

            if (errorCount > ERROR_THRESHOLD) {

                System.out.println();
                System.out.println("ALERT TRIGGERED");

                System.out.println(
                        "Reason: ERROR count exceeded threshold"
                );

                System.out.println(
                        "Action: Simulated webhook notification"
                );

            } else {

                System.out.println("No alert triggered.");
            }

            System.out.println("====================================");
            System.out.println();

        } catch (Exception e) {

            System.err.println(
                    "[Alerting] Error checking alerts: "
                            + e.getMessage()
            );
        }
    }

    // Returns the latest alert status
    public AlertStatus getAlertStatus() {

        return new AlertStatus(
                latestErrorCount,
                ERROR_THRESHOLD,
                alertTriggered
        );
    }

    // Returns recent alert history
    public List<AlertHistory> getAlertHistory() {

        return alertHistory;
    }
}