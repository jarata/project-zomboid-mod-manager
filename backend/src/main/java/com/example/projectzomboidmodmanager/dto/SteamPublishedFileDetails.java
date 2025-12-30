package com.example.projectzomboidmodmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamPublishedFileDetails {

    private int result;
    private String publishedfileid;
    private String creator;
    private String filename;
    private long file_size;
    private long time_created;
    private long time_updated;
    private String title;
    private String description;
    private String preview_url;
    private List<SteamTag> tags;

    public SteamPublishedFileDetails() {
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getPublishedfileid() {
        return publishedfileid;
    }

    public void setPublishedfileid(String publishedfileid) {
        this.publishedfileid = publishedfileid;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getFile_size() {
        return file_size;
    }

    public void setFile_size(long file_size) {
        this.file_size = file_size;
    }

    public long getTime_created() {
        return time_created;
    }

    public void setTime_created(long time_created) {
        this.time_created = time_created;
    }

    public long getTime_updated() {
        return time_updated;
    }

    public void setTime_updated(long time_updated) {
        this.time_updated = time_updated;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreview_url() {
        return preview_url;
    }

    public void setPreview_url(String preview_url) {
        this.preview_url = preview_url;
    }

    public List<SteamTag> getTags() {
        return tags;
    }

    public void setTags(List<SteamTag> tags) {
        this.tags = tags;
    }
}
