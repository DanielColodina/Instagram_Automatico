package br.com.instagrambot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.nio.file.StandardOpenOption;

@Service
public class DailyPostGuard {

    @Value("${app.state-file}")
    private String stateFile;

    public boolean wasPublished(LocalDateTime slot) {
        Path path = Path.of(stateFile);
        if (!Files.exists(path)) {
            return false;
        }

        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .map(String::trim)
                    .anyMatch(value -> slot.toString().equals(value)
                            || slot.toLocalDate().toString().equals(value));
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível ler o controle da última publicação.", e);
        }
    }

    public void markPublished(LocalDateTime slot) {
        Path path = Path.of(stateFile);
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.writeString(path, System.lineSeparator() + slot + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("A publicação foi feita, mas não foi possível salvar o controle diário.", e);
        }
    }
}
