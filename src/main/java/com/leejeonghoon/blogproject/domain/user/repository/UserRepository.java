package com.leejeonghoon.blogproject.domain.user.repository;

import com.leejeonghoon.blogproject.domain.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByLoginId(String loginId);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPassword(String password);
    Optional<UserEntity> findByNickname(String nickName);
}
