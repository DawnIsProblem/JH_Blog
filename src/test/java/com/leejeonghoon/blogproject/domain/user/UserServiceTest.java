package com.leejeonghoon.blogproject.domain.user;

import com.leejeonghoon.blogproject.domain.user.dto.request.UserRegisterRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserUpdateRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.response.UserResponseDto;
import com.leejeonghoon.blogproject.domain.user.repository.UserRepository;
import com.leejeonghoon.blogproject.domain.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Test
    public void 사용자는_회원가입을_할_수_있다() {
        UserRegisterRequestDto userRegisterRequestDto = UserRegisterRequestDto.builder()
                .loginId("테스트사용자")
                .password("테스트비밀번호")
                .email("테스트이메일@gmail.com")
                .nickname("테스트닉네임")
                .build();

        when(userRepository.findByLoginId(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        UserResponseDto userResponseDto = userService.register(userRegisterRequestDto);

        assertEquals("테스트사용자", userResponseDto.getLoginId());
        assertEquals("테스트이메일@gmail.com", userResponseDto.getEmail());
        assertEquals("테스트닉네임", userResponseDto.getNickname());
    }

    @Test
    public void 사용자는_회원정보를_변경_할_수_있다(){
        UserUpdateRequestDto userUpdateRequestDto = UserUpdateRequestDto.builder()
                .nickname("수정된닉네임")
                .email("수정된이메일@gmail.com")
                .build();

        when(userRepository.findByLoginId(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertEquals("수정된닉네임", userUpdateRequestDto.getNickname());
        assertEquals("수정된이메일@gmail.com", userUpdateRequestDto.getEmail());
    }
}
