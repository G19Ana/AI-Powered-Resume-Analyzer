package com.resume.dto;

import java.util.List;

public record AnalysisResult(
    int matchScore,
    String candidateName,
    List<String> keywordGaps,
    List<String> strengths,
    List<String> improvements,
    List<BulletPair> rewrittenBullets
) {}
