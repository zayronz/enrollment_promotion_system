package com.edu.enrollment.controller;

import com.edu.enrollment.entity.SchoolDictEntity;
import com.edu.enrollment.mapper.SchoolDictMapper;
import com.edu.enrollment.utils.SchoolNameNormalizer;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/school-dict")
@RequiredArgsConstructor
public class SchoolDictController {

    private final SchoolDictMapper schoolDictMapper;
    private final SchoolNameNormalizer schoolNameNormalizer;

    @GetMapping("/search")
    public ResultVO<List<Map<String, Object>>> search(@RequestParam String keyword) {
        List<SchoolDictEntity> schools = schoolDictMapper.searchByName(keyword);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SchoolDictEntity s : schools) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getStandardName());
            map.put("province", s.getProvince());
            map.put("city", s.getCity());
            result.add(map);
        }
        return ResultVO.success(result);
    }

    @GetMapping("/normalize")
    public ResultVO<Map<String, String>> normalize(@RequestParam String name) {
        String normalized = schoolNameNormalizer.normalize(name);
        Map<String, String> result = new HashMap<>();
        result.put("original", name);
        result.put("normalized", normalized);
        return ResultVO.success(result);
    }

    @GetMapping("/all")
    public ResultVO<List<Map<String, Object>>> getAll() {
        List<SchoolDictEntity> schools = schoolDictMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (SchoolDictEntity s : schools) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getStandardName());
            map.put("province", s.getProvince());
            map.put("city", s.getCity());
            result.add(map);
        }
        return ResultVO.success(result);
    }
}
