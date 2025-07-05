package com.ezlearn.models;

import java.util.Objects;

public class Course {
    private String id;
    private String title;
    private String description;
    private int durationHours;
    private boolean published;

    // Default constructor (required by JPA/Spring)
    public Course() {}

    // All-args constructor
    public Course(String id, String title, String description, int durationHours, boolean published) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.durationHours = durationHours;
        this.published = published;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    // equals() and hashCode()
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

    // toString()
    @Override
    public String toString() {
        return "Course{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", durationHours=" + durationHours +
                ", published=" + published +
                '}';
    }
}