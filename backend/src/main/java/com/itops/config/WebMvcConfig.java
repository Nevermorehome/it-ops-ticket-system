package com.itops.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置: CORS、本地上传目录静态映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${itops.upload.path}")
    private String uploadPath;

    @Value("${itops.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        if (!location.startsWith("file:")) {
            location = "file:" + location;
        }
        registry.addResourceHandler(urlPrefix + "/**").addResourceLocations(location);
    }
}
