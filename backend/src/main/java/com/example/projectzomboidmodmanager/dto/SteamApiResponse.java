package com.example.projectzomboidmodmanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SteamApiResponse {

    private SteamResponse response;

    public SteamApiResponse() {
    }

    public SteamApiResponse(SteamResponse response) {
        this.response = response;
    }

    public SteamResponse getResponse() {
        return response;
    }

    public void setResponse(SteamResponse response) {
        this.response = response;
    }
}
