package org.s1queence.plugin.actionpanel.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
        if (getActionUUID(item) == null) return false;
        for (ItemStack action : plugin.getRPActionPanel().getActionsList()) {
            ItemStack cloned = action.clone();
            insertLoreBeforeEnd(cloned, plugin.getInfoLore());
            if (cloned.equals(item)) return true;
        }

        return false;
    }

    public static void insertLoreBeforeEnd(ItemStack is, List<String> loreToInsert) {
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
}
