package org.s1queence.plugin.utils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.s1queence.plugin.RPWorldInteractions;

import static org.s1queence.plugin.utils.MyUtils.empty;

public class BarrierClickListener implements Listener {
    private final RPWorldInteractions plugin;
    public BarrierClickListener(RPWorldInteractions plugin) {this.plugin = plugin;}
    @EventHandler
    private void onPlayerClick(InventoryClickEvent e) {
        Inventory clickedInventory = e.getClickedInventory();
        if (clickedInventory == null) return;
        int slot = e.getSlot();
        ItemStack clickedItem = clickedInventory.getItem(slot);
        if (clickedItem != null && clickedItem.equals(empty(plugin))) e.setCancelled(true);
    }
}
