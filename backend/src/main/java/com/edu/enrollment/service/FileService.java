package com.edu.enrollment.service;

import com.edu.enrollment.entity.AttachmentEntity;
import com.edu.enrollment.mapper.AttachmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    private final AttachmentMapper attachmentMapper;

    public String upload(MultipartFile file, Long relatedId, String relatedType, Long uploaderId) throws IOException {
        // 创建目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        File destDir = new File(uploadPath + dateDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // 生成文件名
        String originalName = file.getOriginalFilename();
        String ext = originalName.substring(originalName.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + ext;

        // 保存文件
        String relativePath = dateDir + "/" + newFileName;
        File destFile = new File(uploadPath + relativePath);
        file.transferTo(destFile);

        // 保存记录
        AttachmentEntity attachment = new AttachmentEntity();
        attachment.setFileName(originalName);
        attachment.setFilePath(relativePath);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(file.getContentType());
        attachment.setRelatedId(relatedId);
        attachment.setRelatedType(relatedType);
        attachment.setUploaderId(uploaderId);
        attachmentMapper.insert(attachment);

        return relativePath;
    }
}