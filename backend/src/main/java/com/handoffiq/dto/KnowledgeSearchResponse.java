package com.handoffiq.dto;

import java.util.List;

public class KnowledgeSearchResponse {

    private String query;
    private String synthesizedAnswer;
    private List<SearchResultItem> results;
    private List<SourceCitation> citations;
    private boolean fallbackUsed;

    public KnowledgeSearchResponse() {}

    public static class SearchResultItem {
        private Long documentId;
        private String title;
        private String category;
        private String fileName;
        private String snippet;
        private Long projectId;
        private String projectName;

        public SearchResultItem() {}
        public SearchResultItem(Long documentId, String title, String category, String fileName, String snippet, Long projectId, String projectName) {
            this.documentId = documentId;
            this.title = title;
            this.category = category;
            this.fileName = fileName;
            this.snippet = snippet;
            this.projectId = projectId;
            this.projectName = projectName;
        }

        public Long getDocumentId() { return documentId; }
        public void setDocumentId(Long documentId) { this.documentId = documentId; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }
        public Long getProjectId() { return projectId; }
        public void setProjectId(Long projectId) { this.projectId = projectId; }
        public String getProjectName() { return projectName; }
        public void setProjectName(String projectName) { this.projectName = projectName; }
    }

    public static class SourceCitation {
        private String sourceTitle;
        private String sourceType;
        private String referenceUrl;

        public SourceCitation() {}
        public SourceCitation(String sourceTitle, String sourceType, String referenceUrl) {
            this.sourceTitle = sourceTitle;
            this.sourceType = sourceType;
            this.referenceUrl = referenceUrl;
        }

        public String getSourceTitle() { return sourceTitle; }
        public void setSourceTitle(String sourceTitle) { this.sourceTitle = sourceTitle; }
        public String getSourceType() { return sourceType; }
        public void setSourceType(String sourceType) { this.sourceType = sourceType; }
        public String getReferenceUrl() { return referenceUrl; }
        public void setReferenceUrl(String referenceUrl) { this.referenceUrl = referenceUrl; }
    }

    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    public String getSynthesizedAnswer() { return synthesizedAnswer; }
    public void setSynthesizedAnswer(String synthesizedAnswer) { this.synthesizedAnswer = synthesizedAnswer; }
    public List<SearchResultItem> getResults() { return results; }
    public void setResults(List<SearchResultItem> results) { this.results = results; }
    public List<SourceCitation> getCitations() { return citations; }
    public void setCitations(List<SourceCitation> citations) { this.citations = citations; }
    public boolean isFallbackUsed() { return fallbackUsed; }
    public void setFallbackUsed(boolean fallbackUsed) { this.fallbackUsed = fallbackUsed; }
}
