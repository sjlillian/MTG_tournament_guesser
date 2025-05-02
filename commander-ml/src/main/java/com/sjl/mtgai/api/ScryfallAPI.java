package com.sjl.mtgai.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Client for interacting with the Scryfall API.
 * Implements rate limiting and error handling according to Scryfall's guidelines.
 * 
 * This class provides methods to fetch card data, search for cards, and retrieve
 * card images from Scryfall.
 */
public class ScryfallAPI {
    // Scryfall API base URL
    private static final String API_BASE_URL = "https://api.scryfall.com";
    
    // Scryfall API endpoints
    private static final String CARDS_NAMED_ENDPOINT = "/cards/named";
    private static final String CARDS_SEARCH_ENDPOINT = "/cards/search";
    private static final String CARDS_BY_ID_ENDPOINT = "/cards/";
    
    // Rate limiting constants (Scryfall recommends 50-100ms between requests)
    private static final long RATE_LIMIT_MS = 100;
    private long lastRequestTime = 0;
    
    // HTTP client for making API calls
    private final HttpClient httpClient;
    
    /**
     * Creates a new ScryfallAPI instance with default HTTP client settings.
     */
    public ScryfallAPI() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    /**
     * Fetches a card by its exact name.
     * 
     * @param cardName The exact card name to fetch
     * @return JSONObject containing the card data
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws ScryfallAPIException If an API error occurs
     */
    public JSONObject getCardByExactName(String cardName) throws IOException, InterruptedException, ScryfallAPIException {
        Map<String, String> params = new HashMap<>();
        params.put("exact", cardName);
        
        return makeRequest(CARDS_NAMED_ENDPOINT, params);
    }
    
    /**
     * Fetches a card by a fuzzy name match.
     * 
     * @param cardName The card name to fuzzy match
     * @return JSONObject containing the card data
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws ScryfallAPIException If an API error occurs
     */
    public JSONObject getCardByFuzzyName(String cardName) throws IOException, InterruptedException, ScryfallAPIException {
        Map<String, String> params = new HashMap<>();
        params.put("fuzzy", cardName);
        
        return makeRequest(CARDS_NAMED_ENDPOINT, params);
    }
    
    /**
     * Fetches a card by its Scryfall ID.
     * 
     * @param id The Scryfall ID of the card
     * @return JSONObject containing the card data
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws ScryfallAPIException If an API error occurs
     */
    public JSONObject getCardById(String id) throws IOException, InterruptedException, ScryfallAPIException {
        return makeRequest(CARDS_BY_ID_ENDPOINT + id, null);
    }
    
    /**
     * Searches for cards using the Scryfall search syntax.
     * 
     * @param query The Scryfall search query
     * @return JSONObject containing the search results
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws ScryfallAPIException If an API error occurs
     */
    public JSONObject searchCards(String query) throws IOException, InterruptedException, ScryfallAPIException {
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        
        return makeRequest(CARDS_SEARCH_ENDPOINT, params);
    }
    
    /**
     * Searches for cards using the Scryfall search syntax with pagination.
     * 
     * @param query The Scryfall search query
     * @param page The page number to fetch (starting from 1)
     * @return JSONObject containing the search results
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws ScryfallAPIException If an API error occurs
     */
    public JSONObject searchCards(String query, int page) throws IOException, InterruptedException, ScryfallAPIException {
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("page", String.valueOf(page));
        
        return makeRequest(CARDS_SEARCH_ENDPOINT, params);
    }
    
    /**
     * Gets all cards that match a search query by automatically paginating through all results.
     * This method may make multiple API calls.
     * 
     * @param query The Scryfall search query
     * @return JSONArray containing all cards matching the search
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws ScryfallAPIException If an API error occurs
     */
    public JSONArray getAllCards(String query) throws IOException, InterruptedException, ScryfallAPIException {
        JSONArray allCards = new JSONArray();
        JSONObject response = searchCards(query);
        
        // Add first page of results to the array
        JSONArray data = response.getJSONArray("data");
        for (int i = 0; i < data.length(); i++) {
            allCards.put(data.getJSONObject(i));
        }
        
        // Continue fetching pages until we've got all results
        while (response.has("has_more") && response.getBoolean("has_more")) {
            String nextPage = response.getString("next_page");
            response = makeRequestToUrl(nextPage);
            
            data = response.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                allCards.put(data.getJSONObject(i));
            }
        }
        
