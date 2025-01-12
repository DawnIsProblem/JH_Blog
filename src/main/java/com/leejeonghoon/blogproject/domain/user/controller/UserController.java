package com.leejeonghoon.blogproject.domain.user.controller;

import com.leejeonghoon.blogproject.domain.user.dto.request.UserLoginRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserRegisterRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserUpdateRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.response.UserResponseDto;
import com.leejeonghoon.blogproject.domain.user.repository.UserRepository;
import com.leejeonghoon.blogproject.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@ModelAttribute UserRegisterRequestDto userRegisterRequestDto) {
        UserResponseDto userResponseDto = userService.register(userRegisterRequestDto);
        return ResponseEntity.ok(userResponseDto);
    }


    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequestDto userLoginRequestDto) {
        String token = userService.login(userLoginRequestDto);
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    // 회원 정보 수정
    @PatchMapping("/update/{loginId}")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable String loginId,
            @ModelAttribute UserUpdateRequestDto userUpdateRequestDto,
            @RequestParam(value = "profileImg", required = false) MultipartFile profileImg
    ) {
        UserResponseDto userResponseDto = userService.update(loginId, userUpdateRequestDto, profileImg);
        return ResponseEntity.ok(userResponseDto);
    }




    // 비밀번호 변경
    @PatchMapping("/{loginId}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable String loginId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        userService.changePassword(loginId, oldPassword, newPassword);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    // 나의 정보 보기
    @GetMapping("/my_info")
    public ResponseEntity<UserResponseDto> getMyInfo(@PathVariable String loginId) {
        return ResponseEntity.ok(userService.getUserInfo(loginId));
    }

    // 다른 회원의 정보 보기
//    @GetMapping("")
//    public

    // 회원 탈퇴
    @DeleteMapping("/{loginId}/delete")
    public ResponseEntity<String> delete(
            @PathVariable String loginId,
            @RequestParam String password) {
        userService.deleteUser(loginId, password);
        return ResponseEntity.ok("계정이 삭제되었습니다.");
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorication") String token){
        if(token == null || !token.startsWith("Bearer ")){
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }
        token = token.substring(7);
        userService.logout(token);
        return ResponseEntity.ok("로그아웃 하였습니다.");
    }

}
