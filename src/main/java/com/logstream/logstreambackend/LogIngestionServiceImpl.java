package com.logstream.grpc;

import com.logstream.lucene.LogIndexService;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.atomic.AtomicLong;
import com.logstream.logstreambackend.LogWebSocketHandler;
public class LogIngestionServiceImpl
        extends LogIngestionServiceGrpc.LogIngestionServiceImplBase {

    private final AtomicLong totalLogsReceived = new AtomicLong(0);
    private final LogIndexService logIndexService;

    public LogIngestionServiceImpl(LogIndexService logIndexService) {
        this.logIndexService = logIndexService;
    }

    @Override
    public void sendLog(
            LogMessage request,
            StreamObserver<LogResponse> responseObserver) {

        try {
            long count = totalLogsReceived.incrementAndGet();

            logIndexService.indexLog(request);
            String liveLog =
                    "[" + request.getLevel() + "] "
                            + request.getService()
                            + " - "
                            + request.getMessage();

            LogWebSocketHandler.broadcastLog(liveLog);

            logIndexService.commit();

            if (count % 1000 == 0) {
                System.out.println(
                        "Logs received and indexed: " + count
                );
            }

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