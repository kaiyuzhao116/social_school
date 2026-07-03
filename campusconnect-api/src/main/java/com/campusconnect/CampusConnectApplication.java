package com.campusconnect;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campusconnect.mapper")
@MapperScan("com.campusconnect.**.mapper")
public class CampusConnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusConnectApplication.class, args);
    }
}
