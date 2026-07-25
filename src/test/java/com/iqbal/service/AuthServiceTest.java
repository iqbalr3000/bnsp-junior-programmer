package com.iqbal.service;

import com.iqbal.model.Role;
import com.iqbal.model.Staff;
import com.iqbal.repository.StaffRepository;
import com.iqbal.service.exception.AuthenticationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String RAW_PASSWORD = "admin123";

    @Mock
    private StaffRepository staffRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(staffRepository);
    }

    private Staff staffWithPassword(String rawPassword) {
        Staff staff = new Staff();
        staff.setId(1L);
        staff.setUsername("admin");
        staff.setNama("Admin");
        staff.setRole(Role.ADMIN);
        staff.setPasswordHash(BCrypt.hashpw(rawPassword, BCrypt.gensalt(10)));
        return staff;
    }

    @Test
    void login_shouldReturnStaff_whenUsernameDanPasswordBenar() {
        when(staffRepository.findByUsername("admin")).thenReturn(Optional.of(staffWithPassword(RAW_PASSWORD)));

        Staff hasil = authService.login("admin", RAW_PASSWORD);

        assertEquals("admin", hasil.getUsername());
    }

    @Test
    void login_shouldThrowAuthenticationException_whenUsernameTidakDitemukan() {
        when(staffRepository.findByUsername("tidakada")).thenReturn(Optional.empty());

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.login("tidakada", RAW_PASSWORD));
        assertEquals("Username atau password tidak valid", ex.getMessage());
    }

    @Test
    void login_shouldThrowAuthenticationException_whenPasswordSalah() {
        when(staffRepository.findByUsername("admin")).thenReturn(Optional.of(staffWithPassword(RAW_PASSWORD)));

        AuthenticationException ex = assertThrows(AuthenticationException.class,
                () -> authService.login("admin", "passwordSalah"));
        assertEquals("Username atau password tidak valid", ex.getMessage());
    }
}
