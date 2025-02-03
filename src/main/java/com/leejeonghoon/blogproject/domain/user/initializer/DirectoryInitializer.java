package com.leejeonghoon.blogproject.domain.user.initializer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class DirectoryInitializer {

    @Value("${file.upload.dir}")
    private String uploadProfileImgDir;

    @EventListener(ApplicationReadyEvent.class)
    public void initDirectories() {
        File uploadDir = new File(uploadProfileImgDir);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                System.out.println("업로드 디렉토리가 생성되었습니다: " + uploadProfileImgDir);
            } else {
                throw new RuntimeException("업로드 디렉토리 생성에 실패했습니다: " + uploadProfileImgDir);
            }
        } else {
            System.out.println("업로드 디렉토리가 이미 존재합니다: " + uploadProfileImgDir);
        }
    }
}
