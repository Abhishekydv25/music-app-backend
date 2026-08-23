package com.musicapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.storage.local-path}")
    private String storagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = new File(storagePath).getAbsolutePath();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath + File.separator);
    }
}
