package br.com.instagrambot.controller;

import br.com.instagrambot.service.PostService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dev")
@Profile("dev")
public class DevPostController {

    private final PostService postService;

    public DevPostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping("/post-now")
    public ResponseEntity<Map<String, String>> postNow() {
        String id = postService.publishNow();
        return ResponseEntity.ok(Map.of(
                "status", "PUBLICADO",
                "instagramMediaId", id
        ));
    }
}
