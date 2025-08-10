package org.cubewhy.badtweaks.managers;

import org.cubewhy.badtweaks.tweaks.Tweak;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class TweakManger {
    private final List<Tweak> enabledTweaks = new ArrayList<>();

    public void addTweak(Tweak tweak) {
        enabledTweaks.add(tweak);
    }

    public void applyTweaks() {
        this.enabledTweaks.forEach(tweak -> {
            String tweakClassName = tweak.getClass().getName();
            try {
                System.out.println("Applying tweak " + tweakClassName);
                tweak.apply();
                System.out.println("Success applied tweak " + tweakClassName);
            } catch (Exception e) {
                System.out.println("Failed to apply tweak " + tweakClassName);
                e.printStackTrace();
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                JOptionPane.showMessageDialog(null, "Failed to init tweak " + tweakClassName + "\n" + sw, "BadTweaks", JOptionPane.ERROR_MESSAGE);
            }
        });
        System.out.println("Success applied tweaks!");
    }
}
