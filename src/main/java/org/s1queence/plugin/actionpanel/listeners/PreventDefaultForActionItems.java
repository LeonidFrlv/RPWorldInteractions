package org.s1queence.plugin.actionpanel.listeners;

import dev.geco.gsit.api.event.*;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.isActionItem;

public class PreventDefaultForActionItems implements Listener {
    @EventHandler
    private void onPlayerClick(InventoryClickEvent e) {
        if (e.getHotbarButton() != -1 && isActionItem(e.getWhoClicked().getInventory().getItem(e.getHotbarButton()))) {
            e.setCancelled(true);
            return;
        }

        ItemStack currentItem = e.getCurrentItem();
        if (currentItem == null) return;
        if (isActionItem(currentItem)) e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        if (!(e.getRightClicked() instanceof ItemFrame)) return;
        if (isActionItem(e.getPlayer().getInventory().getItemInMainHand())) e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerDropItem(PlayerDropItemEvent e) {
        if (isActionItem(e.getItemDrop().getItemStack())) e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerDeath(PlayerDeathEvent e) {
        for (ItemStack is : e.getDrops()) {
            if (isActionItem(is)) is.setType(Material.AIR);
        }
    }

    @EventHandler
    private void onPlayerSwapHands(PlayerSwapHandItemsEvent e) {
        if (isActionItem(e.getPlayer().getInventory().getItemInMainHand())) e.setCancelled(true);
    }

    public static boolean isEntityHolder(Entity entity) {
        return entity instanceof ArmorStand && entity.getCustomName() != null && entity.getCustomName().equals("rpwi_entity_holder_XXX");
    }

    private static void removeNearbyInvisibleArmorStands(Entity entity) {
        for (Entity en : entity.getNearbyEntities(2, 2, 2)) {
            if (isEntityHolder(en)) en.remove();
        }
    }

    public static void removePassengers(Entity entity) {
        List<Entity> passengers = entity.getPassengers();
        passengers.forEach(entity::removePassenger);
        removeNearbyInvisibleArmorStands(entity);
    }

    @EventHandler
    private void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        if (!e.isSneaking()) return;
        Player player = e.getPlayer();
        if (player.isInsideVehicle()) return;
        removePassengers(player);
    }
    @EventHandler
    private void onPlayerChangeGameMode(PlayerGameModeChangeEvent e) {
        if (e.getNewGameMode().equals(GameMode.SPECTATOR)) removePassengers(e.getPlayer());
    }

    @EventHandler
    private void onPlayerPose(PlayerPoseEvent e) {
        removePassengers(e.getPlayer());
    }

    @EventHandler
    private void onPlayerCrawl(PlayerCrawlEvent e) {
        removePassengers(e.getPlayer());
    }

    @EventHandler
    private void onEntitySit(EntitySitEvent e) {
        removePassengers(e.getEntity());
    }

    @EventHandler
    private void onEntityEmote(EntityEmoteEvent e) {
        removePassengers(e.getEntity());
    }

    @EventHandler
    private void onEntityHangingBreak(HangingBreakEvent e) {
        Entity entity = e.getEntity();
        if (!(entity instanceof ItemFrame)) return;
        ItemFrame frame = (ItemFrame) entity;
        if (frame.isVisible()) return;
        e.setCancelled(true);
        e.getEntity().getWorld().dropItemNaturally(frame.getLocation(), frame.getItem());
        entity.remove();
    }
}
