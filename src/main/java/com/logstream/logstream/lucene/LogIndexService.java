package com.logstream.lucene;

import org.springframework.stereotype.Service;

import com.logstream.grpc.LogMessage;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LogIndexService {

    private final Directory directory;
    private final StandardAnalyzer analyzer;
    private final IndexWriter indexWriter;

    public LogIndexService() throws IOException {

        directory = FSDirectory.open(
                java.nio.file.Paths.get("data/lucene-index")
        );

        analyzer = new StandardAnalyzer();

        IndexWriterConfig config =
                new IndexWriterConfig(analyzer);

        indexWriter = new IndexWriter(directory, config);

        System.out.println(
                "Lucene index initialized successfully."
        );
    }

    public synchronized void indexLog(
            LogMessage logMessage) throws IOException {

        Document document = new Document();

        // Service
        document.add(
                new StringField(
                        "service",
                        logMessage.getService(),
                        Field.Store.YES
                )
        );

        // Level
        document.add(
                new StringField(
                        "level",
                        logMessage.getLevel(),
                        Field.Store.YES
                )
        );

        // Message
        document.add(
                new TextField(
                        "message",
                        logMessage.getMessage(),
                        Field.Store.YES
                )
        );

        // Trace ID
        document.add(
                new StringField(
                        "trace_id",
                        logMessage.getTraceId(),
                        Field.Store.YES
                )
        );

        // Response time - numeric field for range queries
        document.add(
                new LongPoint(
                        "response_time",
                        logMessage.getResponseTime()
                )
        );

        // Store response time so it can be displayed
        document.add(
                new StoredField(
                        "response_time",
                        logMessage.getResponseTime()
                )
        );

        // Timestamp
        long timestamp;

        try {
            timestamp = Long.parseLong(
                    logMessage.getTimestamp()
            );
        } catch (NumberFormatException e) {
            timestamp = System.currentTimeMillis();
        }

        // Timestamp - numeric field for range queries
        document.add(
                new LongPoint(
                        "timestamp",
                        timestamp
                )
        );

        // Store timestamp
        document.add(
                new StoredField(
                        "timestamp",
                        timestamp
                )
        );

        indexWriter.addDocument(document);
    }

    public synchronized void commit()
            throws IOException {

        indexWriter.commit();

        System.out.println(
                "Lucene index committed."
        );
    }

    public synchronized List<Document> searchLogs(
            String searchQuery,
            int limit) throws Exception {

        indexWriter.commit();

        try (DirectoryReader reader =
                     DirectoryReader.open(directory)) {

            IndexSearcher searcher =
                    new IndexSearcher(reader);

            Query query =
                    new org.apache.lucene.queryparser.classic.QueryParser(
                            "message",
                            analyzer
                    ).parse(searchQuery);

            TopDocs topDocs =
                    searcher.search(query, limit);

            List<Document> results =
                    new ArrayList<>();

            for (ScoreDoc scoreDoc :
                    topDocs.scoreDocs) {

                Document document =
                        searcher.storedFields()
                                .document(scoreDoc.doc);

                results.add(document);
            }

            return results;
        }
    }

    public synchronized int getDocumentCount()
            throws IOException {

        indexWriter.commit();

        try (DirectoryReader reader =
                     DirectoryReader.open(directory)) {

            return reader.numDocs();
        }
    }

    public synchronized void close()
            throws IOException {

        indexWriter.close();
        analyzer.close();
        directory.close();
    }
}