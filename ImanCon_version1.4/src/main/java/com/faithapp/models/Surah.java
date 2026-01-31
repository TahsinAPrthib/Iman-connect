package com.faithapp.models;

public class Surah {
    private final int number;
    private final String name;
    private final String englishName;
    private final String englishTranslation;
    private final int numberOfAyahs;
    private final String revelationType;
    
    public Surah(int number, String name, String englishName, String englishTranslation, 
                 int numberOfAyahs, String revelationType) {
        this.number = number;
        this.name = name;
        this.englishName = englishName;
        this.englishTranslation = englishTranslation;
        this.numberOfAyahs = numberOfAyahs;
        this.revelationType = revelationType;
    }
    
    public int getNumber() { return number; }
    public String getName() { return name; }
    public String getEnglishName() { return englishName; }
    public String getEnglishTranslation() { return englishTranslation; }
    public int getNumberOfAyahs() { return numberOfAyahs; }
    public String getRevelationType() { return revelationType; }
    
    @Override
    public String toString() {
        return String.format("%d. %s (%s) - %d verses", number, name, englishName, numberOfAyahs);
    }
} 