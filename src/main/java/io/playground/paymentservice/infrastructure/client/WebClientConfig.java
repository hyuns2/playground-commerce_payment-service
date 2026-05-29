package io.playground.paymentservice.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Configuration
public class WebClientConfig {
    @Value("${api.external.toss-payments.url}")
    private String baseUrl;
    @Value("${api.external.toss-payments.grant-type}")
    private String grantType;
    @Value("${api.external.toss-payments.secret-key}")
    private String secretKey;

    @Bean
    public WebClient toss() {
        String authorizationValue = grantType + " " +
                Base64.getEncoder()
                        .encodeToString((secretKey + ":").getBytes());

        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> {
                    headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
                    headers.add(HttpHeaders.AUTHORIZATION, authorizationValue);
                })
                .build();
    }
}
