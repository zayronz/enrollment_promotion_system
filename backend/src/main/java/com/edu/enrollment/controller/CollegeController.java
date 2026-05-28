package com.edu.enrollment.controller;

import com.edu.enrollment.entity.CollegeEntity;
import com.edu.enrollment.mapper.CollegeMapper;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/college")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeMapper collegeMapper;

    @GetMapping("/list")
    public ResultVO<List<CollegeEntity>> list() {
        return ResultVO.success(collegeMapper.selectList(null));
    }
}