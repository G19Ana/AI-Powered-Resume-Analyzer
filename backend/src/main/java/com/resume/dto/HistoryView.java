package com.resume.dto;

import java.time.LocalDateTime;

public record HistoryView(Long id, LocalDateTime createdAt, String candidateName, String jobTitle, int matchScore) {}
