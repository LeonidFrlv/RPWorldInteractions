package org.s1queence.plugin.actionpanel.utils;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.ActionItemID;
import org.s1queence.plugin.actionpanel.RPActionPanel;

import java.util.List;

public abstract class ActionPanelUtil {

    public static ActionItemID getActionItemID(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return null;
        return ActionItemID.fromString(new NBTItem(item).getString("rpwi_action_type"));
    }

    public static boolean isActionItem(ItemStack item) {
        if (item == null || item.getType().equals(Material.AIR)) return false;
        return ActionItemID.fromString(new NBTItem(item).getString("rpwi_action_type")) != null;
    }

    public static void insertUsageToLore(ItemStack is, List<String> loreToInsert) {
        ItemMeta im = is.getItemMeta();
        if (im == null) return;
        List<String> lore = is.getItemMeta().getLore();
        if (lore == null) return;
        loreToInsert.forEach(row -> lore.add(ChatColor.translateAlternateColorCodes('&', row)));
        im.setLore(lore);
        is.setItemMeta(im);
    }

    public static void addDefaultActionItem(Player player, RPWorldInteractions plugin) {
        RPActionPanel rpAP = new RPActionPanel(player, plugin);
        List<ItemStack> actionItemsList = rpAP.getActionItemsList();
        if (actionItemsList.isEmpty()) return;
        ItemStack cloned = actionItemsList.get(0).clone();
        ActionPanelUtil.insertUsageToLore(cloned, plugin.getItemUsage());
        player.getInventory().setItem(8, cloned);
    }
}
