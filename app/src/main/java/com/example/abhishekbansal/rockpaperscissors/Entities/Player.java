package com.example.abhishekbansal.rockpaperscissors.Entities;

public class Player {
    private String phoneNumber;
    private double lat;
    private double lng;
    private boolean isLoggedIn;

    /*Constructor*/
    public Player(String phoneNumber, double lat, double lng, boolean isLoggedIn) {
        this.phoneNumber = phoneNumber;
        this.lat = lat;
        this.lng = lng;
        this.isLoggedIn = isLoggedIn;
    }
    /*
    public Player(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.lat = lat;
        this.lng = lng;
        this.isLoggedIn = isLoggedIn;

    }
    */
    /*Getters and setters*/
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    @Override
    public String toString() {
        return " --phoneNumber: " + this.phoneNumber + " --lat: " + this.lat + " --lng: " + this.lng + " --isLoggedIn: " + this.isLoggedIn;
    }
}
