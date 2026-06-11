package com.edu.enrollment.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.dto.MaterialDTO;
import com.edu.enrollment.entity.MaterialEntity;
import com.edu.enrollment.mapper.MaterialMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaterialService {

    @Value("${file.upload-path}")
    private String uploadPath;

    private final MaterialMapper materialMapper;

    public List<MaterialEntity> list(String keyword, String category) {
        LambdaQueryWrapper<MaterialEntity> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(MaterialEntity::getName, keyword)
                    .or()
                    .like(MaterialEntity::getDescription, keyword)
                    .or()
                    .like(MaterialEntity::getFileName, keyword));
        }
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(MaterialEntity::getCategory, category);
        }
        wrapper.orderByDesc(MaterialEntity::getCreateTime);
        return materialMapper.selectList(wrapper);
    }

    public MaterialEntity upload(MultipartFile file, MaterialDTO dto, Long uploaderId) throws IOException {
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        File destDir = new File(uploadPath + dateDir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (StrUtil.isNotBlank(originalName) && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID() + ext;
        String relativePath = dateDir + "/" + newFileName;
        File destFile = new File(uploadPath + relativePath);
        file.transferTo(destFile);

        MaterialEntity material = new MaterialEntity();
        material.setName(dto.getName());
        material.setCategory(dto.getCategory());
        material.setDescription(dto.getDescription());
        material.setFileName(originalName);
        material.setFilePath(relativePath);
        material.setFileSize(file.getSize());
        material.setFileType(file.getContentType());
        material.setUploaderId(uploaderId);
        materialMapper.insert(material);
        return material;
    }

    public void delete(Long id) {
        materialMapper.deleteById(id);
    }
}
