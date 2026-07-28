package com.tcsproject.finance_tracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcsproject.finance_tracker.entity.*;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
