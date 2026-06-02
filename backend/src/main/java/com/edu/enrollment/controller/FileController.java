package com.edu.enrollment.controller;

import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.FileService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public ResultVO<String> upload(@RequestParam("file") MultipartFile file,
                                   @RequestParam(required = false) Long relatedId,
                                   @RequestParam(required = false) String relatedType,
                                   @CurrentUserId Long userId) throws IOException {
        String path = fileService.upload(file, relatedId, relatedType, userId);
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
        String relativePath = requestUri.replace("/api/file/view/", "");

        // 安全校验：防止路径穿越
        if (relativePath.contains("..") || relativePath.contains("//")) {
            response.setStatus(403);
            return;
        }

        String uploadPath = fileService.getUploadPath();
        File file = new File(uploadPath + relativePath);
        if (!file.exists()) {
            response.setStatus(404);
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
    }
}