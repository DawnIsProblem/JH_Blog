package com.leejeonghoon.blogproject.domain.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leejeonghoon.blogproject.common.config.SecurityConfig;
import com.leejeonghoon.blogproject.domain.user.controller.UserController;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserLoginRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserRegisterRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.request.UserUpdateRequestDto;
import com.leejeonghoon.blogproject.domain.user.dto.response.UserResponseDto;
import com.leejeonghoon.blogproject.domain.user.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Order(1)
    @DisplayName("회원가입 테스트")
    @WithMockUser(username = "testUser", roles = {"USER"})
    @Test
    public void 회원가입_테스트() throws Exception {
        UserRegisterRequestDto requestDto = UserRegisterRequestDto.builder()
                .loginId("testUser")
                .password("password123")
                .email("test@example.com")
                .nickname("testNickname")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .loginId("testUser")
                .email("test@example.com")
                .nickname("testNickname")
                .build();

        Mockito.when(userService.register(any(UserRegisterRequestDto.class), any(MultipartFile.class))).thenReturn(responseDto);

        mockMvc.perform(multipart("/api/users/register")
                        .file(new MockMultipartFile(
                                "profileImg", // 컨트롤러의 파라미터 이름과 일치해야 함
                                "profile.png",
                                "image/png",
                                "test data".getBytes()
                        ))
                        .param("loginId", "testUser")
                        .param("password", "password123")
                        .param("email", "test@example.com")
                        .param("nickname", "testNickname")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loginId").value("testUser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("testNickname"));
    }

    @Order(2)
    @DisplayName("회원 로그인 테스트")
    @WithMockUser(username = "testUser", roles = {"USER"})
    @Test
    public void 로그인_테스트() throws Exception {
        UserLoginRequestDto loginRequestDto = UserLoginRequestDto.builder()
                .loginId("testUser")
                .password("password123")
                .build();

        String mockToken = "mockedJwtToken";

        Mockito.when(userService.login(any(UserLoginRequestDto.class))).thenReturn(mockToken);

        mockMvc.perform(post("/api/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(mockToken));
    }

    @Order(3)
    @DisplayName("회원 정보 수정 테스트")
    @WithMockUser(username = "testUser", roles = {"USER"})
    @Test
    public void 회원정보_수정_테스트() throws Exception {
        UserUpdateRequestDto requestDto = UserUpdateRequestDto.builder()
                .nickname("updatedNickname")
                .email("updated@example.com")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .nickname("updatedNickname")
                .email("updated@example.com")
                .build();

        Mockito.when(userService.update(anyString(), any(UserUpdateRequestDto.class), any())).thenReturn(responseDto);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "profileImg",
                "profile.png",
                "image/png",
                "test data".getBytes()
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/users/update/{loginId}", "testUser")
                        .file(multipartFile)
                        .param("email", "updated@example.com")
                        .param("nickname", "updatedNickname")
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("updatedNickname"))
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }


}
