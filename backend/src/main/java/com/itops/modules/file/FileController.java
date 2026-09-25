package com.itops.modules.file;

import com.itops.common.api.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "文件上传")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService storageService;

    @Operation(summary = "单文件上传(图片/文档)")
    @PostMapping("/upload")
    public R<FileStorageService.UploadResult> upload(@RequestParam("file") MultipartFile file) {
        return R.ok(storageService.store(file));
    }
}
