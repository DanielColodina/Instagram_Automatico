package br.com.instagrambot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.ZoneId;
import java.util.Optional;

@Service
public class PostService {

    private static final Logger log = LoggerFactory.getLogger(PostService.class);

    private final OpenAiImageService imageService;
    private final InstagramService instagramService;
    private final TextFileService textFileService;
    private final DailyPostGuard dailyPostGuard;

    @Value("${app.schedule.zone}")
    private String scheduleZone;

    public PostService(
            OpenAiImageService imageService,
            InstagramService instagramService,
            TextFileService textFileService,
            DailyPostGuard dailyPostGuard
    ) {
        this.imageService = imageService;
        this.instagramService = instagramService;
        this.textFileService = textFileService;
        this.dailyPostGuard = dailyPostGuard;
    }

    public synchronized Optional<String> publishDailyIfNeeded() {
        LocalDateTime slot = LocalDateTime.now(ZoneId.of(scheduleZone)).truncatedTo(ChronoUnit.HOURS);

        if (dailyPostGuard.wasPublished(slot)) {
            log.info("Já existe publicação registrada para o horário {}. Nada será publicado novamente.", slot);
            return Optional.empty();
        }

        String postId = publishNow();
        dailyPostGuard.markPublished(slot);
        return Optional.of(postId);
    }

    public synchronized String publishNow() {
        String imageUrl = imageService.generateAndStoreImage();
        String caption = textFileService.readCaption();

        log.info("Enviando imagem para o Instagram...");
        return instagramService.publishImage(imageUrl, caption);
    }
}
