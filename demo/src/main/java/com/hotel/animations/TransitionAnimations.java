package com.hotel.animations;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public class TransitionAnimations {
    
    public static void fadeIn(Node node, Duration duration) {
        FadeTransition fadeIn = new FadeTransition(duration, node);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    public static void fadeOut(Node node, Duration duration) {
        FadeTransition fadeOut = new FadeTransition(duration, node);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.play();
    }
    
    public static void fadeInOut(Node node, Duration duration) {
        FadeTransition fadeInOut = new FadeTransition(duration, node);
        fadeInOut.setFromValue(0.0);
        fadeInOut.setToValue(1.0);
        fadeInOut.setAutoReverse(true);
        fadeInOut.setCycleCount(2);
        fadeInOut.play();
    }
}