package com.example.projectzomboidmodmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamResponse {

    private List<SteamPublishedFileDetails> publishedfiledetails;

    public SteamResponse() {
    }

    public SteamResponse(List<SteamPublishedFileDetails> publishedfiledetails) {
        this.publishedfiledetails = publishedfiledetails;
    }

    public List<SteamPublishedFileDetails> getPublishedfiledetails() {
        return publishedfiledetails;
    }

    public void setPublishedfiledetails(List<SteamPublishedFileDetails> publishedfiledetails) {
        this.publishedfiledetails = publishedfiledetails;
    }
}
