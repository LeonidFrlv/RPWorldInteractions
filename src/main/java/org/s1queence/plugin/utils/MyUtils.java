package org.s1queence.plugin.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.s1queence.plugin.RPWorldInteractions;

public class MyUtils {
    public static void sendActionBarMsg(Player player, String msg) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }
    public static ItemStack empty(RPWorldInteractions plugin) {
        return TextUtils.createItemFromConfig(plugin.getTextConfig().getSection("empty_item").getStringRouteMappedValues(true), false);
    }
}
