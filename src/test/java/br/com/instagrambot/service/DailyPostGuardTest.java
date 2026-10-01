package br.com.instagrambot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DailyPostGuardTest {
    @TempDir Path directory;

    private DailyPostGuard guard() {
        DailyPostGuard guard = new DailyPostGuard();
        ReflectionTestUtils.setField(guard, "stateFile", directory.resolve("posts.txt").toString());
        return guard;
    }

    @Test void allowsTwoSlotsAndKeepsTheirHistoryAcrossRestarts() {
        var morning = LocalDateTime.parse("2026-10-02T11:00");
        var evening = LocalDateTime.parse("2026-10-02T17:00");
        var guard = guard();
        assertFalse(guard.wasPublished(morning));
        guard.markPublished(morning);
        assertTrue(guard().wasPublished(morning));
        assertFalse(guard().wasPublished(evening));
        guard.markPublished(evening);
        assertTrue(guard().wasPublished(morning));
        assertTrue(guard().wasPublished(evening));
        assertFalse(guard().wasPublished(morning.plusDays(1)));
    }

    @Test void respectsLegacyDateAndAppendsWithoutCorruptingIt() throws Exception {
        Files.writeString(directory.resolve("posts.txt"), "2026-10-01");
        var guard = guard();
        assertTrue(guard.wasPublished(LocalDateTime.parse("2026-10-01T11:00")));
        assertTrue(guard.wasPublished(LocalDateTime.parse("2026-10-01T17:00")));
        var next = LocalDateTime.parse("2026-10-02T11:00");
        assertFalse(guard.wasPublished(next));
        guard.markPublished(next);
        assertTrue(guard().wasPublished(next));
        assertTrue(guard().wasPublished(LocalDateTime.parse("2026-10-01T17:00")));
    }
}
