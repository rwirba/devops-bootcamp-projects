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
     * Constructs a new Course with all fields.
     * @param id The course identifier
     * @param title The course title
     * @param description The course description
     * @param durationHours The duration in hours
     * @param published The publication status
     */
    public Course(
            final String id,
            final String title,
            final String description,
            final int durationHours,
            final boolean published) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationHours = durationHours;
        this.published = published;
    }

    /**
     * @return the course ID
     */
    public String getId() {
        return id;
    }

    /**
     * @param courseId the ID to set
     */
    public void setId(final String courseId) {
        this.id = courseId;
    }

    /**
     * @return the course title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param courseTitle the title to set
     */
    public void setTitle(final String courseTitle) {
        this.title = courseTitle;
    }

    /**
     * @return the course description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param courseDescription the description to set
     */
    public void setDescription(final String courseDescription) {
        this.description = courseDescription;
    }

    /**
     * @return the duration in hours
     */
    public int getDurationHours() {
        return durationHours;
    }

    /**
     * @param hours the duration to set
     */
    public void setDurationHours(final int hours) {
        this.durationHours = hours;
    }

    /**
     * @return the publication status
     */
    public boolean isPublished() {
        return published;
    }

    /**
     * @param status the publication status to set
     */
    public void setPublished(final boolean status) {
        this.published = status;
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
                + ", durationHours=" + durationHours
                + ", published=" + published
                + '}';
    }
}