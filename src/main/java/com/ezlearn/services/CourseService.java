package com.ezlearn.services;

import org.springframework.stereotype.Service;
import java.util.List;
import com.ezlearn.models.Course;

@Service
public class CourseService {
    
    public List<Course> getAllCourses() {
        // Implementation logic here
        return List.of(
            new Course("Java Fundamentals", "JAVA-101"),
            new Course("Spring Boot", "SPRING-201")
        );
    }

    public Course getCourseById(String courseId) {
        // Implementation logic here
        return new Course("Sample Course", courseId);
    }
}