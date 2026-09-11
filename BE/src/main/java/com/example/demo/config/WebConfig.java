package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String storageRoot;

    public WebConfig(@Value("${app.storage.root}") String storageRoot) {
        this.storageRoot = storageRoot;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // Espone la cartella di storage locale su /files/**, cosi' il
    // frontend puo' mettere il percorso restituito nelle risposte
    // (es. "/files/posts/2026/09/xxx.jpg") direttamente in <img src>.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absoluteRoot = Path.of(storageRoot).toAbsolutePath().normalize();
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + absoluteRoot + "/");
    }
}
