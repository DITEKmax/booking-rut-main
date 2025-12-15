package com.rut.booking.repository;

import com.rut.booking.models.entities.Booking;
import com.rut.booking.models.enums.BookingStatus;
import com.rut.booking.models.enums.ClassPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByTeacherId(Long teacherId);

    List<Booking> findByTeacherIdOrderByBookingDateDescCreatedAtDesc(Long teacherId);

    List<Booking> findByRoomId(Long roomId);

    List<Booking> findByStatus(BookingStatus status);

    List<Booking> findByBookingDate(LocalDate date);

    List<Booking> findByRoomIdAndBookingDate(Long roomId, LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.bookingDate = :date AND b.classPeriod = :period AND b.status != 'CANCELLED' AND b.status != 'REJECTED'")
    List<Booking> findConflictingBookings(@Param("roomId") Long roomId,
                                          @Param("date") LocalDate date,
                                          @Param("period") ClassPeriod period);

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.bookingDate = :date AND b.status IN ('APPROVED', 'PENDING', 'CREATED')")
    List<Booking> findActiveBookingsForRoomOnDate(@Param("roomId") Long roomId,
                                                   @Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.bookingDate BETWEEN :startDate AND :endDate ORDER BY b.bookingDate, b.startTime")
    List<Booking> findByDateRange(@Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.bookingDate BETWEEN :startDate AND :endDate AND b.status IN ('APPROVED', 'PENDING', 'CREATED') ORDER BY b.bookingDate, b.startTime")
    List<Booking> findByRoomAndDateRange(@Param("roomId") Long roomId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM Booking b WHERE " +
            "(:status IS NULL OR b.status = :status) AND " +
            "(:roomId IS NULL OR b.room.id = :roomId) AND " +
            "(:teacherId IS NULL OR b.teacher.id = :teacherId) AND " +
            "(:startDate IS NULL OR b.bookingDate >= :startDate) AND " +
            "(:endDate IS NULL OR b.bookingDate <= :endDate) " +
            "ORDER BY b.createdAt DESC")
    List<Booking> findWithFilters(@Param("status") BookingStatus status,
                                  @Param("roomId") Long roomId,
                                  @Param("teacherId") Long teacherId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Long countByStatus(@Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b ORDER BY b.createdAt DESC")
    List<Booking> findAllOrderByCreatedAtDesc();

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.room.id = :roomId AND b.bookingDate = :date AND b.classPeriod = :period AND b.status IN ('APPROVED', 'PENDING', 'CREATED')")
    boolean isRoomBookedForPeriod(@Param("roomId") Long roomId,
                                  @Param("date") LocalDate date,
                                  @Param("period") ClassPeriod period);
}
