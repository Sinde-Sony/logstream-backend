package com.logstream.logstream.lucene;
import org.apache.lucene.search.Query;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;

public class LuceneSearchBenchmark {

    public static void main(String[] args) throws Exception {

        var directory =
                FSDirectory.open(Paths.get("data/lucene-index"));

        try (DirectoryReader reader =
                     DirectoryReader.open(directory)) {

            IndexSearcher searcher =
                    new IndexSearcher(reader);

            // level:ERROR
            TermQuery levelQuery =
                    new TermQuery(
                            new Term("level", "ERROR")
                    );

            // service:billing-api
            TermQuery serviceQuery =
                    new TermQuery(
                            new Term("service", "billing-api")
                    );

            // response_time > 1000
            Query responseTimeQuery =
                    LongPoint.newRangeQuery(
                            "response_time",
                            1001,
                            Long.MAX_VALUE
                    );

            // Combine all three conditions
            BooleanQuery query =
                    new BooleanQuery.Builder()
                            .add(
                                    levelQuery,
                                    BooleanClause.Occur.MUST
                            )
                            .add(
                                    serviceQuery,
                                    BooleanClause.Occur.MUST
                            )
                            .add(
                                    responseTimeQuery,
                                    BooleanClause.Occur.MUST
                            )
                            .build();

            // Warm-up
            searcher.search(query, 100);

            // Benchmark
            long start = System.nanoTime();

            TopDocs results =
                    searcher.search(query, 1000);

            long end = System.nanoTime();

            double searchTimeMs =
                    (end - start) / 1_000_000.0;

            System.out.println();
            System.out.println("====================================");
            System.out.println("       LUCENE SEARCH BENCHMARK");
            System.out.println("====================================");

            System.out.println(
                    "Indexed documents : "
                            + reader.numDocs()
            );

            System.out.println(
                    "Query             : "
                            + "level:ERROR AND "
                            + "service:billing-api AND "
                            + "response_time > 1000"
            );

            System.out.println(
                    "Results returned  : "
                            + results.scoreDocs.length
            );

            System.out.println(
                    "Search time       : "
                            + String.format(
                            "%.4f",
                            searchTimeMs
                    )
                            + " ms"
            );

            System.out.println("====================================");

            System.out.println();
            System.out.println("Sample matching logs:");
            System.out.println("------------------------------------");

            int printed = 0;

            for (ScoreDoc scoreDoc :
                    results.scoreDocs) {

                Document document =
                        searcher.storedFields()
                                .document(scoreDoc.doc);

                System.out.println(
                        document.get("level")
                                + " | "
                                + document.get("service")
                                + " | "
                                + document.get("message")
                                + " | response_time="
                                + document.get("response_time")
                );

                printed++;

                if (printed >= 10) {
                    break;
                }
            }

            System.out.println("------------------------------------");
        }

        directory.close();
    }
}