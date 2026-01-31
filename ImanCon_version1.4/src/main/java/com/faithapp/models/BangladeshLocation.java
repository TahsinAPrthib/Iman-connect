package com.faithapp.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class BangladeshLocation {
    private static final Map<String, List<String>> divisionCities = new HashMap<>();
    
    static {
        // Dhaka Division
        divisionCities.put("Dhaka", List.of(
            "Dhaka", "Gazipur", "Narayanganj", "Tangail", "Narsingdi",
            "Manikganj", "Munshiganj", "Rajbari", "Madaripur", "Gopalganj",
            "Faridpur", "Shariatpur", "Kishoreganj"
        ));
        
        // Chittagong Division
        divisionCities.put("Chittagong", List.of(
            "Chittagong", "Cox's Bazar", "Rangamati", "Bandarban", "Khagrachari",
            "Feni", "Lakshmipur", "Noakhali", "Chandpur", "Comilla", "Brahmanbaria"
        ));
        
        // Rajshahi Division
        divisionCities.put("Rajshahi", List.of(
            "Rajshahi", "Natore", "Naogaon", "Chapainawabganj", "Pabna",
            "Bogra", "Sirajganj", "Joypurhat"
        ));
        
        // Khulna Division
        divisionCities.put("Khulna", List.of(
            "Khulna", "Bagerhat", "Satkhira", "Jessore", "Magura",
            "Jhenaidah", "Narail", "Kushtia", "Chuadanga", "Meherpur"
        ));
        
        // Barisal Division
        divisionCities.put("Barisal", List.of(
            "Barisal", "Bhola", "Barguna", "Pirojpur", "Patuakhali", "Jhalokati"
        ));
        
        // Sylhet Division
        divisionCities.put("Sylhet", List.of(
            "Sylhet", "Moulvibazar", "Habiganj", "Sunamganj"
        ));
        
        // Rangpur Division
        divisionCities.put("Rangpur", List.of(
            "Rangpur", "Dinajpur", "Kurigram", "Gaibandha", "Nilphamari",
            "Panchagarh", "Thakurgaon", "Lalmonirhat"
        ));
        
        // Mymensingh Division
        divisionCities.put("Mymensingh", List.of(
            "Mymensingh", "Jamalpur", "Sherpur", "Netrokona"
        ));
    }
    
    public static List<String> getDivisions() {
        return new ArrayList<>(divisionCities.keySet());
    }
    
    public static List<String> getCitiesForDivision(String division) {
        return divisionCities.getOrDefault(division, new ArrayList<>());
    }
} 