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
     * @param courseId The course ID
     * @param courseTitle The course title
     */
    public Course(final String courseId, final String courseTitle) {
        this.id = courseId;
        this.title = courseTitle;
        this.description = "";
        this.durationHours = 0;
        this.published = false;
    }

    /**
     * Full constructor for all course fields.
     * @param courseId The course ID
     * @param courseTitle The course title
     * @param courseDescription The course description
     * @param hoursDuration The duration in hours
     * @param publishStatus The publication status
     */
    public Course(final String courseId, 
                 final String courseTitle, 
                 final String courseDescription,
                 final int hoursDuration,
                 final boolean publishStatus) {
        this.id = courseId;
        this.title = courseTitle;
        this.description = courseDescription;
        this.durationHours = hoursDuration;
        this.published = publishStatus;
    }

    public String getId() { 
        return id; 
    }

    public void setId(final String courseId) { 
        this.id = courseId; 
    }

    public String getTitle() { 
        return title; 
    }

    public void setTitle(final String courseTitle) { 
        this.title = courseTitle; 
    }

    public String getDescription() { 
        return description; 
    }

    public void setDescription(final String courseDescription) { 
        this.description = courseDescription; 
    }

    public int getDurationHours() { 
        return durationHours; 
    }

    public void setDurationHours(final int hoursDuration) { 
        this.durationHours = hoursDuration; 
    }

    public boolean isPublished() { 
        return published; 
    }

    public void setPublished(final boolean publishStatus) { 
        this.published = publishStatus; 
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Course course = (Course) o;
        return Objects.equals(id, course.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Course{"
                + "id='" + id + '\''
                + ", title='" + title + '\''
                + ", description='" + description + '\''
                + ", durationHours=" + durationHours
                + ", published=" + published
                + '}';
    }
}