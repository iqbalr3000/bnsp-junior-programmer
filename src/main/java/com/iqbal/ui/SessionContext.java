package com.iqbal.ui;

import com.iqbal.model.Staff;

/**
 * Holder statis sederhana untuk staff yang sedang login.
 * Bukan mekanisme session penuh - cukup untuk kebutuhan menampilkan
 * nama staff yang login di header main-layout.
 */
public final class SessionContext {

    private static Staff currentStaff;

    private SessionContext() {
    }

    public static Staff getCurrentStaff() {
        return currentStaff;
    }

    public static void setCurrentStaff(Staff staff) {
        currentStaff = staff;
    }
}
