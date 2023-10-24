package com.exam.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class MultipartConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        // Set max file size
        factory.setMaxFileSize(DataSize.ofMegabytes(128));

        // Set max request size
        factory.setMaxRequestSize(DataSize.ofMegabytes(128));

        return factory.createMultipartConfig();
    }
}
