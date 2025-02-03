package com.leejeonghoon.blogproject.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterRequestDto {
    @NotBlank(message = "로그인 ID 입력은 필수입니다.")
    private String loginId;
    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    private String password;
    @NotBlank(message = "이메일 입력은 필수입니다.")
    private String email;
    private String nickname;
//    private MultipartFile profileImg;
//    스웨거에서는 멀티파트 타입을 리퀘스트파트로 처리해야하며
//    DTO 내부에 포함하면 변환 오류가 발생할 수 있으므로 멀티파트 타입은 DTO에서 제거하는게 좋음

}
