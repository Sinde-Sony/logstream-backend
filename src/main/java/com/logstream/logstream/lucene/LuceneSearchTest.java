package com.logstream.lucene;

import org.apache.lucene.document.Document;

import java.util.List;

public class LuceneSearchTest {

    public static void main(String[] args) throws Exception {

        LogIndexService indexService =
                new LogIndexService();

        System.out.println("====================================");
        System.out.println("       LUCENE SEARCH TEST");
        System.out.println("====================================");

        /*
         * Search for ERROR logs.
         */
        List<Document> results =
                indexService.searchLogs(
                        "level:ERROR",
                        50
                );

        System.out.println(
                "Query: level:ERROR"
        );

        System.out.println(
                "Results found: " + results.size()
        );

        System.out.println("------------------------------------");

        for (Document document : results) {

            System.out.println(
                    "Level          : "
                            + document.get("level")
            );

            System.out.println(
                    "Service        : "
                            + document.get("service")
            );

            System.out.println(
                    "Message        : "
                            + document.get("message")
            );

            System.out.println(
                    "Response Time  : "
                            + document.get("response_time")
            );

            System.out.println("------------------------------------");
        }

        indexService.close();

        System.out.println("====================================");
        System.out.println("       SEARCH TEST COMPLETED");
        System.out.println("====================================");
    }
}