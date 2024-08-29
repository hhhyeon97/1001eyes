package com.demo.orderservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SlackNotifierService {

    private final WebClient webClient;
    private final String webhookUrl;

    public SlackNotifierService(@Value("${slack.webhook.url}") String webhookUrl) {
        this.webClient = WebClient.builder().build();
        this.webhookUrl = webhookUrl;
    }

    public void sendNotification(String message) {
        Map<String, String> payload = new HashMap<>();
        payload.put("text", message);

        webClient.post()
                .uri(webhookUrl)
                .body(Mono.just(payload), Map.class)
                .retrieve()
                .bodyToMono(String.class)
                .doOnError(e -> log.error("Failed to send Slack notification", e))
                .subscribe(response -> log.info("Slack notification sent successfully: {}", response));
    }
}