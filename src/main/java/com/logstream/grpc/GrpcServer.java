package com.logstream.grpc;

import com.logstream.lucene.LogIndexService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class GrpcServer {

    private Server server;

    public void start() throws IOException {

        // One shared Lucene index
        LogIndexService logIndexService =
                new LogIndexService();

        server = ServerBuilder
                .forPort(9090)
                .addService(
                        new LogIngestionServiceImpl(logIndexService)
                )
                .build()
                .start();

        System.out.println("====================================");
        System.out.println("     LOGSTREAM gRPC SERVER STARTED");
        System.out.println("====================================");
        System.out.println("Listening on port: 9090");
        System.out.println("Lucene index ready");
        System.out.println("====================================");

        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    System.out.println(
                            "Shutting down gRPC server..."
                    );

                    GrpcServer.this.stop();

                    try {
                        logIndexService.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
        );
    }

    public void stop() {

        if (server != null) {
            server.shutdown();
        }
    }

    public static void main(String[] args)
            throws IOException, InterruptedException {

        GrpcServer grpcServer =
                new GrpcServer();

        grpcServer.start();

        grpcServer.server.awaitTermination();
    }
}