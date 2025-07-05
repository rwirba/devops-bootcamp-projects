package com.ezlearn.services;

import org.junit.jupiter.api.Assertions;
import org.springframework.stereotype.Service;

@Service
public class CourseService {
    
    public String getCourseDetails(String courseId) {
        // Implementation here
        return "Course details for " + courseId;
    }
    
    // Example testable method
    public int calculateTotalLessons(int modules, int lessonsPerModule) {
        return modules * lessonsPerModule;
    }
}