package com.itops.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * Web MVC 配置: CORS、本地上传目录静态映射
 */
@Slf4j
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${itops.upload.path}")
    private String uploadPath;

    @Value("${itops.upload.url-prefix:/uploads}")
    private String urlPrefix;

    /**
     * 允许的跨域来源, 逗号分隔, 如 https://ops.example.com
     * 为 * 时放行任意来源(仅建议开发环境); 为空时不注册 CORS(同源经 Nginx 反代)
     */
    @Value("${itops.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (origins.length == 0) {
            log.info("CORS 未配置允许来源, 仅支持同源访问");
            return;
        }
        CorsRegistration reg = registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition")
                .maxAge(3600);
        boolean wildcard = origins.length == 1 && "*".equals(origins[0]);
        if (wildcard) {
            // 通配来源下不允许携带凭证, 二者不能在浏览器规范下同时精确生效
            reg.allowedOriginPatterns("*");
            log.warn("CORS 允许任意来源(*), 请勿在生产环境使用");
        } else {
            reg.allowedOrigins(origins);
            log.info("CORS 允许来源: {}", String.join(", ", origins));
        }
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
