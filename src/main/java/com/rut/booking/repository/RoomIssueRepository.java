package com.rut.booking.repository;

import com.rut.booking.models.entities.RoomIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomIssueRepository extends JpaRepository<RoomIssue, Long> {

    @Query("SELECT ri FROM RoomIssue ri WHERE ri.isResolved = false ORDER BY ri.createdAt DESC")
    List<RoomIssue> findUnresolvedIssues();

    @Query("SELECT ri FROM RoomIssue ri ORDER BY ri.createdAt DESC")
    List<RoomIssue> findAllIssues();

    @Query("SELECT ri FROM RoomIssue ri WHERE ri.user.id = :userId ORDER BY ri.createdAt DESC")
    List<RoomIssue> findByUserId(Long userId);

    @Query("SELECT ri FROM RoomIssue ri WHERE ri.room.id = :roomId ORDER BY ri.createdAt DESC")
    List<RoomIssue> findByRoomId(Long roomId);
}
