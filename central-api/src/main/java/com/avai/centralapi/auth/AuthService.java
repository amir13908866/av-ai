package com.avai.centralapi.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();
    private final Map<String, PendingCode> pendingCodes = new ConcurrentHashMap<>();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(UserRepository users) {
        this.users = users;
    }

    public User register(String username, String displayName, String password) {
        String u = normalize(username);
        if (u.length() < 3 || u.length() > 32 || !u.matches("[A-Za-z0-9_]+")) {
            throw new IllegalArgumentException("Username must be 3-32 chars using A-Z, a-z, 0-9 or _");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (users.existsByUsernameIgnoreCase(u)) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setUsername(u);
        user.setDisplayName(displayName == null || displayName.isBlank() ? u : displayName.trim());
        user.setPasswordHash(encoder.encode(password));
        return users.save(user);
    }

    public CodeResult requestCode(String username, String password) {
        User user = users.findByUsernameIgnoreCase(normalize(username))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!encoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        String challengeId = UUID.randomUUID().toString();
        String code = String.format("%06d", random.nextInt(1_000_000));
        Instant expiresAt = Instant.now().plusSeconds(300);
        pendingCodes.put(challengeId, new PendingCode(user.getUsername(), code, expiresAt));
        return new CodeResult(challengeId, code, expiresAt.toString());
    }

    public SessionResult verifyCode(String challengeId, String code) {
        PendingCode pending = pendingCodes.remove(challengeId);
        if (pending == null || Instant.now().isAfter(pending.expiresAt)) {
            throw new IllegalArgumentException("Code expired or invalid");
        }
        if (!pending.code.equals(code)) {
            throw new IllegalArgumentException("Code expired or invalid");
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(pending.username, Instant.now().plusSeconds(86_400)));
        User user = users.findByUsernameIgnoreCase(pending.username).orElseThrow();
        return new SessionResult(token, user.getUsername(), user.getDisplayName());
    }

    public String requireUser(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) throw new IllegalArgumentException("Missing authorization");
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.substring(7).trim() : bearerToken.trim();
        Session s = sessions.get(token);
        if (s == null || Instant.now().isAfter(s.expiresAt)) {
            sessions.remove(token);
            throw new IllegalArgumentException("Session expired");
        }
        return s.username;
    }

    private String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase();
    }

    private record PendingCode(String username, String code, Instant expiresAt) {}
    private record Session(String username, Instant expiresAt) {}
    public record CodeResult(String challengeId, String code, String expiresAt) {}
    public record SessionResult(String token, String username, String displayName) {}
}
