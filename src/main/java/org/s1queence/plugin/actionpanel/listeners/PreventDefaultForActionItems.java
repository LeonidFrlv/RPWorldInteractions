package org.s1queence.plugin.actionpanel.listeners;

import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.utils.ActionPanelUtil;

public class PreventDefaultForActionItems implements Listener {
    private final RPWorldInteractions plugin;
    public PreventDefaultForActionItems(RPWorldInteractions plugin) {this.plugin = plugin;}

    @EventHandler
    private void onPlayerClickAtLookAtItem(InventoryClickEvent e) {
        try {
            if (ActionPanelUtil.isActionItem(e.getCurrentItem(), plugin)) e.setCancelled(true);
        } catch (NullPointerException ignored) {

        }
    }

    @EventHandler
    private void onPlayerInteractWithItemFrame(PlayerInteractEntityEvent e) {
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        if (!(e.getRightClicked() instanceof ItemFrame)) return;
        if (ActionPanelUtil.isActionItem(e.getPlayer().getInventory().getItemInMainHand(), plugin)) e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerDropLookAtItem(PlayerDropItemEvent e) {
        if (ActionPanelUtil.isActionItem(e.getItemDrop().getItemStack(), plugin)) e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
        for (ItemStack is : e.getDrops())
            if (is != null && ActionPanelUtil.isActionItem(is, plugin)) is.setType(Material.AIR);
    }

    @EventHandler
    private void onPlayerSwapHands(PlayerSwapHandItemsEvent e) {
        if (ActionPanelUtil.isActionItem(e.getPlayer().getInventory().getItemInMainHand(), plugin)) e.setCancelled(true);
    }

}