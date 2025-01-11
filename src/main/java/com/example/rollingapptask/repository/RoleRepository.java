package com.example.rollingapptask.repository;

import com.example.rollingapptask.model.Role;
import com.example.rollingapptask.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName roleName);
} 