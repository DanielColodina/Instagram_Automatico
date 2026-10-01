package br.com.instagrambot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Path;

@Service
public class BrandingService {
    private final Path logoFile;

    public BrandingService(@Value("${app.logo-file}") String logoFile) {
        this.logoFile = Path.of(logoFile);
    }

    public byte[] applyOfficialLogo(byte[] imageBytes) {
        try {
            BufferedImage artwork = ImageIO.read(new ByteArrayInputStream(imageBytes));
            BufferedImage logo = ImageIO.read(logoFile.toFile());
            if (artwork == null || logo == null) throw new IOException("Imagem ou logo inválida");
            int width = artwork.getWidth(), height = artwork.getHeight();
            int padding = Math.max(16, width / 54);
            int logoSize = Math.min(192, width / 6);
            int footer = logoSize + padding * 2;
            if (height <= footer) throw new IOException("Imagem pequena demais para a marca");
            BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = output.createGraphics();
            try {
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, width, height);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                double scale = Math.min((double) width / artwork.getWidth(), (double) (height - footer) / artwork.getHeight());
                int aw = (int) Math.round(artwork.getWidth() * scale), ah = (int) Math.round(artwork.getHeight() * scale);
                g.drawImage(artwork, (width - aw) / 2, 0, aw, ah, null);
                double logoScale = Math.min((double) logoSize / logo.getWidth(), (double) logoSize / logo.getHeight());
                int lw = (int) Math.round(logo.getWidth() * logoScale), lh = (int) Math.round(logo.getHeight() * logoScale);
                g.drawImage(logo, (width - lw) / 2, height - footer + (footer - lh) / 2, lw, lh, null);
            } finally {
                g.dispose();
            }
            ByteArrayOutputStream encoded = new ByteArrayOutputStream();
            if (!ImageIO.write(output, "jpg", encoded)) throw new IOException("Codificador JPEG indisponível");
            return encoded.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível aplicar a logo oficial; publicação interrompida.", e);
        }
    }
}
