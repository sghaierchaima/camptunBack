package innovadev.tn.userservice.service;

import innovadev.tn.userservice.entity.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(Long id, User userDetails);
    User updateAdmin(Long id, User userDetails);
    void deleteUser(Long id);
    UserDetails loadUserByUsername(String username);
    Optional<User> findByUsername(String username);

}
