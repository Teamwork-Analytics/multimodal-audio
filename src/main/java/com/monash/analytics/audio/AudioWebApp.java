package com.monash.analytics.audio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class AudioWebApp {
    public static void main(String[] args) {
        System.out.println(System.getProperty("java.library.path"));

        SpringApplication.run(AudioWebApp.class, args);
    }
}
