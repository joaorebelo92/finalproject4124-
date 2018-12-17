package com.example.abhishekbansal.rockpaperscissors.Entities;


public class GameRoom {
    private String player1Number;
    private String player2Number;
    private String player1Option;
    private String player2Option;
    private String Winner;

    public GameRoom(String player1Number, String player2Number, String player1Option, String player2Option, String winner) {
        this.player1Number = player1Number;
        this.player2Number = player2Number;
        this.player1Option = player1Option;
        this.player2Option = player2Option;
        Winner = winner;
    }

    /*Getters and Setters*/

    public String getPlayer1Number() {
        return player1Number;
    }

    public void setPlayer1Number(String player1Number) {
        this.player1Number = player1Number;
    }

    public String getPlayer2Number() {
        return player2Number;
    }

    public void setPlayer2Number(String player2Number) {
        this.player2Number = player2Number;
    }

    public String getPlayer1Option() {
        return player1Option;
    }

    public void setPlayer1Option(String player1Option) {
        this.player1Option = player1Option;
    }

    public String getPlayer2Option() {
        return player2Option;
    }

    public void setPlayer2Option(String player2Option) {
        this.player2Option = player2Option;
    }

    public String getWinner() {
        return Winner;
    }

    public void setWinner(String winner) {
        Winner = winner;
    }
}

