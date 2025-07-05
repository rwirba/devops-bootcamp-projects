package com.ezlearn.controllers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HomeControllerTest {
    @Test
    public void testHomePage() {
        HomeController controller = new HomeController();
        String viewName = controller.home();
        Assertions.assertEquals("index", viewName);
    }
}