package com.wyx.enrollment_promotion_systemmaster;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wyx.enrollment_promotion_systemmaster.mapper")
public class EnrollmentPromotionSystemMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnrollmentPromotionSystemMasterApplication.class, args);
    }

}
