package com.example.job_prep_tracker.controller;

import com.example.job_prep_tracker.JobPrepTrackerApplication;
import com.example.job_prep_tracker.model.Job;
import com.example.job_prep_tracker.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/job")
public class JobController {

    @Autowired
    private JobRepository jobRepository;

    @GetMapping("/hello")
    public String hello(){
        return "hello mayank and anmol ";
    }

    // Get all jobs
    @GetMapping
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    // Get a job by ID
    @GetMapping("/{id}")
    public Optional<Job> getJobById(@PathVariable Long id) {
        return jobRepository.findById(id);
    }

    // Add a new job
    @PostMapping
    public String createJob(@RequestBody Job job) {
        jobRepository.save(job);
        return "job added successfully";
    }

    // Update a job
    @PutMapping("/{id}")
    public String updateJob(@PathVariable Long id, @RequestBody Job jobDetails) {
        jobRepository.findById(id).map(job -> {
            job.setTitle(jobDetails.getTitle());
            job.setCompany(jobDetails.getCompany());
            job.setLocation(jobDetails.getLocation());
            job.setSalary(jobDetails.getSalary());
            job.setDescription(jobDetails.getDescription());
            job.setStatus(jobDetails.getStatus());
            return jobRepository.save(job);
        }).orElseThrow(() -> new RuntimeException("Job not found with id " + id));

        return "job updated successfully";
    }

    // Delete a job
    @DeleteMapping("/{id}")
    public String deleteJob(@PathVariable Long id) {
        jobRepository.deleteById(id);
        return "Job with ID " + id + " has been deleted!";
    }

}
