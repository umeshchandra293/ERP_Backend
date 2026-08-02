package com.hst.materialmgmt.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.hst.materialmgmt.repository.auth.UserRepository;
import com.hst.materialmgmt.util.JwtUtil;
import reactor.core.publisher.Mono;

@Service
public class AuthService {

    @Autowired private UserRepository  userRepo;
    @Autowired private JwtUtil         jwt;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public record LoginRequest(String username, String password) {}
    public record LoginResponse(String token, String username, String fullName,
                                 String role, String companyId) {}

    public Mono<LoginResponse> login(LoginRequest req) {
        return userRepo.findByUsername(req.username())
            .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
            .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
            .flatMap(u -> userRepo.updateLastLogin(u.getUsername())
                .thenReturn(new LoginResponse(
                    jwt.generate(u.getUsername(), u.getRole(), u.getFullName(), u.getCompanyId()),
                    u.getUsername(), u.getFullName(), u.getRole(), u.getCompanyId()
                )));
    }

    public Mono<Void> changePassword(String username, String oldPass, String newPass) {
        return userRepo.findByUsername(username)
            .filter(u -> encoder.matches(oldPass, u.getPasswordHash()))
            .switchIfEmpty(Mono.error(new RuntimeException("Old password incorrect")))
            .flatMap(u -> userRepo.changePassword(username, encoder.encode(newPass)));
    }

    /** One-time utility — call to get a bcrypt hash for a password */
    public String hashPassword(String raw) { return encoder.encode(raw); }
}