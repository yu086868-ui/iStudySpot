package com.ycyu.istudyspotbackend;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IstudyspotBackendApplicationTest {

    @Test
    void testClassExists() {
        assertNotNull(IstudyspotBackendApplication.class);
    }

    @Test
    void testPackageStructure() {
        assertEquals("com.ycyu.istudyspotbackend", IstudyspotBackendApplication.class.getPackageName());
    }
}