package com.iqbal.repository;

import com.iqbal.model.Staff;

import java.util.Optional;

public interface StaffRepository {

    Optional<Staff> findByUsername(String username);
}
