package com.edu.enrollment.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.entity.CollegeEntity;
import com.edu.enrollment.exception.BusinessException;
import com.edu.enrollment.mapper.CollegeMapper;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/college")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeMapper collegeMapper;

    @GetMapping("/list")
    public ResultVO<List<CollegeEntity>> list() {
        return ResultVO.success(collegeMapper.selectList(null));
    }

    @PostMapping("/resolve")
    public ResultVO<CollegeEntity> resolve(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (StrUtil.isBlank(name)) {
            throw new BusinessException("学院不能为空");
        }

        String collegeName = name.trim();
        CollegeEntity college = collegeMapper.selectOne(
                new LambdaQueryWrapper<CollegeEntity>()
                        .eq(CollegeEntity::getName, collegeName)
                        .last("LIMIT 1")
        );
        if (college != null) {
            return ResultVO.success(college);
        }

        CollegeEntity newCollege = new CollegeEntity();
        newCollege.setName(collegeName);
        newCollege.setCode("C" + System.currentTimeMillis());
        collegeMapper.insert(newCollege);
        return ResultVO.success(newCollege);
    }
}
