package net.kritika.journalApp;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // Register a new user
    public User registerUser(String username, String password) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException(
                    "Username and password are required."
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists!");
        }

        String hashedPassword = passwordEncoder.encode(password);

        User user = new User(username, hashedPassword);

        return userRepository.save(user);
    }

    // Validate username and password during login
    public boolean validateUser(String username, String password) {

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return false;
        }

        return userRepository.findByUsername(username)
                .map(user -> passwordEncoder.matches(
                        password,
                        user.getPassword()
                ))
                .orElse(false);
    }
}
