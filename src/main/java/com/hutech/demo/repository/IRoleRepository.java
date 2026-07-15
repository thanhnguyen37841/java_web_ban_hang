package com.hutech.demo.repository;

import com.hutech.demo.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IRoleRepository extends JpaRepository<RoleEntity, Long> {
    RoleEntity findRoleById(Long id);
    RoleEntity findByName(String name);
}
