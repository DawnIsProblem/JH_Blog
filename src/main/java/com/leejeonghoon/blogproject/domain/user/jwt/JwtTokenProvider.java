package com.leejeonghoon.blogproject.domain.user.jwt;

import com.leejeonghoon.blogproject.domain.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtUtil jwtUtil;

    // 토큰에 유저id, 로그인id, 이메일, 닉네임, 권한 저장.
    public String createToken(Long userId, String loginId, String email, String nickname, Role role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("loginId", loginId)
                .claim("email", email)
                .claim("nickname", nickname)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtUtil.getExpTime()))
                .signWith(jwtUtil.getKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtUtil.getKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        System.out.println("[DEBUG] Authorization 헤더 값: " + bearerToken);

        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return null;
        }

        String token = bearerToken.substring(7).trim();
        System.out.println("[DEBUG] 추출된 토큰: " + token);

        return token;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtUtil.getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String loginId = claims.get("loginId", String.class);
        String email = claims.get("email", String.class);
        String nickname = claims.get("nickname", String.class);
        String role = claims.get("role", String.class);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        UserDetails userDetails = new org.springframework.security.core.userdetails.User(loginId, "", authorities);
        return new UsernamePasswordAuthenticationToken(userDetails, "", authorities);
    }

    public long getExpiration(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(jwtUtil.getKey()) // 🔹 getKey() 메서드 사용
                .build().parseClaimsJws(token).getBody();
        Date expirationDate = claims.getExpiration();
        return expirationDate.getTime() - System.currentTimeMillis();
    }

}
