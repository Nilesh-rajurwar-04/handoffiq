package com.handoffiq.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

/**
 * Single Page Application (SPA) WebMvc configuration.
 * Seamlessly resolves React Router client-side routes when the frontend bundle
 * is packaged inside Spring Boot's classpath:/static directory (Unified Full-Stack Deployment).
 */
@Configuration
public class SpaWebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requested = location.createRelative(resourcePath);
                        if (requested.exists() && requested.isReadable()) {
                            return requested;
                        }
                        // Do not route API, Swagger, or Actuator paths to index.html
                        if (resourcePath.startsWith("api/") 
                                || resourcePath.startsWith("swagger-ui") 
                                || resourcePath.startsWith("v3/api-docs") 
                                || resourcePath.startsWith("actuator")) {
                            return null;
                        }
                        Resource indexHtml = new ClassPathResource("/static/index.html");
                        return (indexHtml.exists() && indexHtml.isReadable()) ? indexHtml : null;
                    }
                });
    }
}
