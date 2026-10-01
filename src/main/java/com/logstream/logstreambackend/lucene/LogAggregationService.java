package com.logstream.logstreambackend.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

@Service
public class LogAggregationService {

    private final Directory directory;

    public LogAggregationService() throws Exception {
        directory = FSDirectory.open(Paths.get("data/lucene-index"));
    }

    public Map<String, Long> getLogsPerMinute() throws Exception {

        Map<String, Long> counts = new TreeMap<>();

        long currentTime = System.currentTimeMillis();
        long oneHourAgo = currentTime - (60 * 60 * 1000);

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("HH:mm")
                        .withZone(ZoneId.systemDefault());

        try (DirectoryReader reader = DirectoryReader.open(directory)) {

            IndexSearcher searcher = new IndexSearcher(reader);

            TopDocs topDocs = searcher.search(
                    new MatchAllDocsQuery(),
                    reader.numDocs()
            );

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

                Document document =
                        searcher.storedFields().document(scoreDoc.doc);

                String timestamp = document.get("timestamp");

                if (timestamp == null) {
                    continue;
                }

                try {

                    long millis = Long.parseLong(timestamp);

                    // Only include logs from the last 60 minutes
                    if (millis >= oneHourAgo && millis <= currentTime) {

                        String minute =
                                formatter.format(
                                        Instant.ofEpochMilli(millis)
                                );

                        counts.put(
                                minute,
                                counts.getOrDefault(minute, 0L) + 1
                        );
                    }

                } catch (NumberFormatException ignored) {
                }
            }
        }

        return counts;
    }
}