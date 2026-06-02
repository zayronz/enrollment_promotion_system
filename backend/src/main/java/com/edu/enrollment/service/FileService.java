package com.edu.enrollment.service;

import com.edu.enrollment.entity.AttachmentEntity;
import com.edu.enrollment.mapper.AttachmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    public String getUploadPath() {
        return uploadPath;
    }

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

    /**
     * 文件下载：根据附件ID下载文件
     */
    public void download(Long attachmentId, HttpServletResponse response) throws IOException {
        AttachmentEntity attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new RuntimeException("文件不存在");
        }

        File file = new File(uploadPath + attachment.getFilePath());
        if (!file.exists()) {
            throw new RuntimeException("文件已被删除");
        }

        // 设置响应头
        String encodedFileName = URLEncoder.encode(attachment.getFileName(), StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Content-Length", String.valueOf(file.length()));

        // 流式写出文件
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
    }
}