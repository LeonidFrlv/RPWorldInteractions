package org.s1queence.plugin.actionpanel.utils;

import org.bukkit.ChatColor;

public class ProgressBar {
    public static String repeat(String ch, int count) {
        StringBuilder buffer = new StringBuilder();
        for ( int i = 0; i < count; ++i )
            buffer.append(ch);

        return buffer.toString();
    }

    public static String getProgressBar(int current, int max, int totalBars, char symbol, ChatColor completedColor, ChatColor notCompletedColor) {
        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        return repeat("" + completedColor + symbol, progressBars) + repeat("" + notCompletedColor + symbol, totalBars - progressBars);
    }
}
