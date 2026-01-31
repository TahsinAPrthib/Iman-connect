package com.faithapp.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ProfilePictureManager {
    private static final Logger logger = LoggerFactory.getLogger(ProfilePictureManager.class);
    
    // Store pictures in user's home directory
    private static final String APP_DATA_DIR = System.getProperty("user.home") + File.separator + "ImanConnect";
    private static final String PROFILE_PICTURES_DIR = APP_DATA_DIR + File.separator + "profile_pictures";
    private static final String DEFAULT_PROFILE_PICTURE = APP_DATA_DIR + File.separator + "default-profile.png";

    static {
        try {
            createDirectories();
            createDefaultProfilePicture();
            logger.info("ProfilePictureManager initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing ProfilePictureManager", e);
        }
    }

    private static void createDirectories() {
        try {
            // Create application data directory
            Path appDataPath = Paths.get(APP_DATA_DIR);
            if (!Files.exists(appDataPath)) {
                Files.createDirectories(appDataPath);
                logger.info("Created application data directory at: {}", appDataPath);
            }

            // Create profile pictures directory
            Path profilePicsPath = Paths.get(PROFILE_PICTURES_DIR);
            if (!Files.exists(profilePicsPath)) {
                Files.createDirectories(profilePicsPath);
                logger.info("Created profile pictures directory at: {}", profilePicsPath);
            }
        } catch (IOException e) {
            logger.error("Error creating directories", e);
            throw new RuntimeException("Failed to create necessary directories", e);
        }
    }

    private static void createDefaultProfilePicture() {
        File defaultPicture = new File(DEFAULT_PROFILE_PICTURE);
        if (!defaultPicture.exists()) {
            try {
                logger.info("Creating default profile picture at: {}", defaultPicture.getAbsolutePath());
                
                // Create a simple default profile picture
                BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
                java.awt.Graphics2D g2d = img.createGraphics();
                
                // Fill background with a nice color
                g2d.setColor(new java.awt.Color(0, 191, 165)); // #00BFA5
                g2d.fillOval(0, 0, 200, 200);
                
                // Add a simple user icon or initial
                g2d.setColor(java.awt.Color.WHITE);
                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 100));
                g2d.drawString("?", 75, 135);
                
                g2d.dispose();
                
                // Save the image
                ImageIO.write(img, "png", defaultPicture);
                logger.info("Default profile picture created successfully");
            } catch (IOException e) {
                logger.error("Error creating default profile picture", e);
                throw new RuntimeException("Failed to create default profile picture", e);
            }
        } else {
            logger.info("Default profile picture already exists at: {}", defaultPicture.getAbsolutePath());
        }
    }

    public static String saveProfilePicture(File sourceFile, String username) {
        try {
            logger.info("Saving profile picture for user: {}", username);
            logger.info("Source file: {}", sourceFile.getAbsolutePath());
            
            String fileExtension = getFileExtension(sourceFile.getName());
            String targetFileName = username + "_" + System.currentTimeMillis() + fileExtension;
            Path targetPath = Paths.get(PROFILE_PICTURES_DIR, targetFileName).toAbsolutePath();
            
            logger.info("Target path: {}", targetPath);
            
            // Create parent directories if they don't exist
            Files.createDirectories(targetPath.getParent());
            
            // Copy the file
            Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Profile picture saved successfully at: {}", targetPath);
            
            return targetPath.toString();
        } catch (IOException e) {
            logger.error("Error saving profile picture for user: " + username, e);
            logger.info("Using default profile picture as fallback");
            return DEFAULT_PROFILE_PICTURE;
        }
    }

    public static Image loadProfilePicture(String picturePath) {
        try {
            if (picturePath == null) {
                logger.info("No profile picture path provided, using default");
                return loadDefaultProfilePicture();
            }

            logger.info("Loading profile picture from: {}", picturePath);
            File file = new File(picturePath);
            
            if (!file.exists()) {
                logger.warn("Profile picture not found at: {}. Using default.", picturePath);
                return loadDefaultProfilePicture();
            }
            
            logger.info("Profile picture found, loading image");
            return new Image(file.toURI().toString());
            
        } catch (Exception e) {
            logger.error("Error loading profile picture: " + picturePath, e);
            return loadDefaultProfilePicture();
        }
    }

    private static Image loadDefaultProfilePicture() {
        try {
            File defaultFile = new File(DEFAULT_PROFILE_PICTURE);
            if (!defaultFile.exists()) {
                logger.error("Default profile picture not found at: {}", defaultFile.getAbsolutePath());
                createDefaultProfilePicture();
            }
            logger.info("Loading default profile picture from: {}", defaultFile.getAbsolutePath());
            return new Image(defaultFile.toURI().toString());
        } catch (Exception e) {
            logger.error("Failed to load default profile picture", e);
            // Create an emergency fallback image in memory
            WritableImage img = new WritableImage(200, 200);
            PixelWriter pw = img.getPixelWriter();
            for (int x = 0; x < 200; x++) {
                for (int y = 0; y < 200; y++) {
                    pw.setColor(x, y, Color.LIGHTGRAY);
                }
            }
            return img;
        }
    }

    private static String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex) : ".png";
    }

    public static void deleteOldProfilePicture(String picturePath) {
        if (picturePath != null && !picturePath.equals(DEFAULT_PROFILE_PICTURE)) {
            try {
                logger.info("Attempting to delete old profile picture: {}", picturePath);
                Files.deleteIfExists(Paths.get(picturePath));
                logger.info("Old profile picture deleted successfully");
            } catch (IOException e) {
                logger.error("Error deleting old profile picture: " + picturePath, e);
            }
        }
    }
} 