package com.membership.member.controller;

import com.membership.member.Service.CoffeeImageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

@RestController
public class CoffeeImageController {

    private final CoffeeImageService coffeeImages;

    public CoffeeImageController(CoffeeImageService coffeeImages) {
        this.coffeeImages = coffeeImages;
    }

    @PostMapping("/coffeeboard/upload")
    public Map<String, String> uploadCoffeeImage(@RequestParam("file") MultipartFile file) throws IOException {
        return Map.of("image", coffeeImages.upload(file));
    }

    @GetMapping("/coffee-images/{name}")
    public ResponseEntity<Resource> coffeeImage(@PathVariable("name") String name) {
        Path path = coffeeImages.resolve(name);
        return ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .contentType(name.endsWith(".png") ? MediaType.IMAGE_PNG : MediaType.IMAGE_JPEG)
                .body(new FileSystemResource(path));
    }
}
