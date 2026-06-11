package com.edu.enrollment.controller;

import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.FileService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResultVO<String> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(required = false) Long relatedId,
                                   @RequestParam(required = false) String relatedType,
                                   @RequestParam(required = false) String bizType,
                                   @CurrentUserId Long userId) throws IOException {
        String path = fileService.upload(file, relatedId, relatedType, bizType, userId);
        return ResultVO.success(path);
    }

    @GetMapping("/download/{id}")
    public void download(@PathVariable Long id, HttpServletResponse response) throws IOException {
        fileService.download(id, response);
    }

    /**
     * 文件访问（用于图片等静态资源预览）
     * 请求路径：/api/file/view/2026/05/31/uuid.jpg
     */
    @GetMapping("/view/**")
    public void view(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestUri = request.getRequestURI();
        String servletPath = request.getServletPath();
        log.info("收到文件访问请求，requestUri={}, servletPath={}", requestUri, servletPath);

        // 从 /api/file/view/xxx... 中提取文件路径
        String prefix = "/api/file/view/";
        String relativePath;
        
        if (requestUri.startsWith(prefix)) {
            relativePath = requestUri.substring(prefix.length());
        } else if (servletPath.startsWith(prefix)) {
            relativePath = servletPath.substring(prefix.length());
        } else {
            // 使用更通用的提取方法
            int idx = requestUri.indexOf("/view/");
            if (idx != -1) {
                relativePath = requestUri.substring(idx + 6);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                log.warn("无法解析文件路径，requestUri={}", requestUri);
                return;
            }
        }

        log.info("提取的文件路径: {}", relativePath);

        // 安全校验：防止路径穿越
        if (relativePath.contains("..") || relativePath.contains("//")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            log.warn("路径安全检查失败，relativePath={}", relativePath);
            return;
        }

        String uploadPath = fileService.getUploadPath();
        File file = new File(uploadPath + relativePath);
        log.info("文件完整路径: {}", file.getAbsolutePath());
        
        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            log.warn("文件不存在: {}", file.getAbsolutePath());
            return;
        }

        String contentType = Files.probeContentType(file.toPath());
        if (contentType != null) {
            response.setContentType(contentType);
        }

        response.setHeader("Content-Length", String.valueOf(file.length()));

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
        }
        
        log.info("文件发送成功: {}", file.getAbsolutePath());
    }
}
