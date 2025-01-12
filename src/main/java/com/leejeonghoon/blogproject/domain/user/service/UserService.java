package com.leejeonghoon.blogproject.domain.user.service;

import com.leejeonghoon.blogproject.domain.user.dto.request.UserLoginRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserUpdateRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.response.UserResponseDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserRegisterRequestDto;
import com.leejeonghoon.blogproject.domain.user.entity.UserEntity;
import com.leejeonghoon.blogproject.domain.user.jwt.JwtTokenProvider;
import com.leejeonghoon.blogproject.domain.user.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    // 프로필 이미지 저장
    public String saveProfileImage(MultipartFile file) throws IOException {
        if(file == null || file.isEmpty()) {
            return null; // 추후 기본 이미지로 사용할 수 있게 변경 가능
        }

        String filePath = "uploads/" + file.getOriginalFilename();
        File destination = new File(filePath);
        file.transferTo(destination);
        return filePath;
    }

    // 회원 가입
    public UserEntity register(UserRegisterRequestDto userRegisterRequestDto) throws Exception {
        if(userRepository.findByLoginId(userRegisterRequestDto.getLoginId()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 아이디 입니다.");
        }
        if(userRepository.findByEmail(userRegisterRequestDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 Email 주소입니다.");
        }

        String encoredPassword = passwordEncoder.encode(userRegisterRequestDto.getPassword());
        String profileImagePath = saveProfileImage(userRegisterRequestDto.getProfileImg());

        UserEntity userEntity = UserEntity.builder()
                .loginId(userRegisterRequestDto.getLoginId())
                .password(encoredPassword)
                .email(userRegisterRequestDto.getEmail())
                .nickname(userRegisterRequestDto.getNickname())
                .profileImg(profileImagePath)
                .build();

        return userRepository.save(userEntity);
    }

    // 로그인
    public UserLoginRequestDto login(UserLoginRequestDto userLoginRequestDto) {
        UserEntity userEntity = userRepository.findByLoginId(userLoginRequestDto.getLoginId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디 입니다."));

        if(!passwordEncoder.matches(userLoginRequestDto.getPassword(), userEntity.getPassword() )) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.createToken(userEntity.getLoginId(), 1000L*60*60*24);
    }


    // 로그아웃
    public void logout(String token) {
        long expiration = jwtTokenProvider.getExpiration(token);

        redisTemplate.opsForValue().set(token, "로그아웃", expiration, TimeUnit.MILLISECONDS);
    }


    // 회원 정보 수정
    public UserEntity updateUser(String loginId, UserUpdateRequestDto userUpdateRequestDto) {
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 로그인 아이디입니다."));

        if(userUpdateRequestDto.getEmail() != null) {
            if(userRepository.findByEmail(userUpdateRequestDto.getEmail()).isPresent()) {
                throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
            }
            userEntity.setEmail(userUpdateRequestDto.getEmail());
        }

        if(userUpdateRequestDto.getNickname() != null) {
            userEntity.setNickname(userUpdateRequestDto.getNickname());
        }

        return userRepository.save(userEntity);
    }

    public void deleteUser(String loginId) {
        UserEntity userEntity = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        userRepository.delete(userEntity);
    }

    public UserResponseDto getUserInfo(String nickName) {
        UserEntity userEntity = userRepository.findByNickName(nickName)
                .orElseThrow(() -> new IllegalArgumentException("해당 닉네임의 유저를 찾을 수 없습니다."));

        return UserResponseDto.builder()
                .loginId(userEntity.getLoginId())
                .nickname(userEntity.getNickname())
                .email(userEntity.getEmail())
                .profileImg(userEntity.getProfileImg())
                .build();
    }
}
