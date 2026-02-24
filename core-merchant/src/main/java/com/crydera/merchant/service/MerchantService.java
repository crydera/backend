package com.crydera.merchant.service;

import com.crydera.merchant.api.dto.AccountDtos;
import com.crydera.merchant.api.dto.AuthDtos;
import com.crydera.merchant.domain.Merchant;
import com.crydera.merchant.repo.MerchantRepository;
import com.crydera.merchant.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthDtos.TokenResponse register(AuthDtos.RegisterRequest req) {
        if (merchantRepository.existsByEmail(req.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "email already registered");
        }
        Merchant m = Merchant.builder()
                .email(req.email())
                .passwordHash(passwordEncoder.encode(req.password()))
                .company(req.company() != null && !req.company().isBlank() ? req.company().trim() : null)
                .build();
        m = merchantRepository.save(m);
        String token = jwtService.issue(m.getId());
        return new AuthDtos.TokenResponse(token, jwtService.ttlSeconds());
    }

    public AuthDtos.TokenResponse login(AuthDtos.LoginRequest req) {
        Merchant m = merchantRepository.findByEmail(req.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));
        if (!passwordEncoder.matches(req.password(), m.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }
        return new AuthDtos.TokenResponse(jwtService.issue(m.getId()), jwtService.ttlSeconds());
    }

    @Transactional
    public void changePassword(UUID merchantId, AuthDtos.ChangePasswordRequest req) {
        Merchant m = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "not authenticated"));
        if (!passwordEncoder.matches(req.currentPassword(), m.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid current password");
        }
        m.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        merchantRepository.save(m);
    }

    public void logout(UUID merchantId, String token) {
        
    }

    public AccountDtos.AccountResponse getAccount(UUID merchantId) {
        Merchant m = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "merchant not found"));
        return new AccountDtos.AccountResponse(m.getId(), m.getEmail(), m.getCompany(), m.getCreatedAt());
    }
}
