package br.com.instagrambot.service;

import br.com.instagrambot.dto.OpenAiImageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class OpenAiImageService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiImageService.class);

    private final RestClient restClient;
    private final TextFileService textFileService;
    private final BrandingService brandingService;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.base-url}")
    private String baseUrl;

    @Value("${openai.image-model}")
    private String model;

    @Value("${openai.image-size}")
    private String size;

    @Value("${openai.image-quality}")
    private String quality;

    @Value("${app.image-dir}")
    private String imageDir;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    public OpenAiImageService(RestClient restClient, TextFileService textFileService, BrandingService brandingService) {
        this.restClient = restClient;
        this.textFileService = textFileService;
        this.brandingService = brandingService;
    }

    public String generateAndStoreImage() {
        String prompt = textFileService.readPrompt();
        log.info("Solicitando imagem à OpenAI usando o modelo {}...", model);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("prompt", prompt);
        body.put("size", size);
        body.put("quality", quality);
        body.put("output_format", "jpeg");
        body.put("output_compression", 90);
        body.put("n", 1);

        OpenAiImageResponse response;
        try {
            response = restClient.post()
                    .uri(normalizeBaseUrl(baseUrl) + "/v1/images/generations")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(OpenAiImageResponse.class);
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "Erro da OpenAI (HTTP " + e.getStatusCode().value() + "): " + safeBody(e.getResponseBodyAsString()),
                    e
            );
        }

        if (response == null || response.data() == null || response.data().isEmpty()
                || response.data().getFirst().b64Json() == null
                || response.data().getFirst().b64Json().isBlank()) {
            throw new IllegalStateException("A OpenAI não retornou os dados da imagem.");
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(response.data().getFirst().b64Json());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("A OpenAI retornou uma imagem Base64 inválida.", e);
        }

        String fileName = "post-" + LocalDate.now() + "-" + UUID.randomUUID() + ".jpg";
        saveImage(fileName, brandingService.applyOfficialLogo(imageBytes));

        String imageUrl = stripTrailingSlash(publicBaseUrl) + "/generated/" + fileName;
        log.info("Imagem gerada e salva. URL pública esperada: {}", imageUrl);
        return imageUrl;
    }

    private void saveImage(String fileName, byte[] imageBytes) {
        try {
            Path directory = Path.of(imageDir).toAbsolutePath().normalize();
            Files.createDirectories(directory);
            Files.write(directory.resolve(fileName), imageBytes);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível salvar a imagem gerada.", e);
        }
    }

    private String normalizeBaseUrl(String value) {
        return stripTrailingSlash(value);
    }

    private String stripTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String safeBody(String body) {
        if (body == null) {
            return "sem corpo de resposta";
        }
        return body.length() > 1200 ? body.substring(0, 1200) + "..." : body;
    }
}
