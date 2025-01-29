package com.chatapp.models;

public class ChatThread extends Thread {
    private String user;

    public ChatThread(String user) {
        this.user = user;
    }

    @Override
    public void run() {
        System.out.println(user + " se ha unido al chat.");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(user + " ha salido del chat.");
    }
}
