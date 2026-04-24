package com.company.birthday.dto.gemini;

import java.util.List;

public final class GeminiDto {

    private GeminiDto() {
    }

    public record GenerateContentRequest(List<Content> contents) {
    }

    public record GenerateContentResponse(List<Candidate> candidates) {
    }

    public record Candidate(Content content) {
    }

    public record Content(List<Part> parts) {
    }

    public record Part(String text) {
    }
}

