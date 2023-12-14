package org.s1queence.plugin.actionpanel.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.ActionItemUUID;
import org.s1queence.plugin.actionpanel.RPActionPanel;

import java.util.List;

public abstract class ActionPanelUtil {
    public static boolean isActionItemUUID(String str) {
        for (ActionItemUUID aiuuid : ActionItemUUID.values()) {
            if (aiuuid.toString().equals(str)) return true;
        }
        return false;
    }

    public static String getActionUUID(ItemStack item) {
        if (!item.getType().equals(Material.ENCHANTED_BOOK)) return null;
        if (!item.hasItemMeta()) return null;
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null) return null;
        if (!itemMeta.hasLore()) return null;
        List<String> lore = itemMeta.getLore();
        if (lore == null || lore.isEmpty()) return null;
        String llr = lore.get(lore.size() - 1);
        if (!isActionItemUUID(llr)) return null;
        return llr;
    }

    public static boolean isActionItem(ItemStack item, Player owner, RPWorldInteractions plugin) {
        String uuid = getActionUUID(item);
        if (uuid == null) return false;
        String pUUID = owner.getUniqueId().toString();
        RPActionPanel rpAP = plugin.getPlayersAndPanels().get(pUUID);
        if (rpAP == null) return false;

        for (ItemStack action : rpAP.getActionItemsList()) {
            ItemStack cloned = action.clone();
            insertLoreBeforeUUID(cloned, plugin.getItemUsage());
            if (cloned.equals(item)) return true;
        }

        return false;
    }

    public static void insertLoreBeforeUUID(ItemStack is, List<String> loreToInsert) {
        ItemMeta im = is.getItemMeta();
        if (im == null) return;
        List<String> lore = is.getItemMeta().getLore();
        if (lore == null) return;
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
        RPActionPanel rpAP = new RPActionPanel(player, plugin);
        List<ItemStack> actionItemsList = rpAP.getActionItemsList();
        if (actionItemsList.isEmpty()) return;
        ItemStack cloned = actionItemsList.get(0).clone();
        ActionPanelUtil.insertLoreBeforeUUID(cloned, plugin.getItemUsage());
        player.getInventory().setItem(8, cloned);
    }
}
