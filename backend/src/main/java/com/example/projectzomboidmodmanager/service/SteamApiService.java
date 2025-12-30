package com.example.projectzomboidmodmanager.service;

import com.example.projectzomboidmodmanager.dto.SteamApiResponse;
import com.example.projectzomboidmodmanager.dto.SteamPublishedFileDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class SteamApiService {

    @Value("${steam.api.url}")
    private String steamApiUrl;

    private final RestTemplate restTemplate;

    public SteamApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetch details for a single mod by workshop ID.
     *
     * @param workshopId The Steam Workshop ID
     * @return SteamPublishedFileDetails or null if not found/error
     */
    public SteamPublishedFileDetails fetchSingleModDetails(String workshopId) {
        rateLimit();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<String> request = new HttpEntity<>(buildFormData(List.of(workshopId)), headers);

            ResponseEntity<SteamApiResponse> response = restTemplate.postForEntity(
                    steamApiUrl,
                    request,
                    SteamApiResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<SteamPublishedFileDetails> details = response.getBody().getResponse().getPublishedfiledetails();
                if (details != null && !details.isEmpty()) {
                    return details.get(0);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch mod details for workshop ID " + workshopId + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Fetch details for multiple mods in a single batch API call.
     *
     * @param workshopIds List of Steam Workshop IDs
     * @return List of SteamPublishedFileDetails (may contain null entries for failed items)
     */
    public List<SteamPublishedFileDetails> fetchBatchModDetails(List<String> workshopIds) {
        if (workshopIds == null || workshopIds.isEmpty()) {
            return List.of();
        }

        rateLimit();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");

            HttpEntity<String> request = new HttpEntity<>(buildFormData(workshopIds), headers);

            ResponseEntity<SteamApiResponse> response = restTemplate.postForEntity(
                    steamApiUrl,
                    request,
                    SteamApiResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<SteamPublishedFileDetails> details = response.getBody().getResponse().getPublishedfiledetails();
                if (details != null) {
                    return details;
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch batch mod details: " + e.getMessage());
        }
        return List.of();
    }

    /**
     * Build form data for the Steam API request.
     * Format: itemcount=N&publishedfileids[0]=id1&publishedfileids[1]=id2&...
     */
    private String buildFormData(List<String> workshopIds) {
        StringBuilder formData = new StringBuilder();
        formData.append("itemcount=").append(workshopIds.size());

        for (int i = 0; i < workshopIds.size(); i++) {
            formData.append("&publishedfileids[").append(i).append("]=").append(workshopIds.get(i));
        }

        return formData.toString();
    }

    /**
     * Rate limiting - delay between API calls to avoid being throttled.
     */
    private void rateLimit() {
        try {
            Thread.sleep(100); // 100ms delay
        } catch (InterruptedException ignored) {
        }
    }
}
