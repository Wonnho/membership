package com.membership.member.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.nio.file.*;
import java.io.IOException;
import java.util.*;

@Service
public class CoffeeImageService {
    private final Path directory;
    public CoffeeImageService(@Value("${coffee.upload-dir:uploads/coffee}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    public String upload(MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getSize() > 5 * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a JPG or PNG up to 5 MB.");
        }
        try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image.");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!Set.of("jpeg", "png").contains(format)
                        || (long) reader.getWidth(0) * reader.getHeight(0) > 20000000) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a JPG or PNG under 20 megapixels.");
                }
                var decoded = reader.read(0);
                Files.createDirectories(directory);
                String name = UUID.randomUUID() + (format.equals("jpeg") ? ".jpg" : ".png");
                ImageIO.write(decoded, format, directory.resolve(name).toFile());
                return "/coffee-images/" + name;
            } finally {
                reader.dispose();
            }
        }
    }

    public Path resolve(String name) {
        if (!name.matches("[a-f0-9-]{36}\\.(jpg|png)")) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        Path path = directory.resolve(name).normalize();
        if (!path.startsWith(directory) || !Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return path;
    }

    public boolean isValid(String image) {
        if (List.of("/images/coffee/americano.jpg", "/images/coffee/coffee-latte.jpg",
                "/images/coffee/cafe-mocha.jpg").contains(image)) return true;
        if (!image.startsWith("/coffee-images/")) return false;
        try {
            resolve(image.substring("/coffee-images/".length()));
            return true;
        } catch (ResponseStatusException ex) {
            return false;
        }
    }
}
