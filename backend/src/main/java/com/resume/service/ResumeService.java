package com.resume.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume.dto.AnalysisResult;
import com.resume.dto.HistoryView;
import com.resume.entity.ResumeAnalysis;
import com.resume.repo.AnalysisRepo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ResumeService {

    private final PdfService pdfService;
    private final GeminiService geminiService;
    private final AnalysisRepo repo;
    private final ObjectMapper mapper = new ObjectMapper();

    public ResumeService(PdfService pdfService, GeminiService geminiService, AnalysisRepo repo) {
        this.pdfService = pdfService;
        this.geminiService = geminiService;
        this.repo = repo;
    }

    public AnalysisResult analyze(MultipartFile file, String jobTitle, String jobDescription, String key) throws Exception {
        String resumeText = pdfService.extractText(file);
        AnalysisResult result = geminiService.analyze(resumeText, jobDescription, key);
        String json = mapper.writeValueAsString(result);

        repo.save(new ResumeAnalysis(
            result.candidateName(),
            jobTitle,
            jobDescription,
            resumeText,
            result.matchScore(),
            json
        ));

        return result;
    }

    public List<HistoryView> getHistory() {
        return repo.findAllByOrderByCreatedAtDesc().stream()
                .map(a -> new HistoryView(a.getId(), a.getCreatedAt(), a.getCandidateName(), a.getJobTitle(), a.getMatchScore()))
                .toList();
    }

    public AnalysisResult getById(Long id) throws Exception {
        ResumeAnalysis a = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Analysis not found."));
        return mapper.readValue(a.getAnalysisJson(), AnalysisResult.class);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}
