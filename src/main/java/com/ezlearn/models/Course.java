package com.ezlearn.models;

import java.util.Objects;

/**
 * Represents a course in the e-learning platform.
 */
public class Course {
    /** Unique identifier for the course. */
    private String id;
    
    /** Title of the course. */
    private String title;
    
    /** Detailed description of the course content. */
    private String description;
    
    /** Duration of the course in hours. */
    private int durationHours;
    
    /** Publication status of the course. */
    private boolean published;

    /**
     * Default constructor.
     */
    public Course() { }

    /**
     * Constructor for basic course information.
     * @param id The course ID
     * @param title The course title
     */
    public Course(String id, String title) {
        this.id = id;
        this.title = title;
        this.description = "";
        this.durationHours = 0;
        this.published = false;
    }

    /**
     * Full constructor for all course fields.
     * @param id The course ID
     * @param title The course title
     * @param description The course description
     * @param durationHours The duration in hours
     * @param published The publication status
     */
    public Course(String id, String title, String description, 
                 int durationHours, boolean published) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationHours = durationHours;
        this.published = published;
    }

    // Getters and setters with Javadoc comments
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getDurationHours() { return durationHours; }
    public void setDurationHours(int durationHours) { this.durationHours = durationHours; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course course = (Course) o;
        return Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}