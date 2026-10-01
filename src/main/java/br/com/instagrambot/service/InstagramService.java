package br.com.instagrambot.service;

import br.com.instagrambot.dto.InstagramContainerResponse;
import br.com.instagrambot.dto.InstagramContainerStatusResponse;
import br.com.instagrambot.dto.InstagramPublishResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

@Service
public class InstagramService {

    private static final Logger log = LoggerFactory.getLogger(InstagramService.class);

    private final RestClient restClient;

    @Value("${instagram.base-url}")
    private String baseUrl;

    @Value("${instagram.api-version}")
    private String apiVersion;

    @Value("${instagram.user-id}")
    private String userId;

    @Value("${instagram.access-token}")
    private String accessToken;

    public InstagramService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String publishImage(String imageUrl, String caption) {
        String containerId = createMediaContainer(imageUrl, caption);
        waitUntilContainerIsReady(containerId);
        return publishContainer(containerId);
    }

    private String createMediaContainer(String imageUrl, String caption) {
        Map<String, String> body = Map.of(
                "image_url", imageUrl,
                "caption", caption
        );

        InstagramContainerResponse response;
        try {
            response = restClient.post()
                    .uri(apiBase() + "/" + userId + "/media")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(InstagramContainerResponse.class);
        } catch (RestClientResponseException e) {
            throw metaException("criar o container de mídia", e);
        }

        if (response == null || response.id() == null || response.id().isBlank()) {
            throw new IllegalStateException("O Instagram não retornou o ID do container de mídia.");
        }

        log.info("Container do Instagram criado: {}", response.id());
        return response.id();
    }

    private void waitUntilContainerIsReady(String containerId) {
        String url = apiBase() + "/" + containerId + "?fields=status_code,status";

        for (int attempt = 1; attempt <= 20; attempt++) {
            InstagramContainerStatusResponse response;
            try {
                response = restClient.get()
                        .uri(url)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve()
                        .body(InstagramContainerStatusResponse.class);
            } catch (RestClientResponseException e) {
                throw metaException("consultar o status do container", e);
            }

            if (response == null || response.statusCode() == null) {
                throw new IllegalStateException("O Instagram não retornou o status do container.");
            }

            log.info("Status do container: {} (tentativa {}/20)", response.statusCode(), attempt);

            if ("FINISHED".equalsIgnoreCase(response.statusCode())) {
                return;
            }

            if ("ERROR".equalsIgnoreCase(response.statusCode())
                    || "EXPIRED".equalsIgnoreCase(response.statusCode())) {
                throw new IllegalStateException(
                        "O Instagram não conseguiu processar o container. status_code="
                                + response.statusCode() + ", status=" + response.status()
                );
            }

            sleep(3000);
        }

        throw new IllegalStateException("O Instagram demorou mais de 60 segundos para processar a imagem.");
    }

    private String publishContainer(String containerId) {
        Map<String, String> body = Map.of("creation_id", containerId);

        InstagramPublishResponse response;
        try {
            response = restClient.post()
                    .uri(apiBase() + "/" + userId + "/media_publish")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(InstagramPublishResponse.class);
        } catch (RestClientResponseException e) {
            throw metaException("publicar o container", e);
        }

        if (response == null || response.id() == null || response.id().isBlank()) {
            throw new IllegalStateException("O Instagram não retornou o ID da publicação.");
        }

        log.info("Publicação concluída no Instagram. Media ID: {}", response.id());
        return response.id();
    }

    private String apiBase() {
        String normalized = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        return normalized + "/" + apiVersion;
    }

    private IllegalStateException metaException(String action, RestClientResponseException e) {
        String body = e.getResponseBodyAsString();
        if (body != null && body.length() > 1200) {
            body = body.substring(0, 1200) + "...";
        }
        return new IllegalStateException(
                "Erro da API do Instagram ao " + action + " (HTTP " + e.getStatusCode().value() + "): " + body,
                e
        );
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("A espera pelo processamento do Instagram foi interrompida.", e);
        }
    }
}
