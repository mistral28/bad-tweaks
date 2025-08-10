package org.cubewhy;


import org.cubewhy.badtweaks.managers.TweakManger;
import org.cubewhy.badtweaks.tweaks.impl.CosmeticsTweak;

@SuppressWarnings("unused")
public class TweakEntrypoint {
    public static void init(String arg) throws Exception {
        System.out.println("[BadTweaks] Welcome to the tweaker");
        System.out.println("[BadTweaks] args: " + arg);

        // init tweaks
        TweakManger tweakManger = new TweakManger();
//        tweakManger.addTweak(new CosmeticsTweak());

        // apply tweaks
        tweakManger.applyTweaks();
    }
}
