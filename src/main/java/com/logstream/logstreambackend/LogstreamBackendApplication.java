package com.logstream.logstreambackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ComponentScan("com.logstream")
@EnableScheduling
public class LogstreamBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(
                LogstreamBackendApplication.class,
                args
        );
    }
}