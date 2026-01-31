package com.faithapp.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.faithapp.models.Surah;
import com.faithapp.services.QuranService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Slider;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;

public class QuranTextViewerController {
    private static final Logger logger = LoggerFactory.getLogger(QuranTextViewerController.class);
    
    @FXML private ComboBox<String> reciterComboBox;
    @FXML private Label currentSurahLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label totalTimeLabel;
    @FXML private Label surahTitleLabel;
    @FXML private Slider timeSlider;
    @FXML private Slider volumeSlider;
    @FXML private Button playPauseButton;
    @FXML private Button previousButton;
    @FXML private Button nextButton;
    @FXML private Button repeatButton;
    @FXML private ListView<String> surahListView;
    @FXML private TextFlow quranTextFlow;
    @FXML private MediaView mediaView;
    @FXML private ProgressIndicator loadingIndicator;
    
    private Stage stage;
    private MediaPlayer mediaPlayer;
    private Map<String, String> reciterUrls;
    private List<String> surahs;
    private int currentSurahIndex = 0;
    private boolean isRepeatEnabled = false;
    private Timer progressTimer;
    private Preferences prefs;
    private QuranService quranService;
    
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setMaximized(true);
        stage.setOnCloseRequest(event -> cleanup());
    }
    
    public void initialize() {
        prefs = Preferences.userNodeForPackage(QuranTextViewerController.class);
        quranService = new QuranService();
        initializeReciters();
        initializeSurahs();
        setupControls();
        loadLastSession();
        logger.info("QuranTextViewer initialized");
    }
    
    private void initializeReciters() {
        reciterUrls = new HashMap<>();
        // Add popular Quran reciters and their audio URLs
        reciterUrls.put("Mishary Rashid Alafasy", "https://download.quranicaudio.com/quran/mishaari_raashid_al_3afaasee/");
        reciterUrls.put("Abdul Rahman Al-Sudais", "https://download.quranicaudio.com/quran/abdurrahmaan_as-sudays/");
        reciterUrls.put("Saud Al-Shuraim", "https://download.quranicaudio.com/quran/sa3ood_al-shuraym/");
        
        reciterComboBox.setItems(FXCollections.observableArrayList(reciterUrls.keySet()));
        reciterComboBox.setValue(prefs.get("lastReciter", "Mishary Rashid Alafasy"));
        
        reciterComboBox.setOnAction(e -> {
            prefs.put("lastReciter", reciterComboBox.getValue());
            loadSurah(currentSurahIndex);
        });
    }
    
    private void initializeSurahs() {
        List<Surah> surahList = quranService.getSurahs();
        surahs = new ArrayList<>();
        for (Surah surah : surahList) {
            surahs.add(String.format("%d. %s (%s)", 
                surah.getNumber(), 
                surah.getEnglishName(), 
                surah.getName()));
        }
        
        surahListView.setItems(FXCollections.observableArrayList(surahs));
        surahListView.getSelectionModel().selectedIndexProperty().addListener((obs, old, newValue) -> {
            if (newValue != null) {
                currentSurahIndex = newValue.intValue();
                loadSurah(currentSurahIndex);
                prefs.putInt("lastSurah", currentSurahIndex);
            }
        });
    }
    
    private void setupControls() {
        timeSlider.valueProperty().addListener((obs, old, newValue) -> {
            if (mediaPlayer != null && timeSlider.isValueChanging()) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
        });
        
        volumeSlider.valueProperty().addListener((obs, old, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100.0);
                prefs.putDouble("lastVolume", newValue.doubleValue());
            }
        });
        
        volumeSlider.setValue(prefs.getDouble("lastVolume", 100.0));
    }
    
    private void loadLastSession() {
        int lastSurah = prefs.getInt("lastSurah", 0);
        surahListView.getSelectionModel().select(lastSurah);
    }
    
    private void loadSurah(int index) {
        if (index < 0 || index >= surahs.size()) return;
        
        String surahName = surahs.get(index);
        currentSurahLabel.setText(surahName);
        surahTitleLabel.setText(surahName);
        
        // Load Quran text
        loadQuranText(index + 1); // Surah numbers are 1-based
        
        // Load audio
        String reciter = reciterComboBox.getValue();
        String baseUrl = reciterUrls.get(reciter);
        String audioUrl = baseUrl + String.format("%03d.mp3", index + 1);
        
        try {
            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }
            
            Media media = new Media(audioUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            
            setupMediaPlayer();
            
            logger.info("Loaded surah {}: {}", index + 1, surahName);
        } catch (Exception e) {
            logger.error("Error loading audio for surah {}: {}", index + 1, e.getMessage());
            showError("Error", "Failed to load audio. Please check your internet connection.");
        }
    }
    
    private void setupMediaPlayer() {
        mediaPlayer.setVolume(volumeSlider.getValue() / 100.0);
        
        mediaPlayer.currentTimeProperty().addListener((obs, old, newValue) -> {
            if (!timeSlider.isValueChanging()) {
                timeSlider.setValue(newValue.toSeconds());
                updateTimeLabels(newValue, mediaPlayer.getTotalDuration());
            }
        });
        
        mediaPlayer.setOnReady(() -> {
            timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
            updateTimeLabels(Duration.ZERO, mediaPlayer.getTotalDuration());
        });
        
        mediaPlayer.setOnEndOfMedia(() -> {
            if (isRepeatEnabled) {
                mediaPlayer.seek(Duration.ZERO);
                mediaPlayer.play();
            } else {
                handleNext();
            }
        });
    }
    
    private void updateTimeLabels(Duration current, Duration total) {
        currentTimeLabel.setText(formatTime(current));
        totalTimeLabel.setText(formatTime(total));
    }
    
    private String formatTime(Duration duration) {
        int minutes = (int) Math.floor(duration.toMinutes());
        int seconds = (int) Math.floor(duration.toSeconds()) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    private void loadQuranText(int surahNumber) {
        loadingIndicator.setVisible(true);
        quranTextFlow.getChildren().clear();
        
        Surah surah = quranService.getSurah(surahNumber);
        if (surah == null) {
            loadingIndicator.setVisible(false);
            showError("Error", "Failed to load surah information");
            return;
        }
        
        // Add surah header
        Text headerText = new Text(surah.getName() + "\n" +
                                 "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ\n\n");
        headerText.getStyleClass().add("arabic-text");
        Platform.runLater(() -> quranTextFlow.getChildren().add(headerText));
        
        // Load ayahs sequentially to ensure none are skipped
        AtomicInteger currentAyah = new AtomicInteger(1);
        loadNextAyah(surah, currentAyah);
    }
    
    private void loadNextAyah(Surah surah, AtomicInteger currentAyah) {
        int ayahNumber = currentAyah.get();
        if (ayahNumber > surah.getNumberOfAyahs()) {
            Platform.runLater(() -> loadingIndicator.setVisible(false));
            return;
        }
        
        CompletableFuture<String> arabicFuture = quranService.getAyahText(surah.getNumber(), ayahNumber);
        CompletableFuture<String> translationFuture = quranService.getAyahTranslation(surah.getNumber(), ayahNumber);
        
        CompletableFuture.allOf(arabicFuture, translationFuture)
            .thenAccept(v -> {
                String arabicText = arabicFuture.join();
                String translation = translationFuture.join();
                
                Platform.runLater(() -> {
                    if (!arabicText.isEmpty()) {
                        Text arabicVerse = new Text(arabicText + " ﴿" + ayahNumber + "﴾\n\n");
                        arabicVerse.getStyleClass().add("arabic-text");
                        quranTextFlow.getChildren().add(arabicVerse);
                    }
                    
                    if (!translation.isEmpty()) {
                        Text translationVerse = new Text(translation + "\n\n");
                        translationVerse.getStyleClass().add("translation-text");
                        quranTextFlow.getChildren().add(translationVerse);
                    }
                    
                    // Load next ayah
                    currentAyah.incrementAndGet();
                    loadNextAyah(surah, currentAyah);
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading ayah {} for surah {}: {}", 
                    ayahNumber, surah.getNumber(), e.getMessage());
                
                // Retry this ayah after a short delay
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                
                Platform.runLater(() -> loadNextAyah(surah, currentAyah));
                return null;
            });
    }
    
    @FXML
    private void handlePlayPause() {
        if (mediaPlayer == null) return;
        
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("▶");
        } else {
            mediaPlayer.play();
            playPauseButton.setText("⏸");
        }
    }
    
    @FXML
    private void handlePrevious() {
        if (currentSurahIndex > 0) {
            surahListView.getSelectionModel().select(currentSurahIndex - 1);
        }
    }
    
    @FXML
    private void handleNext() {
        if (currentSurahIndex < surahs.size() - 1) {
            surahListView.getSelectionModel().select(currentSurahIndex + 1);
        }
    }
    
    @FXML
    private void handleRepeat() {
        isRepeatEnabled = !isRepeatEnabled;
        repeatButton.setStyle(isRepeatEnabled ? 
            "-fx-background-color: transparent; -fx-text-fill: #00BFA5; -fx-font-size: 20; -fx-opacity: 1.0;" :
            "-fx-background-color: transparent; -fx-text-fill: #00BFA5; -fx-font-size: 20; -fx-opacity: 0.5;");
    }
    
    private void showError(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.show();
        });
    }
    
    public void cleanup() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
        if (progressTimer != null) {
            progressTimer.cancel();
        }
        logger.info("QuranTextViewer cleaned up");
    }
    
    @FXML
    private void handleBackToDashboard() {
        try {
            cleanup(); // Clean up media player before closing
            
            javafx.stage.Stage currentStage = (javafx.stage.Stage) currentSurahLabel.getScene().getWindow();
            currentStage.close();
            
            // Return to the original dashboard window
            javafx.stage.Stage mainDashboard = com.faithapp.controllers.DashboardController.getMainDashboardStage();
            if (mainDashboard != null) {
                mainDashboard.show();
                mainDashboard.toFront();
                logger.info("Returned to original dashboard from Quran Text Viewer");
            } else {
                logger.warn("Main dashboard stage not found, creating new dashboard");
                // Fallback: create new dashboard if main reference is lost
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                javafx.scene.Parent dashboard = loader.load();
                
                javafx.stage.Stage dashboardStage = new javafx.stage.Stage();
                dashboardStage.setTitle("ImanConnect - Dashboard");
                dashboardStage.setScene(new javafx.scene.Scene(dashboard));
                dashboardStage.setMaximized(true);
                dashboardStage.show();
            }
        } catch (Exception e) {
            logger.error("Error returning to dashboard: {}", e.getMessage(), e);
            showError("Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    public void loadJuz(int juzNumber, String startVerse, String endVerse) {
        // Set header to Juz X
        Platform.runLater(() -> {
            currentSurahLabel.setText("Juz " + juzNumber);
            surahTitleLabel.setText("Juz " + juzNumber);
        });
        loadingIndicator.setVisible(true);
        quranTextFlow.getChildren().clear();

        // Parse start and end verse (e.g., "Al-Baqarah 1")
        String[] startParts = startVerse.split(" ");
        String[] endParts = endVerse.split(" ");
        if (startParts.length < 2 || endParts.length < 2) {
            showError("Error", "Invalid Juz verse range");
            loadingIndicator.setVisible(false);
            return;
        }
        String startSurahName = startParts[0];
        int startAyah = Integer.parseInt(startParts[1]);
        String endSurahName = endParts[0];
        int endAyah = Integer.parseInt(endParts[1]);

        // Find surah numbers for start and end
        List<Surah> surahList = quranService.getSurahs();
        int startSurahNum = -1, endSurahNum = -1;
        for (Surah s : surahList) {
            if (s.getEnglishName().replace("'", "").replace("-", "").replace(" ", "").equalsIgnoreCase(startSurahName.replace("'", "").replace("-", "").replace(" ", ""))) {
                startSurahNum = s.getNumber();
            }
            if (s.getEnglishName().replace("'", "").replace("-", "").replace(" ", "").equalsIgnoreCase(endSurahName.replace("'", "").replace("-", "").replace(" ", ""))) {
                endSurahNum = s.getNumber();
            }
        }
        if (startSurahNum == -1 || endSurahNum == -1) {
            showError("Error", "Could not find surah for Juz range");
            loadingIndicator.setVisible(false);
            return;
        }

        // Collect all (surah, ayah) pairs in the Juz
        List<int[]> ayahPairs = new ArrayList<>();
        if (startSurahNum == endSurahNum) {
            for (int ayah = startAyah; ayah <= endAyah; ayah++) {
                ayahPairs.add(new int[]{startSurahNum, ayah});
            }
        } else {
            // Add from start ayah to end of start surah
            Surah startSurah = quranService.getSurah(startSurahNum);
            for (int ayah = startAyah; ayah <= startSurah.getNumberOfAyahs(); ayah++) {
                ayahPairs.add(new int[]{startSurahNum, ayah});
            }
            // Add all ayahs of intermediate surahs
            for (int s = startSurahNum + 1; s < endSurahNum; s++) {
                Surah surah = quranService.getSurah(s);
                for (int ayah = 1; ayah <= surah.getNumberOfAyahs(); ayah++) {
                    ayahPairs.add(new int[]{s, ayah});
                }
            }
            // Add from 1 to endAyah of endSurah
            for (int ayah = 1; ayah <= endAyah; ayah++) {
                ayahPairs.add(new int[]{endSurahNum, ayah});
            }
        }

        // Sequentially load and display all ayahs
        loadJuzAyahsSequentially(ayahPairs, 0);
    }

    private void loadJuzAyahsSequentially(List<int[]> ayahPairs, int index) {
        if (index >= ayahPairs.size()) {
            Platform.runLater(() -> loadingIndicator.setVisible(false));
            return;
        }
        int surahNum = ayahPairs.get(index)[0];
        int ayahNum = ayahPairs.get(index)[1];
        CompletableFuture<String> arabicFuture = quranService.getAyahText(surahNum, ayahNum);
        CompletableFuture<String> translationFuture = quranService.getAyahTranslation(surahNum, ayahNum);
        CompletableFuture.allOf(arabicFuture, translationFuture)
            .thenAccept(v -> {
                String arabicText = arabicFuture.join();
                String translation = translationFuture.join();
                Platform.runLater(() -> {
                    if (!arabicText.isEmpty()) {
                        Text arabicVerse = new Text(arabicText + " ﴿" + ayahNum + "﴾\n\n");
                        arabicVerse.getStyleClass().add("arabic-text");
                        quranTextFlow.getChildren().add(arabicVerse);
                    }
                    if (!translation.isEmpty()) {
                        Text translationVerse = new Text(translation + "\n\n");
                        translationVerse.getStyleClass().add("translation-text");
                        quranTextFlow.getChildren().add(translationVerse);
                    }
                    loadJuzAyahsSequentially(ayahPairs, index + 1);
                });
            })
            .exceptionally(e -> {
                logger.error("Error loading ayah {}:{}: {}", surahNum, ayahNum, e.getMessage());
                Platform.runLater(() -> loadJuzAyahsSequentially(ayahPairs, index + 1));
                return null;
            });
    }

    public void selectSurah(int surahIndex) {
        if (surahIndex >= 0 && surahIndex < surahs.size()) {
            surahListView.getSelectionModel().select(surahIndex);
        }
    }
} 