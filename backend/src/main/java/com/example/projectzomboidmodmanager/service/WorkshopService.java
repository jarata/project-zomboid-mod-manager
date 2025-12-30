package com.example.projectzomboidmodmanager.service;

import com.example.projectzomboidmodmanager.dto.SteamPublishedFileDetails;
import com.example.projectzomboidmodmanager.model.ModDetails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WorkshopService {

    @Value("${workshop.collection.url}")
    private String collectionUrlTemplate;

    private final RestTemplate restTemplate;
    private final SteamApiService steamApiService;

    public WorkshopService(RestTemplate restTemplate, SteamApiService steamApiService) {
        this.restTemplate = restTemplate;
        this.steamApiService = steamApiService;
    }

    /**
     * Extracts individual Workshop item IDs from a collection page.
     * NOTE: This must use HTML scraping as there is no Steam API for collections.
     */
    public List<String> getWorkshopIds(String collectionId) {
        List<String> workshopIds = new ArrayList<>();
        String collectionUrl = String.format(collectionUrlTemplate, collectionId);

        try {
            String htmlContent = restTemplate.getForObject(collectionUrl, String.class);
            if (htmlContent != null) {
                Pattern pattern = Pattern.compile("id=\"sharedfile_(\\d+)\"");
                Matcher matcher = pattern.matcher(htmlContent);

                while (matcher.find()) {
                    workshopIds.add(matcher.group(1));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch collection URL: " + collectionUrl);
        }
        return workshopIds;
    }

    /**
     * Asynchronous method to get mod details for a list of workshopIds.
     * Now uses Steam API batch request for improved performance.
     */
    @Async
    public CompletableFuture<List<ModDetails>> getModDetails(List<String> workshopIds) {
        return CompletableFuture.supplyAsync(() -> {
            List<ModDetails> modDetailsList = new ArrayList<>();

            // Use batch API call for all mods at once
            List<SteamPublishedFileDetails> apiDetails = steamApiService.fetchBatchModDetails(workshopIds);

            // Map API response to ModDetails, maintaining original order
            for (int i = 0; i < workshopIds.size(); i++) {
                String workshopId = workshopIds.get(i);

                // Find matching API response
                SteamPublishedFileDetails details = null;
                if (i < apiDetails.size()) {
                    details = apiDetails.get(i);
                }

                // Check if this workshopId matches (API may return in different order)
                if (details != null && workshopId.equals(details.getPublishedfileid())) {
                    modDetailsList.add(parseSteamApiDetails(details));
                } else {
                    // Search for matching workshopId in the list
                    boolean found = false;
                    for (SteamPublishedFileDetails d : apiDetails) {
                        if (workshopId.equals(d.getPublishedfileid())) {
                            modDetailsList.add(parseSteamApiDetails(d));
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        // Not found in API response - return null ModDetails
                        modDetailsList.add(new ModDetails(null, workshopId, null, null));
                    }
                }
            }

            return modDetailsList;
        });
    }

    /**
     * Parse Steam API response into ModDetails model.
     * Handles result codes and extracts data from API response.
     */
    private ModDetails parseSteamApiDetails(SteamPublishedFileDetails details) {
        String workshopId = details.getPublishedfileid();

        // Check for unsuccessful result (1 = success, 9 = not found, others = errors)
        if (details.getResult() != 1) {
            return new ModDetails(null, workshopId, null, null);
        }

        // Extract thumbnail from preview_url
        String thumbnailUrl = details.getPreview_url();
        if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
            thumbnailUrl = "/placeholder.svg";
        }

        // Extract Mod ID and maps from description
        String description = details.getDescription();
        String modName = extractModId(description);
        Set<String> maps = new HashSet<>();
        if (description != null) {
            extractMapsFromDescription(description, maps);
        }

        // If no Mod ID found, we can't use this mod
        if (modName == null || modName.isEmpty()) {
            return new ModDetails(null, workshopId, null, null);
        }

        return new ModDetails(modName, workshopId, List.copyOf(maps), thumbnailUrl);
    }

    /**
     * Extract Mod ID from description using multiple regex patterns.
     * Patterns from ZomboidServerSetup reference implementation.
     */
    private String extractModId(String description) {
        if (description == null) {
            return null;
        }

        // List of regex patterns to try, in order
        List<String> patterns = Arrays.asList(
            // Standard "Mod ID: xyz" formats
            "(?i)Mod[:\\s]+ID[:\\s]*([a-zA-Z0-9_.\\-\\s]+?)(?:\\n|$)",
            "(?i)mod[:\\s]+id[:\\s]*([a-zA-Z0-9_.\\-\\s]+?)(?:\\n|$)",
            "(?i)ModId[:\\s]*([a-zA-Z0-9_.\\-\\s]+?)(?:\\n|$)",
            "(?i)modid[:\\s]*([a-zA-Z0-9_.\\-\\s]+?)(?:\\n|$)"
        );

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(description);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        }

        return null;
    }

    /**
     * Extract map folders from description.
     * Map patterns remain the same - parse from description text.
     */
    private void extractMapsFromDescription(String description, Set<String> maps) {
        // Existing map patterns
        Pattern mapPattern = Pattern.compile("(?i)(Map ?Folder|Folder|Map): ([\\w. ]+)");
        Matcher matcher = mapPattern.matcher(description);
        while (matcher.find()) {
            maps.add(matcher.group(2).trim());
        }
    }
}
