package com.logstream.grpc;

import com.logstream.lucene.LogIndexService;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

public class LogIngestionServiceImpl
        extends LogIngestionServiceGrpc.LogIngestionServiceImplBase {

    private final AtomicLong totalLogsReceived = new AtomicLong(0);

    private final LogIndexService logIndexService;

    public LogIngestionServiceImpl(
            LogIndexService logIndexService) {

        this.logIndexService = logIndexService;
    }

    @Override
    public void sendLog(
            LogMessage request,
            StreamObserver<LogResponse> responseObserver) {

        try {

            // 1. Receive the log through gRPC
            long count = totalLogsReceived.incrementAndGet();

            // 2. Index the log into Apache Lucene
            logIndexService.indexLog(request);

            // 3. Commit periodically
            if (count % 100 == 0) {
                logIndexService.commit();
            }

            // Print progress
            if (count % 1000 == 0) {
                System.out.println(
                        "Logs received and indexed: " + count
                );
            }

            // 4. Send response to client
            LogResponse response = LogResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Log received and indexed successfully")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {

            e.printStackTrace();

            LogResponse response = LogResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to index log: " + e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    public long getTotalLogsReceived() {
        return totalLogsReceived.get();
    }
}