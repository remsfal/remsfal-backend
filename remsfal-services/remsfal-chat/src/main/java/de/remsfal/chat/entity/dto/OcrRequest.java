package de.remsfal.chat.entity.dto;

public class OcrRequest {
    private String bucket;
    private String fileName;
    private String sessionId;
    private String messageId;


    public OcrRequest(String bucket, String fileName, String sessionId, String messageId) {
        this.bucket = bucket;
        this.fileName = fileName;
        this.sessionId = sessionId;
        this.messageId = messageId;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }
}
