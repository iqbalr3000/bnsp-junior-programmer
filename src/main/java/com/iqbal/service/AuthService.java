package com.iqbal.service;

import com.iqbal.model.Staff;
import com.iqbal.repository.StaffRepository;
import com.iqbal.service.exception.AuthenticationException;
import com.iqbal.util.PasswordHasher;

public class AuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "Username atau password tidak valid";

    private final StaffRepository staffRepository;

    public AuthService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public Staff login(String username, String password) {
        Staff staff = staffRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException(INVALID_CREDENTIALS_MESSAGE));
        if (!PasswordHasher.matches(password, staff.getPasswordHash())) {
            throw new AuthenticationException(INVALID_CREDENTIALS_MESSAGE);
        }
        return staff;
    }
}
