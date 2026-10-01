package br.com.instagrambot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupValidator implements ApplicationRunner {

    @Value("${openai.api-key:}")
    private String openAiKey;

    @Value("${instagram.access-token:}")
    private String instagramToken;

    @Value("${instagram.user-id:}")
    private String instagramUserId;

    @Value("${app.public-base-url:}")
    private String publicBaseUrl;

    @Override
    public void run(ApplicationArguments args) {
        validate("openai.api-key", openAiKey);
        validate("instagram.access-token", instagramToken);
        validate("instagram.user-id", instagramUserId);
        validate("app.public-base-url", publicBaseUrl);
    }

    private void validate(String name, String value) {
        if (value == null || value.isBlank() || value.contains("COLE_") || value.contains("SEU-DOMINIO")) {
            throw new IllegalStateException(
                    "Configure '" + name + "' no arquivo config/secrets.properties antes de iniciar."
            );
        }
    }
}
