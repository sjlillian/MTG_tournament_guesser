package com.sjl.mtgai.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sjl.mtgai.api.ScryfallAPI.ScryfallAPIException;

/**
 * Test class for the ScryfallAPI.
 * This class tests the basic functionality of the ScryfallAPI.
 * 
 * Note: These tests make real API calls to Scryfall, so they may fail if the service is down
 * or if the API contract changes.
 */
public class ScryfallAPITest {
    
    private ScryfallAPI api;
    
    @Before
    public void setUp() {
        api = new ScryfallAPI();
    }
    
    /**
     * Test that we can fetch a card by its exact name.
     */
    @Test
    public void testGetCardByExactName() throws IOException, InterruptedException, ScryfallAPIException {
        // Sol Ring is a common card in many Commander decks
        JSONObject card = api.getCardByExactName("Sol Ring");
        
        assertNotNull("Card should not be null", card);
        assertEquals("Card name should match", "Sol Ring", card.getString("name"));
        assertEquals("Card type should match", "Artifact", card.getString("type_line"));
        
        // Verify some expected fields
        assertTrue("Card should have mana_cost", card.has("mana_cost"));
        assertTrue("Card should have cmc", card.has("cmc"));
        assertTrue("Card should have set", card.has("set"));
        assertTrue("Card should have rarity", card.has("rarity"));
        
        System.out.println("Successfully fetched Sol Ring with data: " + 
                          "CMC=" + card.getDouble("cmc") + 
                          ", Set=" + card.getString("set") + 
                          ", Rarity=" + card.getString("rarity"));
    }
    
    /**
     * Test that we can fetch card images.
     */
    @Test
    public void testGetCardImageUrls() throws IOException, InterruptedException, ScryfallAPIException {
        JSONObject card = api.getCardByExactName("Lightning Bolt");
        
        Map<String, String> imageUrls = api.getCardImageUrls(card);
        
        assertNotNull("Image URLs should not be null", imageUrls);
        assertFalse("Image URLs should not be empty", imageUrls.isEmpty());
        
        // Check that we have the expected image versions
        assertTrue("Should have small image", imageUrls.containsKey("small"));
        assertTrue("Should have normal image", imageUrls.containsKey("normal"));
        assertTrue("Should have large image", imageUrls.containsKey("large"));
        assertTrue("Should have png image", imageUrls.containsKey("png"));
        
        // Verify the image URLs are valid (start with http or https)
        for (String url : imageUrls.values()) {
            assertTrue("URL should start with http(s)", 
                    url.startsWith("http://") || url.startsWith("https://"));
        }
        
        System.out.println("Successfully fetched image URLs for Lightning Bolt");
        System.out.println("Normal image URL: " + imageUrls.get("normal"));
    }
    
    /**
     * Test error handling for non-existent cards.
     */
    @Test
    public void testErrorHandlingForNonExistentCard() {
        String nonExistentCardName = "This Card Definitely Does Not Exist";
        
        try {
            api.getCardByExactName(nonExistentCardName);
            fail("Should have thrown ScryfallAPIException for non-existent card");
        } catch (ScryfallAPIException e) {
            // Expected exception
            assertTrue("Error message should mention the card name", 
                    e.getMessage().contains("API Error"));
            System.out.println("Successfully caught exception for non-existent card: " + e.getMessage());
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }
    
    /**
     * Test handling of double-faced cards.
     */
    @Test
    public void testHandlingDoubleFacedCards() throws IOException, InterruptedException, ScryfallAPIException {
        // "Delver of Secrets" is a classic double-faced card
        JSONObject card = api.getCardByExactName("Delver of Secrets");
        
        assertNotNull("Card should not be null", card);
        assertEquals("Card name should match", "Delver of Secrets // Insectile Aberration", card.getString("name"));
        
        // Verify it's a double-faced card
        assertTrue("Card should have layout field", card.has("layout"));
        assertEquals("Card should have double-faced layout", "transform", card.getString("layout"));
        assertTrue("Card should have card_faces", card.has("card_faces"));
        
        // Check the card faces
        assertEquals("Should have 2 faces", 2, card.getJSONArray("card_faces").length());
        
        // Get image URLs for the double-faced card
        Map<String, String> imageUrls = api.getCardImageUrls(card);
        
        // Should have image URLs for each face
        assertTrue("Should have face 1 normal image", 
                imageUrls.containsKey("face_1_normal"));
        assertTrue("Should have face 2 normal image", 
                imageUrls.containsKey("face_2_normal"));
        
        System.out.println("Successfully fetched double-faced card: Delver of Secrets / Insectile Aberration");
        System.out.println("Face 1 normal image: " + imageUrls.get("face_1_normal"));
        System.out.println("Face 2 normal image: " + imageUrls.get("face_2_normal"));
    }
    
    /**
     * Test downloading a card image.
     */
    @Test
    public void testDownloadCardImage() throws IOException, InterruptedException, ScryfallAPIException, ExecutionException {
        JSONObject card = api.getCardByExactName("Island");
        Map<String, String> imageUrls = api.getCardImageUrls(card);
        
        String smallImageUrl = imageUrls.get("small");
        byte[] imageData = api.downloadCardImageAsync(smallImageUrl).get();
        
        assertNotNull("Image data should not be null", imageData);
        assertTrue("Image data should not be empty", imageData.length > 0);
        
        System.out.println("Successfully downloaded image for Island, size: " + imageData.length + " bytes");
    }
}

