package org.s1queence.plugin.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class MyUtils {
    public static void sendActionBarMsg(Player player, String msg) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
    }

    public static ItemStack empty() {
        ItemStack empty = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta emptyMeta = empty.getItemMeta();
        emptyMeta.setDisplayName(ChatColor.RED + "Ограничитель");
        empty.setItemMeta(emptyMeta);
        return empty;
    }
}
