package com.example.job_prep_tracker.model;

import com.example.job_prep_tracker.enums.JobStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "jobs")
public class Job {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String location;
    private double salary;
    private String description;
    private String category;

    @Enumerated(EnumType.STRING) // ✅ Correct placement
    private JobStatus status;

    public Job() {}

    public Job(String title, String company, String location, double salary, String description, String category, JobStatus status) {
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.description = description;
        this.category = category;
        this.status = status; // ✅ Assigning status
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
}
