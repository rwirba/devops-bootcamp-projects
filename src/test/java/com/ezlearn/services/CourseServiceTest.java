package com.ezlearn.services;

import com.ezlearn.models.Course;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CourseServiceTest {
    @Test
    void getAllCourses_ReturnsCourses() {
        CourseService service = new CourseService();
        assertEquals(2, service.getAllCourses().size());
    }

    @Test
    void getCourseById_ReturnsCourse() {
        CourseService service = new CourseService();
        Course course = service.getCourseById("TEST-123");
        assertEquals("TEST-123", course.getId());
    }
}