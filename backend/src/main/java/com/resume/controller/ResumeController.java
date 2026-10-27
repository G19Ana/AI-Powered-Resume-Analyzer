package com.resume.controller;

import com.resume.dto.AnalysisResult;
import com.resume.dto.HistoryView;
import com.resume.service.ResumeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeController {

    private final ResumeService service;

    public ResumeController(ResumeService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public AnalysisResult analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("jobTitle") String jobTitle,
            @RequestParam("jobDescription") String jobDescription,
            @RequestHeader(value = "X-Gemini-Key", required = false) String apiKey
    ) throws Exception {
        return service.analyze(file, jobTitle, jobDescription, apiKey);
    }

    @GetMapping("/history")
    public List<HistoryView> getHistory() {
        return service.getHistory();
    }

    @GetMapping("/history/{id}")
    public AnalysisResult getById(@PathVariable Long id) throws Exception {
        return service.getById(id);
    }

    @DeleteMapping("/history/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
