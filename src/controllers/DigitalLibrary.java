/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMain.java to edit this template
 */
package controllers;

/**
 * Legacy digital library entrypoint compatibility wrapper.
 * This class redirects main to the new MainApp launcher.
 * Keeping it avoids breaking existing run configurations.
 */
public class DigitalLibrary {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
