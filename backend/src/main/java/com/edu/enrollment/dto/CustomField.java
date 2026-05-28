package com.edu.enrollment.dto;

import lombok.Data;
import java.util.List;

@Data
public class CustomField {

    private String name;       // 字段名（用于存储）

    private String label;      // 显示标签

    private String type;       // 字段类型：text, textarea, select, date

    private List<String> options;  // 选项（当type为select时使用）

    private Boolean required;  // 是否必填
}