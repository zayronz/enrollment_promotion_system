package com.wyx.enrollment_promotion_systemmaster.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class FileUtils {

    @Value("${file.upload.path:D:/enrollment_files/}")
    private String uploadPath;

    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename = UUID.randomUUID().toString() + extension;

        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String filepath = uploadPath + filename;
        file.transferTo(new File(filepath));

        return "/uploads/" + filename;
    }

    public boolean deleteFile(String filepath) {
        if (filepath == null || filepath.isEmpty()) {
            return false;
        }

        String fullPath = uploadPath + filepath.replace("/uploads/", "");
        File file = new File(fullPath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
