package com.rut.booking.services;

import com.rut.booking.dto.BookingCreateRequest;
import com.rut.booking.dto.BookingDto;
import com.rut.booking.dto.CalendarEventDto;
import com.rut.booking.dto.DtoMapper;
import com.rut.booking.models.entities.Booking;
import com.rut.booking.models.entities.Room;
import com.rut.booking.models.entities.User;
import com.rut.booking.models.enums.BookingStatus;
import com.rut.booking.models.enums.ClassPeriod;
import com.rut.booking.models.exceptions.BookingConflictException;
import com.rut.booking.models.exceptions.ResourceNotFoundException;
import com.rut.booking.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final RoomService roomService;
    private final PdfGenerationService pdfGenerationService;
    private final DtoMapper dtoMapper;

    public BookingService(BookingRepository bookingRepository, UserService userService,
                          RoomService roomService, PdfGenerationService pdfGenerationService,
                          DtoMapper dtoMapper) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.roomService = roomService;
        this.pdfGenerationService = pdfGenerationService;
        this.dtoMapper = dtoMapper;
    }

    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", id));
    }

    public BookingDto getBookingById(Long id) {
        return dtoMapper.toBookingDto(findById(id));
    }

    public List<BookingDto> getBookingsByTeacher(Long teacherId) {
        return bookingRepository.findByTeacherIdOrderByBookingDateDescCreatedAtDesc(teacherId).stream()
                .map(dtoMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsByRoom(Long roomId) {
        return bookingRepository.findByRoomId(roomId).stream()
                .map(dtoMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status).stream()
                .map(dtoMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAllOrderByCreatedAtDesc().stream()
                .map(dtoMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public List<BookingDto> filterBookings(BookingStatus status, Long roomId, Long teacherId,
                                           LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findWithFilters(status, roomId, teacherId, startDate, endDate).stream()
                .map(dtoMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    public List<CalendarEventDto> getCalendarEvents(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByDateRange(startDate, endDate).stream()
                .map(dtoMapper::toCalendarEventDto)
                .collect(Collectors.toList());
    }

    public List<CalendarEventDto> getRoomCalendarEvents(Long roomId, LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByRoomAndDateRange(roomId, startDate, endDate).stream()
                .map(dtoMapper::toCalendarEventDto)
                .collect(Collectors.toList());
    }

    public boolean isRoomAvailable(Long roomId, LocalDate date, ClassPeriod period) {
        return !bookingRepository.isRoomBookedForPeriod(roomId, date, period);
    }

    @Transactional
    public BookingDto createBooking(Long teacherId, BookingCreateRequest request) {
        // Check if room is available
        if (!isRoomAvailable(request.getRoomId(), request.getBookingDate(), request.getClassPeriod())) {
            Room room = roomService.findById(request.getRoomId());
            throw new BookingConflictException(
                    room.getNumber(),
                    request.getBookingDate().toString(),
                    request.getClassPeriod().getTimeRange()
            );
        }

        User teacher = userService.findById(teacherId);
        Room room = roomService.findById(request.getRoomId());

        Booking booking = new Booking();
        booking.setTeacher(teacher);
        booking.setRoom(room);
        booking.setBookingDate(request.getBookingDate());
        booking.setClassPeriod(request.getClassPeriod());
        booking.setPurpose(request.getPurpose());
        booking.setNotes(request.getNotes());
        booking.setStatus(BookingStatus.CREATED);

        booking = bookingRepository.save(booking);

        // Auto-approve the booking since the room is available
        return autoApproveBooking(booking);
    }

    @Transactional
    public BookingDto autoApproveBooking(Booking booking) {
        // Check again if the room is still available (double-check)
        if (!isRoomAvailable(booking.getRoom().getId(), booking.getBookingDate(), booking.getClassPeriod())) {
            booking.setStatus(BookingStatus.REJECTED);
            booking.setRejectionReason("Room became unavailable");
            booking.setProcessedAt(LocalDateTime.now());
            return dtoMapper.toBookingDto(bookingRepository.save(booking));
        }

        // Approve the booking
        booking.setStatus(BookingStatus.APPROVED);
        booking.setProcessedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        // Generate PDF
        try {
            String pdfPath = pdfGenerationService.generateBookingConfirmationPdf(booking);
            booking.setPdfFilePath(pdfPath);
            booking.setPdfGeneratedAt(LocalDateTime.now());
            booking = bookingRepository.save(booking);
        } catch (Exception e) {
            // Log error but don't fail the booking
            System.err.println("Failed to generate PDF for booking " + booking.getId() + ": " + e.getMessage());
        }

        return dtoMapper.toBookingDto(booking);
    }

    @Transactional
    public BookingDto rejectBooking(Long bookingId, String reason, Long processedByUserId) {
        Booking booking = findById(bookingId);
        if (booking.getStatus() != BookingStatus.CREATED && booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Cannot reject booking with status: " + booking.getStatus());
        }

        User processedBy = processedByUserId != null ? userService.findById(processedByUserId) : null;

        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectionReason(reason);
        booking.setProcessedBy(processedBy);
        booking.setProcessedAt(LocalDateTime.now());

        return dtoMapper.toBookingDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto cancelBooking(Long bookingId, Long userId) {
        Booking booking = findById(bookingId);

        // Only the booking owner can cancel their booking
        if (!booking.getTeacher().getId().equals(userId)) {
            throw new IllegalStateException("Only the booking owner can cancel the booking");
        }

        if (!booking.canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel booking with status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setProcessedAt(LocalDateTime.now());

        return dtoMapper.toBookingDto(bookingRepository.save(booking));
    }

    public Long countByStatus(BookingStatus status) {
        return bookingRepository.countByStatus(status);
    }

    public byte[] getBookingPdf(Long bookingId) {
        Booking booking = findById(bookingId);
        if (booking.getStatus() != BookingStatus.APPROVED) {
            throw new IllegalStateException("PDF is only available for approved bookings");
        }

        if (booking.getPdfFilePath() == null) {
            // Generate PDF if not exists
            try {
                String pdfPath = pdfGenerationService.generateBookingConfirmationPdf(booking);
                booking.setPdfFilePath(pdfPath);
                booking.setPdfGeneratedAt(LocalDateTime.now());
                bookingRepository.save(booking);
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate PDF: " + e.getMessage(), e);
            }
        }

        return pdfGenerationService.readPdfFile(booking.getPdfFilePath());
    }
}
