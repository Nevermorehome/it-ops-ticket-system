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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
        String contentType = file.getContentType();
        if (contentType != null && !isContentTypeConsistent(ext.toLowerCase(Locale.ROOT), contentType.toLowerCase(Locale.ROOT))) {
            log.warn("上传文件类型与内容类型不匹配: ext={} contentType={} file={}", ext, contentType, original);
            throw BusinessException.of("文件内容类型与扩展名不匹配");
        }
        // 文件头魔数校验: 防止脚本/可执行文件改名为图片或文档
        checkMagicBytes(ext.toLowerCase(Locale.ROOT), file, original);

        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = IdUtil.fastSimpleUUID() + (ext.isEmpty() ? "" : "." + ext);
        File target = new File(new File(basePath, datePart), fileName);
        if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
            log.error("创建存储目录失败: {}", target.getParentFile().getAbsolutePath());
            throw BusinessException.server("文件存储服务异常，请联系管理员");
        }
        try {
            // 不能用 MultipartFile.transferTo(相对路径 File): Tomcat 会按 multipart 临时目录解析;
            // 这里用绝对路径 + NIO 拷贝, 行为与容器无关
            Files.copy(file.getInputStream(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("文件保存失败: {}", target.getAbsolutePath(), e);
            throw BusinessException.server("文件保存失败，请稍后重试");
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

    /**
     * 校验客户端声明的 Content-Type 与扩展名是否一致(防止将脚本改名为图片上传)。
     * application/octet-stream(部分客户端/系统默认) 与空值放行, 以扩展名校验为准。
     */
    private boolean isContentTypeConsistent(String ext, String contentType) {
        if (contentType.isEmpty() || "application/octet-stream".equals(contentType)) {
            return true;
        }
        boolean imageExt = Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp").contains(ext);
        if (imageExt) {
            return contentType.startsWith("image/");
        }
        return switch (ext) {
            case "pdf" -> "application/pdf".equals(contentType);
            case "doc", "docx", "xls", "xlsx" ->
                    contentType.startsWith("application/msword")
                            || contentType.startsWith("application/vnd.openxmlformats-officedocument")
                            || contentType.startsWith("application/vnd.ms-excel");
            default -> true;
        };
    }

    /**
     * 读取文件头校验魔数(只读前 12 字节; MultipartFile 每次返回独立输入流, 不影响后续落盘)
     */
    private void checkMagicBytes(String ext, MultipartFile file, String original) {
        boolean mustCheck = Arrays.asList(
                "jpg", "jpeg", "png", "gif", "bmp", "webp", "pdf", "docx", "xlsx").contains(ext);
        if (!mustCheck) {
            // doc/xls 等旧版 OLE 格式不做强校验
            return;
        }
        byte[] head = new byte[12];
        int len;
        try (var in = file.getInputStream()) {
            len = in.read(head);
        } catch (IOException e) {
            log.error("读取上传文件头失败: {}", original, e);
            throw BusinessException.server("文件读取失败，请稍后重试");
        }
        boolean ok = switch (ext) {
            case "jpg", "jpeg" -> len >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF;
            case "png" -> len >= 8
                    && (head[0] & 0xFF) == 0x89 && head[1] == 'P' && head[2] == 'N' && head[3] == 'G'
                    && (head[4] & 0xFF) == 0x0D && (head[5] & 0xFF) == 0x0A;
            case "gif" -> len >= 4 && head[0] == 'G' && head[1] == 'I' && head[2] == 'F' && head[3] == '8';
            case "bmp" -> len >= 2 && head[0] == 'B' && head[1] == 'M';
            case "webp" -> len >= 12 && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                    && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P';
            case "pdf" -> len >= 5 && head[0] == '%' && head[1] == 'P' && head[2] == 'D' && head[3] == 'F' && head[4] == '-';
            // docx/xlsx 本质是 zip
            case "docx", "xlsx" -> len >= 4 && (head[0] & 0xFF) == 'P' && (head[1] & 0xFF) == 'K'
                    && (head[2] & 0xFF) == 0x03 && (head[3] & 0xFF) == 0x04;
            default -> true;
        };
        if (!ok) {
            log.warn("上传文件魔数校验失败: ext={} file={}", ext, original);
            throw BusinessException.of("文件内容与格式不匹配，请上传真实的 " + ext + " 文件");
        }
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
