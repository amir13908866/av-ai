package com.avai.centralapi.auth;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public ApiResponse register(@RequestBody RegisterRequest req) {
        User user = auth.register(req.username(), req.displayName(), req.password());
        return ApiResponse.ok(new UserView(user.getUsername(), user.getDisplayName()), "Registration successful");
    }

    @PostMapping("/request-code")
    public ApiResponse requestCode(@RequestBody LoginRequest req) {
        return ApiResponse.ok(auth.requestCode(req.username(), req.password()), "Verification code generated");
    }

    @PostMapping("/verify-code")
    public ApiResponse verifyCode(@RequestBody VerifyRequest req) {
        return ApiResponse.ok(auth.verifyCode(req.challengeId(), req.code()), "Login successful");
    }

    public record RegisterRequest(String username, String displayName, String password) {}
    public record LoginRequest(String username, String password) {}
    public record VerifyRequest(String challengeId, String code) {}
    public record UserView(String username, String displayName) {}
    public record ApiResponse(boolean success, String message, Object data) {
        static ApiResponse ok(Object data, String message) { return new ApiResponse(true, message, data); }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse badRequest(IllegalArgumentException ex) {
        return new ApiResponse(false, ex.getMessage(), null);
    }
}
