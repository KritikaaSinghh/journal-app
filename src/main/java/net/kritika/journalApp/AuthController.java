package net.kritika.journalApp;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final SecurityContextRepository contextRepository;

    public AuthController(
            UserService userService,
            SecurityContextRepository contextRepository) {
        this.userService = userService;
        this.contextRepository = contextRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestBody Map<String, String> request) {

        String username = request.get("username");
        String password = request.get("password");

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "Username and password are required.")
            );
        }

        try {
            User user = userService.registerUser(username, password);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "User registered successfully!",
                            "username", user.getUsername()
                    )
            );

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        String username = request.get("username");
        String password = request.get("password");

        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message",
                            "Username and password are required.")
            );
        }

        if (!userService.validateUser(username, password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Map.of("message", "Invalid username or password.")
            );
        }

        // Invalidate any previous session before creating a new login session.
        HttpSession oldSession = httpRequest.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session = httpRequest.getSession(true);

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        Collections.emptyList()
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        contextRepository.saveContext(
                context, httpRequest, httpResponse
        );

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful!",
                        "username", username
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(
            HttpServletRequest request,
            HttpServletResponse response) {

        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.setHeader(
                "Set-Cookie",
                "JSESSIONID=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax"
        );

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully!")
        );
    }
}
