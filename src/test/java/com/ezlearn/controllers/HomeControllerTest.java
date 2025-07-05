package com.ezlearn.controllers;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

public class HomeControllerTest {
    
    @Test
    public void testHomePage() {
        HomeController controller = new HomeController();
        String viewName = controller.home();
        assertEquals("index", viewName);
    }
}