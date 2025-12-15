package com.rut.booking.config;

import com.rut.booking.models.entities.Role;
import com.rut.booking.models.entities.Room;
import com.rut.booking.models.entities.User;
import com.rut.booking.models.enums.RoleType;
import com.rut.booking.models.enums.RoomType;
import com.rut.booking.repository.RoleRepository;
import com.rut.booking.repository.RoomRepository;
import com.rut.booking.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           RoomRepository roomRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initRoles();
        initUsers();
        initRooms();
    }

    private void initRoles() {
        for (RoleType roleType : RoleType.values()) {
            if (!roleRepository.existsByCode(roleType)) {
                Role role = new Role(roleType);
                roleRepository.save(role);
            }
        }
    }

    private void initUsers() {
        // Create Admin user
        if (!userRepository.existsByEmail("admin@rut-miit.ru")) {
            User admin = new User();
            admin.setEmail("admin@rut-miit.ru");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setFirstName("Admin");
            admin.setLastName("System");
            admin.setPhone("+7 (499) 978-33-33");
            admin.setRole(roleRepository.findByCode(RoleType.ADMIN).orElseThrow());
            admin.setIsActive(true);
            userRepository.save(admin);
        }

        // Create Dispatcher user
        if (!userRepository.existsByEmail("dispatcher@rut-miit.ru")) {
            User dispatcher = new User();
            dispatcher.setEmail("dispatcher@rut-miit.ru");
            dispatcher.setPasswordHash(passwordEncoder.encode("dispatcher123"));
            dispatcher.setFirstName("Maria");
            dispatcher.setLastName("Ivanova");
            dispatcher.setMiddleName("Sergeevna");
            dispatcher.setPhone("+7 (499) 978-34-34");
            dispatcher.setRole(roleRepository.findByCode(RoleType.DISPATCHER).orElseThrow());
            dispatcher.setIsActive(true);
            userRepository.save(dispatcher);
        }

        // Create Teacher users
        if (!userRepository.existsByEmail("teacher@rut-miit.ru")) {
            User teacher = new User();
            teacher.setEmail("teacher@rut-miit.ru");
            teacher.setPasswordHash(passwordEncoder.encode("teacher123"));
            teacher.setFirstName("Ivan");
            teacher.setLastName("Petrov");
            teacher.setMiddleName("Aleksandrovich");
            teacher.setPhone("+7 (499) 978-35-35");
            teacher.setRole(roleRepository.findByCode(RoleType.TEACHER).orElseThrow());
            teacher.setIsActive(true);
            userRepository.save(teacher);
        }

        if (!userRepository.existsByEmail("teacher2@rut-miit.ru")) {
            User teacher2 = new User();
            teacher2.setEmail("teacher2@rut-miit.ru");
            teacher2.setPasswordHash(passwordEncoder.encode("teacher123"));
            teacher2.setFirstName("Olga");
            teacher2.setLastName("Sidorova");
            teacher2.setMiddleName("Nikolaevna");
            teacher2.setPhone("+7 (499) 978-36-36");
            teacher2.setRole(roleRepository.findByCode(RoleType.TEACHER).orElseThrow());
            teacher2.setIsActive(true);
            userRepository.save(teacher2);
        }
    }

    private void initRooms() {
        // Building A rooms (first digit = 1)
        createRoom("1101", RoomType.LECTURE, 120, true, true, true,
                "Large lecture hall with modern equipment");
        createRoom("1102", RoomType.LECTURE, 100, false, true, true,
                "Medium lecture hall");
        createRoom("1201", RoomType.COMPUTER, 30, true, true, true,
                "Computer lab with 30 workstations");
        createRoom("1202", RoomType.SEMINAR, 25, false, true, true,
                "Seminar room for group discussions");
        createRoom("1301", RoomType.LAB, 20, true, true, false,
                "Physics laboratory");
        createRoom("1302", RoomType.CONFERENCE, 15, false, true, true,
                "Conference room for meetings");

        // Building B rooms (first digit = 2)
        createRoom("2101", RoomType.LECTURE, 150, true, true, true,
                "Main lecture auditorium");
        createRoom("2102", RoomType.LECTURE, 80, false, true, true,
                "Standard lecture room");
        createRoom("2201", RoomType.COMPUTER, 40, true, true, true,
                "Large computer lab");
        createRoom("2202", RoomType.SEMINAR, 30, false, true, true,
                "Seminar room");
        createRoom("2301", RoomType.LAB, 25, true, true, true,
                "Chemistry laboratory");
        createRoom("2302", RoomType.LAB, 25, true, false, true,
                "Electronics laboratory");

        // Building C rooms (first digit = 3)
        createRoom("3101", RoomType.LECTURE, 60, false, true, true,
                "Compact lecture room");
        createRoom("3102", RoomType.CONFERENCE, 20, false, true, true,
                "Meeting room");
        createRoom("3201", RoomType.COMPUTER, 25, true, true, true,
                "Programming lab");
        createRoom("3202", RoomType.SEMINAR, 20, false, true, true,
                "Small seminar room");

        // Building D rooms (first digit = 4)
        createRoom("4101", RoomType.LECTURE, 200, true, true, true,
                "Large auditorium for conferences");
        createRoom("4201", RoomType.COMPUTER, 35, true, true, true,
                "CAD/CAM laboratory");
        createRoom("4301", RoomType.LAB, 30, true, true, true,
                "Engineering workshop");
    }

    private void createRoom(String number, RoomType type, int capacity,
                            boolean hasComputers, boolean hasProjector, boolean hasWhiteboard,
                            String description) {
        if (!roomRepository.existsByNumber(number)) {
            Room room = new Room();
            room.setNumber(number);
            room.setRoomType(type);
            room.setCapacity(capacity);
            room.setHasComputers(hasComputers);
            room.setHasProjector(hasProjector);
            room.setHasWhiteboard(hasWhiteboard);
            room.setDescription(description);
            room.setIsActive(true);
            roomRepository.save(room);
        }
    }
}
