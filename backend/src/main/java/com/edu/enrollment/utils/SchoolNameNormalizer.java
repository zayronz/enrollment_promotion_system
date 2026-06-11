package com.edu.enrollment.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.edu.enrollment.entity.SchoolDictEntity;
import com.edu.enrollment.mapper.SchoolDictMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // 学生项目演示常用内置标准化词典，避免数据库词典为空时无法完成核心用例
    private static final Map<String, List<String>> BUILTIN_ALIASES = Map.of(
            "太原理工大学", List.of("太原理工", "太理", "太工大"),
            "山西大学", List.of("山大"),
            "中北大学", List.of("中北")
    );

    /**
     * 标准化学校名称
     */
    public String normalize(String inputName) {
        if (StrUtil.isBlank(inputName)) {
            return inputName;
        }

        String trimmed = inputName.trim();

        // 0. 内置别名匹配
        String builtinMatch = matchBuiltin(trimmed);
        if (builtinMatch != null) {
            return builtinMatch;
        }

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
            if (StrUtil.isBlank(dict.getAliasNames())) {
                continue;
            }
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

    /**
     * 根据输入内容返回学校名称联想项
     */
    public List<String> suggest(String keyword, int limit) {
        if (StrUtil.isBlank(keyword)) {
            return List.of();
        }

        String trimmed = keyword.trim();
        Set<String> suggestions = new LinkedHashSet<>();

        for (Map.Entry<String, List<String>> entry : BUILTIN_ALIASES.entrySet()) {
            if (suggestions.size() >= limit) {
                break;
            }
            String standardName = entry.getKey();
            boolean matched = standardName.contains(trimmed)
                    || entry.getValue().stream().anyMatch(alias -> alias.contains(trimmed));
            if (matched) {
                suggestions.add(standardName);
            }
        }

        List<SchoolDictEntity> dicts = schoolDictMapper.selectList(null);

        for (SchoolDictEntity dict : dicts) {
            if (suggestions.size() >= limit) {
                break;
            }
            String standardName = dict.getStandardName();
            if (StrUtil.isNotBlank(standardName) && standardName.contains(trimmed)) {
                suggestions.add(standardName);
                continue;
            }
            if (StrUtil.isNotBlank(dict.getAliasNames())) {
                List<String> aliases = JSONUtil.toList(JSONUtil.parseArray(dict.getAliasNames()), String.class);
                boolean aliasMatched = aliases.stream().anyMatch(alias -> alias != null && alias.contains(trimmed));
                if (aliasMatched && StrUtil.isNotBlank(standardName)) {
                    suggestions.add(standardName);
                }
            }
        }

        return new ArrayList<>(suggestions);
    }

    private String matchBuiltin(String input) {
        for (Map.Entry<String, List<String>> entry : BUILTIN_ALIASES.entrySet()) {
            String standardName = entry.getKey();
            if (standardName.equals(input) || entry.getValue().contains(input)) {
                return standardName;
            }
        }
        return null;
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
