package com.leejeonghoon.blogproject.domain.user.controller;

import com.leejeonghoon.blogproject.domain.user.dto.request.UserLoginRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserRegisterRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserUpdateRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.response.UserResponseDto;
import com.leejeonghoon.blogproject.domain.user.jwt.JwtTokenProvider;
import com.leejeonghoon.blogproject.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "[User] 사용자 관련 시스템")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Operation(summary = "회원가입")
    @PostMapping(path = "/register", consumes = "multipart/form-data")
    public ResponseEntity<UserResponseDto> register(
            @RequestParam String loginId,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam(required = false) String nickname,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg
    ) {
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .loginId(loginId)
                .password(password)
                .email(email)
                .nickname(nickname)
                .build();

        UserResponseDto userResponseDto = userService.register(userRegisterRequestDto, profileImg);
        return ResponseEntity.ok(userResponseDto);
    }

    // 로그인
    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String loginId,
            @RequestParam String password
    ) {
        UserLoginRequestDto userLoginRequestDto = UserLoginRequestDto.builder()
                .loginId(loginId)
                .password(password)
                .build();

        Map<String, String> response = userService.login(userLoginRequestDto);

        return ResponseEntity.ok(response);
    }

    // 회원 정보 수정
    @Operation(summary = "회원 정보 수정", security = { @SecurityRequirement(name = "BearerAuth")})
    @PatchMapping(value = "/update", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponseDto> update(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email,
            @RequestPart(value = "profileImg", required = false) MultipartFile profileImg,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String loginId = userDetails.getUsername();
        log.info("수정 요청: loginId={}, nickname={}, email={}", loginId, nickname, email);

        UserUpdateRequestDto userUpdateRequestDto = UserUpdateRequestDto.builder()
                .nickname(nickname)
                .email(email)
                .build();

        UserResponseDto userResponseDto = userService.update(loginId, userUpdateRequestDto, profileImg);
        return ResponseEntity.ok(userResponseDto);
    }



    // 비밀번호 변경
    @Operation(summary = "비밀번호 변경", security = { @SecurityRequirement(name = "BearerAuth")})
    @PatchMapping("/pw_change")
    public ResponseEntity<String> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String loginId = userDetails.getUsername();
        userService.changePassword(loginId, oldPassword, newPassword);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }


    // 나의 정보 보기
    @Operation(summary = "내 정보 보기", security = { @SecurityRequirement(name = "BearerAuth")})
    @GetMapping("/my_info")
    public ResponseEntity<UserResponseDto> getMyInfo(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String loginId = userDetails.getUsername();
        return ResponseEntity.ok(userService.getUserInfoByLoginId(loginId));
    }


    // 다른 회원 정보 보기
    @Operation(summary = "다른 회원 정보 보기")
    @GetMapping("/other_info/{nickname}")
    public ResponseEntity<UserResponseDto> getUserInfo(@PathVariable String nickname) {
        return ResponseEntity.ok(userService.getUserInfoByNickname(nickname));
    }


    // 회원 탈퇴
    @Operation(summary = "회원탈퇴", security = { @SecurityRequirement(name = "BearerAuth")})
    @DeleteMapping("/{loginId}/delete")
    public ResponseEntity<String> delete(
            @PathVariable String loginId,
            @RequestParam String password) {
        userService.deleteUser(loginId, password);
        return ResponseEntity.ok("계정이 삭제되었습니다.");
    }


    // 로그아웃
    @Operation(summary = "로그아웃", security = { @SecurityRequirement(name = "BearerAuth")})
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body("유효한 토큰이 없습니다.");
        }

        try {
            userService.logout(token.trim());
            return ResponseEntity.ok("로그아웃 하였습니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 내부 오류 발생: " + e.getMessage());
        }
    }

}
