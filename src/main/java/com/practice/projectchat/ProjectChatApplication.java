package com.practice.projectchat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan // 클래스패스에서 @ConfigurationProperties 클래스를 자동 스캔해서 빈으로 등록
public class ProjectChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectChatApplication.class, args);
    }

}
