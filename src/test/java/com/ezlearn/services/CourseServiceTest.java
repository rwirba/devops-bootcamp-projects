package com.ezlearn.services;

import com.ezlearn.models.Course;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Tests for CourseService.
 */
class CourseServiceTest {

    @Test
    void testGetAllCoursesReturnsCourses() {
        CourseService service = new CourseService();
        Assertions.assertEquals(2, service.getAllCourses().size());
    }

    @Test
    void testGetCourseByIdReturnsCourse() {
        CourseService service = new CourseService();
        Course course = service.getCourseById("TEST-123");
        Assertions.assertEquals("TEST-123", course.getId());
    }
}