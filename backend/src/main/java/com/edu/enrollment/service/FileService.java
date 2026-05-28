package com.edu.enrollment.service;

import com.edu.enrollment.entity.AttachmentEntity;
import com.edu.enrollment.exception.BusinessException;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    private final AttachmentMapper attachmentMapper;

    /** 允许的文件MIME类型 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            "video/mp4", "video/avi", "video/quicktime", "video/x-msvideo",
            "text/plain"
    ));

    /** 允许的文件扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            ".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx",
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp",
            ".mp4", ".avi", ".mov", ".txt"
    ));

    /** 最大文件大小 (字节) */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public String upload(MultipartFile file, Long relatedId, String relatedType, Long uploaderId) throws IOException {
        // 0. 校验文件是否为空
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 1. 校验文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过10MB，当前大小：" +
                    String.format("%.2f", file.getSize() / 1024.0 / 1024.0) + "MB");
        }

        // 2. 校验文件类型
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new BusinessException("无法识别文件类型，请上传有效文件");
        }
        String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("不支持的文件格式：" + ext +
                    "，仅支持 PDF/Word/Excel/PPT/图片(JPG/PNG/GIF)/视频(MP4/MOV)/文本");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException("不支持的文件类型：" + contentType +
                    "，仅支持 PDF/Word/Excel/PPT/图片/视频/文本");
        }

        // 3. 创建目录
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        File destDir = new File(uploadPath + dateDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        // 4. 生成文件名
        String newFileName = UUID.randomUUID().toString() + ext;

        // 5. 保存文件
        String relativePath = dateDir + "/" + newFileName;
        File destFile = new File(uploadPath + relativePath);
        file.transferTo(destFile);

        // 6. 保存记录
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
            throw new BusinessException("文件不存在");
        }

        File file = new File(uploadPath + attachment.getFilePath());
        if (!file.exists()) {
            throw new BusinessException("文件已被删除");
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