package com.wyx.enrollment_promotion_systemmaster.controller;

import com.wyx.enrollment_promotion_systemmaster.dto.ApiResponse;
import com.wyx.enrollment_promotion_systemmaster.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private FileUtils fileUtils;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "mp4", "avi", "mov"
    );

    @PostMapping("/upload")
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null) {
                String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                    return ApiResponse.error("File type not allowed");
                }
            }

            String filepath = fileUtils.uploadFile(file);
            return ApiResponse.success(filepath);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error("File upload failed: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ApiResponse<Void> deleteFile(@RequestParam String filepath) {
        boolean success = fileUtils.deleteFile(filepath);
        if (success) {
            return ApiResponse.success();
        } else {
            return ApiResponse.error("File deletion failed");
        }
    }
}
