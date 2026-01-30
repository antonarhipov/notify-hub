package com.jetbrains.notifyhub;

import com.jetbrains.notifyhub.service.NotificationSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class NotifyHubApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(NotifyHubApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("========================================");
        log.info("   NotifyHub Application Started");
        log.info("========================================");
    }

}
