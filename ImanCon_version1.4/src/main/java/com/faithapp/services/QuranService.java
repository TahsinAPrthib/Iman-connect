package com.faithapp.services;

import com.faithapp.models.Surah;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.json.JSONArray;
import org.json.JSONObject;

public class QuranService {
    private static final Logger logger = LoggerFactory.getLogger(QuranService.class);
    private static final String API_BASE_URL = "http://api.alquran.cloud/v1";
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private List<Surah> surahs;
    
    public QuranService() {
        surahs = new ArrayList<>();
        loadSurahs();
    }
    
    private void loadSurahs() {
        try {
            logger.info("Loading surahs from API...");
            String response = makeApiRequestWithRetry("/surah");
            if (response == null || response.isEmpty()) {
                logger.error("Empty response from API when loading surahs");
                return;
            }

            logger.debug("Raw surah response: {}", response);
            JSONObject jsonResponse = new JSONObject(response);
            if (!jsonResponse.has("data")) {
                logger.error("API response missing 'data' field: {}", response);
                return;
            }

            JSONArray surahsArray = jsonResponse.getJSONArray("data");
            logger.info("Received {} surahs from API", surahsArray.length());
            
            for (int i = 0; i < surahsArray.length(); i++) {
                JSONObject surahData = surahsArray.getJSONObject(i);
                try {
                    Surah surah = new Surah(
                        surahData.getInt("number"),
                        surahData.getString("name"),
                        surahData.getString("englishName"),
                        surahData.getString("englishNameTranslation"),
                        surahData.getInt("numberOfAyahs"),
                        surahData.getString("revelationType")
                    );
                    surahs.add(surah);
                    logger.debug("Added surah: {}", surah.getEnglishName());
                } catch (Exception e) {
                    logger.error("Error parsing surah data: {}", surahData.toString(), e);
                }
            }
            logger.info("Successfully loaded {} surahs", surahs.size());
        } catch (Exception e) {
            logger.error("Error loading surahs: {}", e.getMessage(), e);
        }
    }
    
    public CompletableFuture<String> getAyahText(int surahNumber, int ayahNumber) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching ayah {}/{} text...", surahNumber, ayahNumber);
                String response = makeApiRequestWithRetry(String.format("/ayah/%d:%d/ar", surahNumber, ayahNumber));
                if (response == null || response.isEmpty()) {
                    logger.error("Empty response when fetching ayah {}/{}", surahNumber, ayahNumber);
                    return "";
                }

                logger.debug("Raw ayah response: {}", response);
                JSONObject jsonResponse = new JSONObject(response);
                if (!jsonResponse.has("data")) {
                    logger.error("API response missing 'data' field for ayah {}/{}: {}", 
                        surahNumber, ayahNumber, response);
                    return "";
                }

                JSONObject data = jsonResponse.getJSONObject("data");
                String text = data.getString("text");
                logger.debug("Successfully fetched ayah {}/{}: {}", surahNumber, ayahNumber, text);
                return text;
            } catch (Exception e) {
                logger.error("Error fetching ayah {}/{} text: {}", 
                    surahNumber, ayahNumber, e.getMessage(), e);
                return "";
            }
        });
    }
    
    public CompletableFuture<String> getAyahTranslation(int surahNumber, int ayahNumber) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.debug("Fetching ayah {}/{} translation...", surahNumber, ayahNumber);
                String response = makeApiRequestWithRetry(String.format("/ayah/%d:%d/en.sahih", surahNumber, ayahNumber));
                if (response == null || response.isEmpty()) {
                    logger.error("Empty response when fetching translation {}/{}", surahNumber, ayahNumber);
                    return "";
                }

                logger.debug("Raw translation response: {}", response);
                JSONObject jsonResponse = new JSONObject(response);
                if (!jsonResponse.has("data")) {
                    logger.error("API response missing 'data' field for translation {}/{}: {}", 
                        surahNumber, ayahNumber, response);
                    return "";
                }

                JSONObject data = jsonResponse.getJSONObject("data");
                String text = data.getString("text");
                logger.debug("Successfully fetched translation {}/{}: {}", surahNumber, ayahNumber, text);
                return text;
            } catch (Exception e) {
                logger.error("Error fetching ayah {}/{} translation: {}", 
                    surahNumber, ayahNumber, e.getMessage(), e);
                return "";
            }
        });
    }
    
    public List<Surah> getSurahs() {
        return new ArrayList<>(surahs);
    }
    
    public Surah getSurah(int number) {
        return surahs.stream()
                    .filter(s -> s.getNumber() == number)
                    .findFirst()
                    .orElse(null);
    }
    
    private String makeApiRequestWithRetry(String endpoint) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (attempt > 1) {
                    Thread.sleep(RETRY_DELAY_MS * (attempt - 1));
                }
                return makeApiRequest(endpoint);
            } catch (Exception e) {
                lastException = e;
                logger.warn("API request attempt {} failed: {}", attempt, e.getMessage());
            }
        }
        throw lastException;
    }
    
    private String makeApiRequest(String endpoint) throws Exception {
        URL url = new URL(API_BASE_URL + endpoint);
        logger.debug("Making API request to: {}", url);
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");
        
        int responseCode = conn.getResponseCode();
        logger.debug("Response code: {}", responseCode);
        
        if (responseCode == 429) {
            throw new Exception("Rate limit exceeded");
        }
        
        if (responseCode != HttpURLConnection.HTTP_OK) {
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                String error = errorResponse.toString();
                logger.error("API request failed with response code: {} and error: {}", 
                    responseCode, error);
                throw new Exception("API request failed: " + error);
            }
        }
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            
            String result = response.toString();
            logger.debug("API response received: {}", result);
            return result;
        }
    }
} 