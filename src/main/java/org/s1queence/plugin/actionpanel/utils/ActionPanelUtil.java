package org.s1queence.plugin.actionpanel.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.RPActionPanel;


import java.util.List;

public abstract class ActionPanelUtil {
    public static String getActionUUID(ItemStack item) {
        if (!item.getType().equals(Material.ENCHANTED_BOOK)) return null;
        if (!item.hasItemMeta()) return null;
        if (!item.getItemMeta().hasLore()) return null;
        String llr = item.getItemMeta().getLore().get(item.getItemMeta().getLore().size() - 1);
        if (!RPActionPanel.isEnabledAction(llr)) return null;
        return llr;
    }

    public static boolean isActionItem(ItemStack item, RPWorldInteractions plugin) {
        String uuid = getActionUUID(item);
        if (uuid == null) return false;
        if (uuid.contains("#perm") || uuid.contains("#temp")) return true;
        RPActionPanel rpAP = new RPActionPanel(null, plugin);
        for (ItemStack action : rpAP.getActionItemsList()) {
            ItemStack cloned = action.clone();
            insertLoreBeforeUUID(cloned, plugin.getItemUsage());
            if (cloned.equals(item)) return true;
        }

        return false;
    }

    public static void insertLoreBeforeUUID(ItemStack is, List<String> loreToInsert) {
        ItemMeta im = is.getItemMeta();
        List<String> lore = is.getItemMeta().getLore();
        String removed = lore.get(lore.size() - 1);
        lore.remove(lore.size() - 1);
        for (String row : loreToInsert) {
            lore.add(ChatColor.translateAlternateColorCodes('&', row));
        }
        lore.add(removed);
        im.setLore(lore);
        is.setItemMeta(im);
    }

    public static void addDefaultActionItem(Player player, RPWorldInteractions plugin) {
        RPActionPanel rpAP = new RPActionPanel(null, plugin);
        ItemStack cloned = rpAP.getActionItemsList().get(0).clone();
        ActionPanelUtil.insertLoreBeforeUUID(cloned, plugin.getItemUsage());
        player.getInventory().setItem(8, cloned);
    }
}