        return allCards;
    }
    
    /**
     * Gets the image URLs for a card.
     * 
     * @param cardData The JSONObject containing card data (from any get/search method)
     * @return Map containing different image versions and their URLs
     * @throws ScryfallAPIException If the card does not have images
     */
    public Map<String, String> getCardImageUrls(JSONObject cardData) throws ScryfallAPIException {
        Map<String, String> imageUrls = new HashMap<>();
        
        try {
            if (cardData.has("image_uris")) {
                JSONObject imageUris = cardData.getJSONObject("image_uris");
                imageUrls.put("small", imageUris.getString("small"));
                imageUrls.put("normal", imageUris.getString("normal"));
                imageUrls.put("large", imageUris.getString("large"));
                imageUrls.put("png", imageUris.getString("png"));
                imageUrls.put("art_crop", imageUris.getString("art_crop"));
                imageUrls.put("border_crop", imageUris.getString("border_crop"));
            } else if (cardData.has("card_faces")) {
                // Handle double-faced cards
                JSONArray faces = cardData.getJSONArray("card_faces");
                for (int i = 0; i < faces.length(); i++) {
                    JSONObject face = faces.getJSONObject(i);
                    if (face.has("image_uris")) {
                        JSONObject imageUris = face.getJSONObject("image_uris");
                        String prefix = "face_" + (i+1) + "_";
                        imageUrls.put(prefix + "small", imageUris.getString("small"));
                        imageUrls.put(prefix + "normal", imageUris.getString("normal"));
                        imageUrls.put(prefix + "large", imageUris.getString("large"));
                        imageUrls.put(prefix + "png", imageUris.getString("png"));
                        imageUrls.put(prefix + "art_crop", imageUris.getString("art_crop"));
                        imageUrls.put(prefix + "border_crop", imageUris.getString("border_crop"));
                    }
                }
            } else {
                throw new ScryfallAPIException("No images available for this card");
            }
        } catch (JSONException e) {
            throw new ScryfallAPIException("Error parsing image data: " + e.getMessage());
        }
        
        return imageUrls;
    }
    
    /**
     * Downloads a card image asynchronously.
     * 
     * @param imageUrl The URL of the image to download
     * @return CompletableFuture that will contain the image bytes when complete
     */
    public CompletableFuture<byte[]> downloadCardImageAsync(String imageUrl) {
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(imageUrl))
                .build();
        
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(HttpResponse::body);
    }
    
    /**
     * Makes a request to the Scryfall API with the given endpoint and parameters.
     * Implements rate limiting between requests.
     * 
     * @param endpoint The API endpoint to call
     * @param params The query parameters to include
     * @return JSONObject containing the API response
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws ScryfallAPIException If an API error occurs
     */
    private JSONObject makeRequest(String endpoint, Map<String, String> params) 
            throws IOException, InterruptedException, ScryfallAPIException {
        // Build the URL with parameters
        StringBuilder urlBuilder = new StringBuilder(API_BASE_URL);
        urlBuilder.append(endpoint);
        
        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (!first) {
                    urlBuilder.append("&");
                }
                urlBuilder.append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                first = false;
            }
        }
        
        return makeRequestToUrl(urlBuilder.toString());
    }
    
    /**
     * Makes a request to the given full URL.
     * Implements rate limiting between requests.
     * 
     * @param url The full URL to call
     * @return JSONObject containing the API response
     * @throws IOException If an I/O error occurs
     * @throws InterruptedException If the operation is interrupted
     * @throws ScryfallAPIException If an API error occurs
     */
    private JSONObject makeRequestToUrl(String url) 
            throws IOException, InterruptedException, ScryfallAPIException {
        // Apply rate limiting
        applyRateLimit();
        
        // Build the request
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .build();
        
        // Send the request
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // Update the last request time
        lastRequestTime = System.currentTimeMillis();
        
        // Check if the request was successful
        if (response.statusCode() != 200) {
            try {
                JSONObject error = new JSONObject(response.body());
                if (error.has("details")) {
                    throw new ScryfallAPIException("API Error: " + error.getString("details"));
                } else {
                    throw new ScryfallAPIException("API Error: HTTP " + response.statusCode());
                }
            } catch (JSONException e) {
                throw new ScryfallAPIException("API Error: HTTP " + response.statusCode());
            }
        }
        
        // Parse the response
        try {
            return new JSONObject(response.body());
        } catch (JSONException e) {
            throw new ScryfallAPIException("Error parsing API response: " + e.getMessage());
        }
    }
    
    /**
     * Applies rate limiting by sleeping if necessary to ensure we don't exceed
     * Scryfall's rate limits.
     * 
     * @throws InterruptedException If the thread is interrupted while sleeping
     */
    private void applyRateLimit() throws InterruptedException {
        long timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime;
        if (timeSinceLastRequest < RATE_LIMIT_MS) {
            long sleepTime = RATE_LIMIT_MS - timeSinceLastRequest;
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        }
    }
    
    /**
     * Exception thrown when there is an error with the Scryfall API.
     */
    public static class ScryfallAPIException extends Exception {
        private static final long serialVersionUID = 1L;
        
        public ScryfallAPIException(String message) {
            super(message);
        }
        
        public ScryfallAPIException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

