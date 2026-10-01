package br.com.instagrambot.scheduler;

import br.com.instagrambot.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class DailyPostScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailyPostScheduler.class);

    private final PostService postService;

    public DailyPostScheduler(PostService postService) {
        this.postService = postService;
    }

    @Scheduled(cron = "${app.schedule.cron}", zone = "${app.schedule.zone}")
    public void publishDaily() {
        try {
            log.info("Iniciando rotina diária de publicação...");
            postService.publishDailyIfNeeded().ifPresentOrElse(
                    id -> log.info("Rotina concluída. Instagram Media ID: {}", id),
                    () -> log.info("Rotina concluída sem nova postagem.")
            );
        } catch (Exception e) {
            log.error("Falha na rotina automática de publicação: {}", e.getMessage(), e);
        }
    }
}
