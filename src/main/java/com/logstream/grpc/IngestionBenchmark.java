package com.logstream.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class IngestionBenchmark {

    public static void main(String[] args) {

        ManagedChannel channel =
                ManagedChannelBuilder
                        .forAddress("localhost", 9090)
                        .usePlaintext()
                        .build();

        LogIngestionServiceGrpc.LogIngestionServiceBlockingStub stub =
                LogIngestionServiceGrpc.newBlockingStub(channel);

        int totalLogs = 10_000;

        System.out.println("====================================");
        System.out.println("       LOG INGESTION BENCHMARK");
        System.out.println("====================================");

        long start = System.nanoTime();

        try {

            for (int i = 1; i <= totalLogs; i++) {

                String level;
                String service;
                String message;
                long responseTime;

                // ERROR logs: 5% of total
                if (i % 20 == 0) {

                    level = "ERROR";
                    service = "billing-api";
                    message = "Database connection failed";
                    responseTime = 1500;

                    // WARN logs: 5% of total
                } else if (i % 10 == 0) {

                    level = "WARN";
                    service = "payment-api";
                    message = "Slow database response";
                    responseTime = 800;

                    // INFO logs: remaining
                } else {

                    level = "INFO";
                    service = "billing-api";
                    message = "Application log message " + i;
                    responseTime = 100 + (i % 300);
                }

                LogMessage logMessage =
                        LogMessage.newBuilder()
                                .setService(service)
                                .setLevel(level)
                                .setMessage(message)
                                .setTraceId("trace-" + i)
                                .setResponseTime(responseTime)
                                .setTimestamp(
                                        String.valueOf(
                                                System.currentTimeMillis()
                                        )
                                )
                                .build();

                stub.sendLog(logMessage);
            }

        } finally {

            channel.shutdown();
        }

        long end = System.nanoTime();

        double seconds =
                (end - start) / 1_000_000_000.0;

        double throughput =
                totalLogs / seconds;

        System.out.println();
        System.out.println("====================================");
        System.out.println("          BENCHMARK RESULT");
        System.out.println("====================================");
        System.out.println(
                "Total logs      : " + totalLogs
        );
        System.out.println(
                "Time taken      : "
                        + String.format("%.3f", seconds)
                        + " seconds"
        );
        System.out.println(
                "Throughput      : "
                        + String.format("%.2f", throughput)
                        + " logs/sec"
        );
        System.out.println("====================================");
    }
}