package com.leejeonghoon.blogproject.domain.user.service;

import com.leejeonghoon.blogproject.domain.user.dto.request.UserLoginRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserUpdateRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.response.UserResponseDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserRegisterRequestDto;
import com.leejeonghoon.blogproject.domain.user.entity.Role;
import com.leejeonghoon.blogproject.domain.user.entity.UserEntity;
import com.leejeonghoon.blogproject.domain.user.jwt.JwtTokenProvider;
import com.leejeonghoon.blogproject.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${file.upload.dir}")
    private String uploadProfileImgDir;

    // 프로필 이미지 저장
    public String saveProfileImage(MultipartFile profileImage) {
        if(profileImage == null || profileImage.isEmpty()) {
            return "/images/default-profile.jpg";
        }

        File uploadDir = new File(uploadProfileImgDir);
        if(!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        String filePath = uploadProfileImgDir + profileImage.getOriginalFilename();
        File file = new File(filePath);
        try{
            profileImage.transferTo(file);
        } catch (IOException e) {
            throw new RuntimeException("프로필 이미지 저장 실패: " + e.getMessage());
        }
        return filePath;
    }

    // 프로필 이미지 삭제
    private void deleteProfileImage(String profileImgPath) {
        if (profileImgPath == null) return;
        File file = new File(profileImgPath);
        if (file.exists()) {
            file.delete();
        }
    }


    // 회원 가입
    @Transactional
    public UserResponseDto register(UserRegisterRequestDto userRegisterRequestDto, MultipartFile profileImg) {
        if(userRepository.findByLoginId(userRegisterRequestDto.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디 입니다.");
        }
        if(userRepository.findByEmail(userRegisterRequestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 Email 주소입니다.");
        }
        if(userRegisterRequestDto.getPassword() == null || userRegisterRequestDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }

        String encoredPassword = passwordEncoder.encode(userRegisterRequestDto.getPassword());
        String profileImagePath = saveProfileImage(profileImg);

        UserEntity userEntity = UserEntity.builder()
                .loginId(userRegisterRequestDto.getLoginId())
                .password(encoredPassword)
                .email(userRegisterRequestDto.getEmail())
                .nickname(userRegisterRequestDto.getNickname())
                .profileImg(profileImagePath)
                .role(Role.ROLE_USER) // 403 에러 해결을 위한 회원가입 시 권한 부여
                .build();

        userRepository.save(userEntity);
        userRepository.flush();

        return UserResponseDto.builder()
                .loginId(userEntity.getLoginId())
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .profileImg(userEntity.getProfileImg())
                .build();
    }

    // 로그인
    public Map<String, String> login(UserLoginRequestDto userLoginRequestDto) {
        UserEntity userEntity = userRepository.findByLoginId(userLoginRequestDto.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디 입니다."));

        if(!passwordEncoder.matches(userLoginRequestDto.getPassword(), userEntity.getPassword() )) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // JWT 토큰 생성
        String token = jwtTokenProvider.createToken(
                userEntity.getId(),
                userEntity.getLoginId(),
                userEntity.getEmail(),
                userEntity.getNickname(),
                userEntity.getRole()
        );

        // 토큰 확인을 위한 코드
        System.out.println("생성된 토큰: " + token);

        // JSON 형식으로 반환할 Map 생성
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("loginId", userEntity.getLoginId());

        return response;
    }

    // 로그아웃
    public void logout(String token) {
        long expiration = jwtTokenProvider.getExpiration(token);

        redisTemplate.opsForValue().set(token, "로그아웃", expiration, TimeUnit.MILLISECONDS);
    }


    // 회원 정보 수정
    public UserResponseDto update(String loginId, UserUpdateRequestDto userUpdateRequestDto, MultipartFile profileImage) {
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 로그인 아이디입니다."));

        if (userUpdateRequestDto.getEmail() != null) {
            if (userRepository.findByEmail(userUpdateRequestDto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
            }
            userEntity.setEmail(userUpdateRequestDto.getEmail());
        }

        if (userUpdateRequestDto.getNickname() != null) {
            userEntity.setNickname(userUpdateRequestDto.getNickname());
        }

        if (profileImage != null) {
            deleteProfileImage(userEntity.getProfileImg());
            String newProfileImgPath = saveProfileImage(profileImage);
            userEntity.setProfileImg(newProfileImgPath);
        }

        userRepository.save(userEntity);

        return UserResponseDto.builder()
                .email(userEntity.getEmail())
                .nickname(userEntity.getNickname())
                .profileImg(userEntity.getProfileImg())
                .build();
    }

    // 사용자 삭제
    public void deleteUser(String loginId, String password) {
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!passwordEncoder.matches(password, userEntity.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        userRepository.delete(userEntity);
    }

    // 내 정보 보기
    public UserResponseDto getUserInfoByLoginId(String loginId) {
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디의 유저를 찾을 수 없습니다."));

        return UserResponseDto.builder()
                .loginId(userEntity.getLoginId())
                .nickname(userEntity.getNickname())
                .email(userEntity.getEmail())
                .profileImg(userEntity.getProfileImg())
                .build();
    }


    // 다른 사용자 정보 보기
    public UserResponseDto getUserInfoByNickname(String nickname) {
        UserEntity userEntity = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임의 유저를 찾을 수 없습니다."));

        return UserResponseDto.builder()
                .loginId(userEntity.getLoginId())
                .nickname(userEntity.getNickname())
                .email(userEntity.getEmail())
                .profileImg(userEntity.getProfileImg())
                .build();
    }

    // 비밀번호 변경
    public void changePassword(String loginId, String oldPassword, String newPassword) {
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 로그인 아이디입니다."));

        if(!passwordEncoder.matches(oldPassword, userEntity.getPassword())) {
            throw new IllegalArgumentException("기존 비밀번호와 일치하지 않습니다.");
        }

        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);
    }
}
