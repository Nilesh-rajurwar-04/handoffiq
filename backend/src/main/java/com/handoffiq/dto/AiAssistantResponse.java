package com.handoffiq.dto;

import java.util.List;

public class AiAssistantResponse {

    private String answer;
    private List<SourceReference> sourceReferences;
    private int confidenceScore; // 0 - 100
    private boolean fallbackUsed;
    private String matchedIntent;

    public AiAssistantResponse() {}

    public static class SourceReference {
        private String type; // MEETING, TASK, DECISION, HANDOVER, DOCUMENT
        private Long id;
        private String title;
        private String snippet;

        public SourceReference() {}
        public SourceReference(String type, Long id, String title, String snippet) {
            this.type = type;
            this.id = id;
            this.title = title;
            this.snippet = snippet;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<SourceReference> getSourceReferences() { return sourceReferences; }
    public void setSourceReferences(List<SourceReference> sourceReferences) { this.sourceReferences = sourceReferences; }
    public int getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(int confidenceScore) { this.confidenceScore = confidenceScore; }
    public boolean isFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
    public String getMatchedIntent() { return matchedIntent; }
    public void setMatchedIntent(String matchedIntent) { this.matchedIntent = matchedIntent; }
}
