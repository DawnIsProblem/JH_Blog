package com.leejeonghoon.blogproject.domain.user.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserLogoutService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public void logout(String token) {
        long expiration = jwtTokenProvider.getExpiration(token);

        redisTemplate.opsForValue().set(token, "로그아웃", expiration, TimeUnit.MILLISECONDS);
    }
}
