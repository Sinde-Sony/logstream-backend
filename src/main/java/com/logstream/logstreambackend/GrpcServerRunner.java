package com.logstream.logstreambackend;

import com.logstream.grpc.LogIngestionServiceImpl;
import com.logstream.lucene.LogIndexService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class GrpcServerRunner {

    private Server server;

    private final LogIndexService logIndexService;

    public GrpcServerRunner(LogIndexService logIndexService) {
        this.logIndexService = logIndexService;
    }

    @PostConstruct
    public void startGrpcServer() throws Exception {

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
        System.out.println("Integrated with Spring Boot");
        System.out.println("====================================");
    }

    @PreDestroy
    public void stopGrpcServer() {

        if (server != null) {
            server.shutdown();
        }
    }
}