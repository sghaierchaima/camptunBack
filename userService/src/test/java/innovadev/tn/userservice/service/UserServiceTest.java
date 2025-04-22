package innovadev.tn.userservice.service;

import innovadev.tn.userservice.enums.Role;
import innovadev.tn.userservice.entity.User;
import innovadev.tn.userservice.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encoded_password")
                .role(Role.NORMAL_USER)
                .name("Test")
                .prename("User")
                .signUpDate(LocalDate.now())
                .build();
    }

    @Test
    void testCreateUser() {
        when(passwordEncoder.encode("password")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User created = userService.createUser(User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .build());

        Assertions.assertNotNull(created);
        Assertions.assertEquals("testuser", created.getUsername());
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testGetUserByIdFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserById(1L);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<User> users = userService.getAllUsers();

        Assertions.assertEquals(1, users.size());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void testLoadUserByUsernameSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        var userDetails = userService.loadUserByUsername("testuser");

        Assertions.assertNotNull(userDetails);
        Assertions.assertEquals("testuser", userDetails.getUsername());
    }

    @Test
    void testLoadUserByUsernameNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        Assertions.assertThrows(RuntimeException.class, () -> userService.loadUserByUsername("unknown"));
    }
}
