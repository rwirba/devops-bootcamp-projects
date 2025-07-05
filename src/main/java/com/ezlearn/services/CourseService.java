package com.ezlearn.services;

import com.ezlearn.models.Course;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service for managing courses.
 */
@Service
public class CourseService {
    
    /**
     * Gets all available courses.
     * @return List of courses
     */
    public List<Course> getAllCourses() {
        return List.of(
            new Course("JAVA-101", "Java Fundamentals"),
            new Course("SPRING-201", "Spring Boot")
        );
    }

    /**
     * Gets a course by its ID.
     * @param courseId The course ID to find
     * @return The matching course
     */
    public Course getCourseById(String courseId) {
        return new Course(courseId, "Sample Course");
    }
}