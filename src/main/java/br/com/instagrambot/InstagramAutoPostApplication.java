package br.com.instagrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.system.ApplicationHome;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class InstagramAutoPostApplication {

    public static void main(String[] args) {
        Path directory = applicationDirectory();
        SpringApplication app = new SpringApplication(InstagramAutoPostApplication.class);
        app.setDefaultProperties(Map.of(
                "app.base-dir", directory.toString().replace('\\', '/'),
                "app.config-file", directory.resolve("config/secrets.properties").toUri().toString()
        ));
        app.run(args);
    }

    private static Path applicationDirectory() {
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        if (Files.isDirectory(workingDirectory.resolve("config"))) return workingDirectory;
        Path directory = new ApplicationHome(InstagramAutoPostApplication.class)
                .getDir().toPath().toAbsolutePath().normalize();
        while (directory != null) {
            if (Files.isDirectory(directory.resolve("config"))) return directory;
            directory = directory.getParent();
        }
        return workingDirectory;
    }
}
