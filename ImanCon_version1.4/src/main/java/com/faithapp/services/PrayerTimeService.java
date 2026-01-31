package com.faithapp.services;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PrayerTimeService {
    private static final Map<String, Map<String, PrayerTimes>> BANGLADESH_PRAYER_TIMES = new HashMap<>();
    
    static {
        // Initialize prayer times for major cities
        // Dhaka Division
        initializePrayerTimes("Dhaka", "Dhaka", 
            new PrayerTimes("4:15 AM", "12:10 PM", "4:45 PM", "6:30 PM", "7:45 PM", "4:00 AM", "6:30 PM"));
            
        // Chittagong Division
        initializePrayerTimes("Chittagong", "Chittagong", 
            new PrayerTimes("4:20 AM", "12:15 PM", "4:50 PM", "6:35 PM", "7:50 PM", "4:05 AM", "6:35 PM"));
            
        // Add more cities with their specific times...
    }
    
    private static void initializePrayerTimes(String division, String city, PrayerTimes times) {
        BANGLADESH_PRAYER_TIMES.computeIfAbsent(division, k -> new HashMap<>())
            .put(city, times);
    }
    
    public static PrayerTimes getPrayerTimes(String division, String city) {
        Map<String, PrayerTimes> divisionTimes = BANGLADESH_PRAYER_TIMES.get(division);
        if (divisionTimes == null) {
            return getDefaultTimes();
        }
        
        PrayerTimes cityTimes = divisionTimes.get(city);
        return cityTimes != null ? cityTimes : getDefaultTimes();
    }
    
    private static PrayerTimes getDefaultTimes() {
        // Default to Dhaka times
        return BANGLADESH_PRAYER_TIMES.get("Dhaka").get("Dhaka");
    }
    
    public static class PrayerTimes {
        private final LocalTime fajr;
        private final LocalTime dhuhr;
        private final LocalTime asr;
        private final LocalTime maghrib;
        private final LocalTime isha;
        private final LocalTime suhoor;
        private final LocalTime iftar;
        
        public PrayerTimes(String fajr, String dhuhr, String asr, String maghrib, 
                          String isha, String suhoor, String iftar) {
            this.fajr = LocalTime.parse(fajr, java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
            this.dhuhr = LocalTime.parse(dhuhr, java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
            this.asr = LocalTime.parse(asr, java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
            this.maghrib = LocalTime.parse(maghrib, java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
            this.isha = LocalTime.parse(isha, java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
            this.suhoor = LocalTime.parse(suhoor, java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
            this.iftar = LocalTime.parse(iftar, java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
        }
        
        public LocalTime getFajr() { return fajr; }
        public LocalTime getDhuhr() { return dhuhr; }
        public LocalTime getAsr() { return asr; }
        public LocalTime getMaghrib() { return maghrib; }
        public LocalTime getIsha() { return isha; }
        public LocalTime getSuhoor() { return suhoor; }
        public LocalTime getIftar() { return iftar; }
        
        public String format(LocalTime time) {
            return time.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a"));
        }
    }
} 