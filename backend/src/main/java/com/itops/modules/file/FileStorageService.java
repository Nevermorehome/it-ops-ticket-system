package com.itops.modules.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.itops.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * 本地文件存储
 */
@Slf4j
@Service
public class FileStorageService {

    @Value("${itops.upload.path}")
    private String basePath;

    @Value("${itops.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Value("${itops.upload.max-size-mb:10}")
    private long maxSizeMb;

    @Value("${itops.upload.allowed-types}")
    private String allowedTypes;

    @PostConstruct
    public void init() {
        File dir = new File(basePath);
        if (!dir.exists() && dir.mkdirs()) {
            log.info("创建上传目录: {}", dir.getAbsolutePath());
        }
    }

    public UploadResult store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.of("上传文件不能为空");
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String ext = extOf(original);

        if (!allowedExts().contains(ext.toLowerCase(Locale.ROOT))) {
            throw BusinessException.of("不支持的文件类型: " + ext);
        }
        if (file.getSize() > maxSizeMb * 1024 * 1024) {
            throw BusinessException.of("文件大小超过限制 " + maxSizeMb + "MB");
        }

        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = IdUtil.fastSimpleUUID() + (ext.isEmpty() ? "" : "." + ext);
        File target = new File(new File(basePath, datePart), fileName);
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            throw BusinessException.of("创建存储目录失败");
        }
        try {
            file.transferTo(target);
        } catch (IOException e) {
            log.error("文件保存失败", e);
            throw BusinessException.of("文件保存失败: " + e.getMessage());
        }

        UploadResult result = new UploadResult();
        result.setFileName(original);
        result.setUrl(normalizePrefix() + "/" + datePart + "/" + fileName);
        result.setSize(file.getSize());
        result.setFileType(ext);
        result.setStoredPath(target.getAbsolutePath());
        return result;
    }

    public File resolve(String relativePath) {
        return new File(basePath, relativePath);
    }

    private List<String> allowedExts() {
        return Arrays.stream(allowedTypes.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .toList();
    }

    private String extOf(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx + 1).toLowerCase(Locale.ROOT) : "";
    }

    private String normalizePrefix() {
        String p = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
        return p.startsWith("/") ? p : "/" + p;
    }

    @lombok.Data
    public static class UploadResult {
        private String fileName;
        private String url;
        private long size;
        private String fileType;
        private String storedPath;
    }
}
