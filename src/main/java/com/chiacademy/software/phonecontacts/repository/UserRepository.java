package com.chiacademy.software.phonecontacts.repository;

import com.chiacademy.software.phonecontacts.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByLogin(String login);

    Optional<User> findByLogin(String login);
}