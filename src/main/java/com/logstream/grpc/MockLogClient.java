package com.logstream.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class MockLogClient {

    public static void main(String[] args) {

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        LogIngestionServiceGrpc.LogIngestionServiceBlockingStub stub =
                LogIngestionServiceGrpc.newBlockingStub(channel);

        LogMessage log = LogMessage.newBuilder()
                .setTimestamp(String.valueOf(System.currentTimeMillis()))
                .setLevel("ERROR")
                .setService("billing-api")
                .setMessage("Database connection timeout")
                .setResponseTime(1520)
                .setTraceId("trace-001")
                .build();

        System.out.println("Sending log...");

        LogResponse response = stub.sendLog(log);

        System.out.println("Server response: " + response.getMessage());

        channel.shutdown();
    }
}