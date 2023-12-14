package org.s1queence.plugin.actionpanel.listeners;

import org.bukkit.Material;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
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

import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.isActionItem;

public class PreventDefaultForActionItems implements Listener {
    private final RPWorldInteractions plugin;
    public PreventDefaultForActionItems(RPWorldInteractions plugin) {this.plugin = plugin;}

    @EventHandler
    private void onPlayerClickAtActionItem(InventoryClickEvent e) {
        try {
            if (isActionItem(e.getCurrentItem(), (Player) e.getWhoClicked(), plugin)) e.setCancelled(true);
        } catch (NullPointerException ignored) {

        }
    }

    @EventHandler
    private void onPlayerInteractWithItemFrame(PlayerInteractEntityEvent e) {
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        if (!(e.getRightClicked() instanceof ItemFrame)) return;
        if (isActionItem(e.getPlayer().getInventory().getItemInMainHand(), e.getPlayer(), plugin)) e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerDropLookAtItem(PlayerDropItemEvent e) {
        if (isActionItem(e.getItemDrop().getItemStack(), e.getPlayer(), plugin)) e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
        for (ItemStack is : e.getDrops())
            if (is != null && isActionItem(is, e.getEntity(), plugin)) is.setType(Material.AIR);
    }

    @EventHandler
    private void onPlayerSwapHands(PlayerSwapHandItemsEvent e) {
        if (isActionItem(e.getPlayer().getInventory().getItemInMainHand(), e.getPlayer(), plugin)) e.setCancelled(true);
    }

}
