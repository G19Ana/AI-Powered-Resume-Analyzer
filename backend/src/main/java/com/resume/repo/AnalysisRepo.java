package com.resume.repo;

import com.resume.entity.ResumeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisRepo extends JpaRepository<ResumeAnalysis, Long> {
    List<ResumeAnalysis> findAllByOrderByCreatedAtDesc();
}
