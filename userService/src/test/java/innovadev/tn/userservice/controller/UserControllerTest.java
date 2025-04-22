package innovadev.tn.userservice.controller;

import innovadev.tn.userservice.entity.User;
import innovadev.tn.userservice.enums.Role;
import innovadev.tn.userservice.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private IUserService userService;
    private UserController userController;
    private User user;

    @BeforeEach
    void setUp() {
        userService = mock(IUserService.class);
        userController = new UserController(userService);

        user = User.builder()
                .id(7L)
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
        when(userService.createUser(any(User.class))).thenReturn(user);

        ResponseEntity<User> response = userController.createUser(user);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
    }

    @Test
    void testGetUserByIdFound() {
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUserById(1L);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userService.getUserById(1L)).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUserById(1L);

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void testGetAllUsers() {
        when(userService.getAllUsers()).thenReturn(List.of(user));

        ResponseEntity<List<User>> response = userController.getAllUsers();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testUpdateUser() {
        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(user);

        ResponseEntity<User> response = userController.updateUser(1L, user);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
    }

    @Test
    void testUpdateAdmin() {
        when(userService.updateAdmin(eq(1L), any(User.class))).thenReturn(user);

        ResponseEntity<User> response = userController.updateAdmin(1L, user);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(user, response.getBody());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userService).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(userService).deleteUser(1L);
    }
}
