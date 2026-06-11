package com.edu.enrollment.controller;

import com.edu.enrollment.dto.MaterialDTO;
import com.edu.enrollment.security.CurrentUserId;
import com.edu.enrollment.service.MaterialService;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping("/list")
    public ResultVO<?> list(@RequestParam(required = false) String keyword,
                            @RequestParam(required = false) String category) {
        return ResultVO.success(materialService.list(keyword, category));
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('SCHOOL','TEACHER')")
    public ResultVO<?> upload(@RequestParam("file") MultipartFile file,
                              @Valid MaterialDTO dto,
                              @CurrentUserId Long userId) throws IOException {
        return ResultVO.success(materialService.upload(file, dto, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SCHOOL','TEACHER')")
    public ResultVO<?> delete(@PathVariable Long id) {
        materialService.delete(id);
        return ResultVO.success();
    }
}
