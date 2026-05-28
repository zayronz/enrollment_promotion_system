package com.edu.enrollment.controller;

import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.FileService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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
}