package com.rut.booking.repository;

import com.rut.booking.models.entities.User;
import com.rut.booking.models.enums.RoleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByIsActiveTrue();

    @Query("SELECT u FROM User u WHERE u.role.code = :roleType AND u.isActive = true")
    List<User> findByRoleType(@Param("roleType") RoleType roleType);

    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND u.isActive = true")
    List<User> searchUsers(@Param("search") String search);
}
