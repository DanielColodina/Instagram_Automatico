package br.com.instagrambot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TextFileService {

    @Value("${app.prompt-file}")
    private String promptFile;

    @Value("${app.caption-file}")
    private String captionFile;

    public String readPrompt() {
        return readRequiredFile(promptFile, "prompt");
    }

    public String readCaption() {
        return readRequiredFile(captionFile, "legenda");
    }

    private String readRequiredFile(String fileName, String description) {
        try {
            Path path = Path.of(fileName);
            String content = Files.readString(path, StandardCharsets.UTF_8).trim();

            if (content.isBlank()) {
                throw new IllegalStateException("O arquivo de " + description + " está vazio: " + path);
            }

            return content;
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o arquivo de " + description + ": " + fileName, e);
        }
    }
}
