package org.s1queence.plugin.actionpanel.listeners;

import dev.geco.gsit.api.GSitAPI;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.listeners.actions.Rummage;
import org.s1queence.plugin.actionpanel.utils.ActionPanelUtil;

public class ActionUseListener implements Listener {

    private final RPWorldInteractions plugin;
    public ActionUseListener(RPWorldInteractions plugin) {this.plugin = plugin;}

    private void moveActionToInventory(Player player, ItemStack is) {
        ActionPanelUtil.insertLoreBeforeEnd(is, plugin.getItemUsage());
        player.getInventory().setItem(8, is);
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!e.getClickedInventory().equals(plugin.getRPActionPanel().getInventory())) return;
        if (!e.getAction().equals(InventoryAction.PICKUP_ALL)) {
            e.setCancelled(true);
            return;
        }
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;
        String itemUUID = ActionPanelUtil.getActionUUID(clicked);
        if (itemUUID == null) {
            e.setCancelled(true);
            return;
        }

        Player player = (Player) e.getWhoClicked();
        Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());

        if (itemUUID.contains("#sit")) {
            GSitAPI.createSeat(block, player);
        }

        if (itemUUID.contains("#lay")) {
            GSitAPI.createPose(block, player, Pose.SLEEPING);
        }

        if (itemUUID.contains("#crawl")) {
            GSitAPI.startCrawl(player);
        }

        if (!itemUUID.contains("#crawl") && !itemUUID.contains("#lay") && !itemUUID.contains("#sit") && !itemUUID.contains("#close")) {
            moveActionToInventory(player, clicked.clone());
        }

        player.closeInventory();
        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        Player player = e.getPlayer();
        if (e.getRightClicked() instanceof ItemFrame && !player.getGameMode().equals(GameMode.CREATIVE)) {
            ItemFrame frame = (ItemFrame) e.getRightClicked();
            if (frame.isVisible()) return;
            if (!frame.isEmpty()) return;
            frame.remove();
            return;
        }

        if (plugin.isPlayerInAction(player) || !plugin.isActionCoolDownExpired(player) || plugin.isLaying(player)) return;
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) return;
        String itemUUID = ActionPanelUtil.getActionUUID(item);
        if (itemUUID == null || !ActionPanelUtil.isActionItem(item, plugin)) return;

        if (e.getRightClicked() instanceof Player && itemUUID.contains("#rummage") ) {
            Player target = (Player) e.getRightClicked();
            new Rummage(player, target, plugin);
        }
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() == null) return;
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
        if (e.getClickedBlock() != null && e.getClickedBlock().getType().isInteractable()) return;
        Player player = e.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (!ActionPanelUtil.isActionItem(itemInMainHand, plugin)) return;

        if (!player.isSneaking()) return;
        player.openInventory(plugin.getRPActionPanel().getInventory());
    }
}
