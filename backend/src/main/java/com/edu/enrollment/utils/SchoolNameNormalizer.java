package com.edu.enrollment.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.entity.SchoolDictEntity;
import com.edu.enrollment.mapper.SchoolDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 学校名称标准化工具
 * 实现机械分词和模糊匹配
 */
@Component
@RequiredArgsConstructor
public class SchoolNameNormalizer {

    private final SchoolDictMapper schoolDictMapper;

    // 常见学校后缀
    private static final List<String> SCHOOL_SUFFIXES = List.of(
            "大学", "学院", "职业技术学院", "专科学校", "中学", "高中"
    );

    /**
     * 标准化学校名称
     */
    public String normalize(String inputName) {
        if (StrUtil.isBlank(inputName)) {
            return inputName;
        }

        String trimmed = inputName.trim();

        // 1. 精确匹配
        SchoolDictEntity exactMatch = schoolDictMapper.selectOne(
                new LambdaQueryWrapper<SchoolDictEntity>()
                        .eq(SchoolDictEntity::getStandardName, trimmed)
        );
        if (exactMatch != null) {
            return exactMatch.getStandardName();
        }

        // 2. 别名匹配
        List<SchoolDictEntity> allDicts = schoolDictMapper.selectList(null);
        for (SchoolDictEntity dict : allDicts) {
            List<String> aliases = JSONUtil.toList(JSONUtil.parseArray(dict.getAliasNames()), String.class);
            if (aliases.contains(trimmed)) {
                return dict.getStandardName();
            }
        }

        // 3. 模糊匹配（最长公共子串）
        String bestMatch = fuzzyMatch(trimmed, allDicts);
        if (bestMatch != null) {
            return bestMatch;
        }

        // 4. 默认返回原值（去除后缀后标准化）
        return normalizeBySuffix(trimmed);
    }

    private String fuzzyMatch(String input, List<SchoolDictEntity> dicts) {
        String bestMatch = null;
        int maxLength = 0;

        for (SchoolDictEntity dict : dicts) {
            String standard = dict.getStandardName();
            int lcsLength = longestCommonSubstringLength(input, standard);
            if (lcsLength > maxLength && lcsLength >= 3) {
                maxLength = lcsLength;
                bestMatch = standard;
            }
        }
        return bestMatch;
    }

    private int longestCommonSubstringLength(String a, String b) {
        int maxLen = 0;
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                if (a.charAt(i - 1) == b.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    maxLen = Math.max(maxLen, dp[i][j]);
                }
            }
        }
        return maxLen;
    }

    private String normalizeBySuffix(String input) {
        for (String suffix : SCHOOL_SUFFIXES) {
            if (input.endsWith(suffix)) {
                return input;
            }
        }
        // 尝试添加常见后缀
        for (String suffix : SCHOOL_SUFFIXES) {
            if (input.contains(suffix.substring(0, Math.min(2, suffix.length())))) {
                return input + suffix;
            }
        }
        return input;
    }
}