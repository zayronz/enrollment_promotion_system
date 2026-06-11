package com.edu.enrollment.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Jackson 配置 - 支持多种时间格式反序列化
 */
@Configuration
public class JacksonConfig {

    /**
     * 可处理多种时间格式的 LocalDateTime 反序列化器
     * 支持的格式:
     * 1. yyyy-MM-dd HH:mm:ss
     * 2. yyyy-MM-dd'T'HH:mm:ss (ISO 无时区)
     * 3. yyyy-MM-dd'T'HH:mm:ss+HH:MM (ISO 带时区)
     * 4. yyyy-MM-dd'T'HH:mm:ss.SSS+HH:MM (ISO 带毫秒和时区)
     * 5. yyyy-MM-dd HH:mm:ss+HH:MM (前端特殊格式)
     */
    public static class LenientLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        private static final DateTimeFormatter[] FORMATTERS = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME,
                DateTimeFormatter.ISO_ZONED_DATE_TIME
        };

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            value = value.trim();

            // 依次尝试各格式
            for (DateTimeFormatter formatter : FORMATTERS) {
                try {
                    if (formatter == DateTimeFormatter.ISO_OFFSET_DATE_TIME || formatter == DateTimeFormatter.ISO_ZONED_DATE_TIME) {
                        // 带时区的格式 -> 转换为本地时间
                        OffsetDateTime odt = OffsetDateTime.parse(value, formatter);
                        return odt.toLocalDateTime();
                    }
                    return LocalDateTime.parse(value, formatter);
                } catch (DateTimeParseException ignored) {
                    // 继续尝试下一个
                }
            }

            // 兼容 "yyyy-MM-dd HH:mm:ss+HH:MM" 格式 (前端替换T为空格后保留时区)
            int plusIndex = value.indexOf('+', 10);
            int minusIndex = value.lastIndexOf('-');
            if (plusIndex > 15 || (minusIndex > 15 && minusIndex < value.length() - 3)) {
                try {
                    String corePart;
                    if (plusIndex > 15) {
                        corePart = value.substring(0, plusIndex).trim();
                    } else {
                        corePart = value.substring(0, minusIndex).trim();
                    }
                    return LocalDateTime.parse(corePart, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (DateTimeParseException ignored) {
                    // 继续
                }
            }

            // 全部失败
            throw new DateTimeParseException("无法解析日期时间: " + value, value, 0);
        }
    }

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder.createXmlMapper(false).build();
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        // 注册自定义 LocalDateTime 反序列化器 - 支持多种时间格式
        javaTimeModule.addDeserializer(LocalDateTime.class, new LenientLocalDateTimeDeserializer());
        mapper.registerModule(javaTimeModule);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
