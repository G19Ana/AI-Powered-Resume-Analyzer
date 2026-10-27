package com.resume.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "analyses", indexes = @Index(name = "idx_analysis_created", columnList = "createdAt"))
public class ResumeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private String candidateName;

    private String jobTitle;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Column(columnDefinition = "TEXT")
    private String extractedResumeText;

    private int matchScore;

    @Column(columnDefinition = "TEXT")
    private String analysisJson;

    public ResumeAnalysis() {}

    public ResumeAnalysis(String candidateName, String jobTitle, String jobDescription, String extractedResumeText, int matchScore, String analysisJson) {
        this.candidateName = candidateName;
        this.jobTitle = jobTitle;
        this.jobDescription = jobDescription;
        this.extractedResumeText = extractedResumeText;
        this.matchScore = matchScore;
        this.analysisJson = analysisJson;
    }

    public Long getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getCandidateName() { return candidateName; }
    public String getJobTitle() { return jobTitle; }
    public String getJobDescription() { return jobDescription; }
    public String getExtractedResumeText() { return extractedResumeText; }
    public int getMatchScore() { return matchScore; }
    public String getAnalysisJson() { return analysisJson; }
}
