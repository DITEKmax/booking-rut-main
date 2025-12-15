package com.rut.booking.repository;

import com.rut.booking.models.entities.Role;
import com.rut.booking.models.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByCode(RoleType code);

    boolean existsByCode(RoleType code);
}
