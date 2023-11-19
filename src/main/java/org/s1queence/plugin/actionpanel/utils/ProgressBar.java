package org.s1queence.plugin.actionpanel.utils;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.ChatColor;
import org.s1queence.plugin.RPWorldInteractions;

public class ProgressBar {
    public static String repeat(String ch, int count) {
        StringBuilder buffer = new StringBuilder();
        for ( int i = 0; i < count; ++i )
            buffer.append(ch);

        return buffer.toString();
    }

    public static String getProgressBar(int current, int max, RPWorldInteractions plugin) {
        YamlDocument textConfig = plugin.getTextConfig();
        int totalBars = textConfig.getInt("progress_bar.max_bars");
        String symbol = textConfig.getString("progress_bar.symbol");
        String borderLeft = ChatColor.translateAlternateColorCodes('&',  textConfig.getString("progress_bar.border_left"));
        String borderRight = ChatColor.translateAlternateColorCodes('&',  textConfig.getString("progress_bar.border_right"));
        ChatColor color = ChatColor.getByChar(textConfig.getString("progress_bar.color"));
        ChatColor completeColor = ChatColor.getByChar(textConfig.getString("progress_bar.complete_color"));

        float percent = (float) current / max;
        int progressBars = (int) (totalBars * percent);

        return borderLeft + (repeat("" + completeColor  + symbol, progressBars) + repeat("" + color + symbol, totalBars - progressBars)) + borderRight;
    }
}
