package com.example.customerdisplayhandler.model;

import com.google.gson.annotations.SerializedName;

public class Terminal {
    @SerializedName("terminal_id")
    private final String terminalId;
    @SerializedName("terminal_name")
    private final String terminalName;
    @SerializedName("download_url")
    private final String downloadUrl;
    @SerializedName("backup_time")
    private final String backupTime;
    @SerializedName("file_type")
    private final String fileType;
    @SerializedName("app_type")
    private final String appType;
    @SerializedName("current_app_version")
    private final String currentAppVersion;

    private Terminal(Builder builder) {
        this.terminalId = builder.terminalId;
        this.terminalName = builder.terminalName;
        this.downloadUrl = builder.downloadUrl;
        this.backupTime = builder.backupTime;
        this.fileType = builder.fileType;
        this.appType = builder.appType;
        this.currentAppVersion = builder.currentAppVersion;
    }

    // Getters
    public String getTerminalId() {
        return terminalId;
    }

    public String getTerminalName() {
        return terminalName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getBackupTime() {
        return backupTime;
    }

    public String getFileType() {
        return fileType;
    }

    public String getAppType() {
        return appType;
    }

    public String getCurrentAppVersion() {
        return currentAppVersion;
    }

    public static class Builder {
        private String terminalId;
        private String terminalName;
        private String downloadUrl;
        private String backupTime;
        private String fileType;
        private String appType;
        private String currentAppVersion;

        public Builder setTerminalId(String terminalId) {
            this.terminalId = terminalId;
            return this;
        }

        public Builder setTerminalName(String terminalName) {
            this.terminalName = terminalName;
            return this;
        }

        public Builder setDownloadUrl(String downloadUrl) {
            this.downloadUrl = downloadUrl;
            return this;
        }

        public Builder setBackupTime(String backupTime) {
            this.backupTime = backupTime;
            return this;
        }

        public Builder setFileType(String fileType) {
            this.fileType = fileType;
            return this;
        }

        public Builder setAppType(String appType) {
            this.appType = appType;
            return this;
        }

        public Builder setCurrentAppVersion(String currentAppVersion) {
            this.currentAppVersion = currentAppVersion;
            return this;
        }

        public Terminal build() {
            return new Terminal(this);
        }
    }
}
