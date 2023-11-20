package org.s1queence.plugin.actionpanel.listeners;

import dev.geco.gsit.api.GSitAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.listeners.actions.Rummage;
import org.s1queence.plugin.actionpanel.utils.ActionPanelUtil;
import org.s1queence.plugin.utils.MyUtils;
import org.s1queence.plugin.utils.TextUtils;

import java.util.List;

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
    private void onPlayerPlaceBlock(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        PlayerInventory inv = player.getInventory();
        ItemStack item = inv.getItemInMainHand();
        BlockData clonedBlockData = e.getBlock().getBlockData().clone();
        Material blockType = e.getBlock().getType();
        if (!blockType.isOccluding() && !blockType.toString().contains("STAIRS")) return;
        if (item.getType().equals(Material.AIR)) return;
        String itemUUID = ActionPanelUtil.getActionUUID(item);
        if (itemUUID == null || !ActionPanelUtil.isActionItem(item, plugin)) return;
        if (!itemUUID.contains("#dropblock")) return;
        Location blockLocation = e.getBlock().getLocation();
        Location newLocation = blockLocation.add(0.5d, 0.0d, 0.5d);
        e.getBlock().setType(Material.AIR);
        e.getBlock().getWorld().spawnFallingBlock(newLocation, clonedBlockData);
    }


    @EventHandler
    private void onVehicleDamage(VehicleDamageEvent e) {
        if (!(e.getAttacker() instanceof Player)) return;
        Player pusher = (Player) e.getAttacker();
        Vehicle vehicle = e.getVehicle();
        ItemStack item = pusher.getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) return;
        String itemUUID = ActionPanelUtil.getActionUUID(item);
        if (!ActionPanelUtil.isActionItem(item, plugin)) return;
        if (!itemUUID.contains("#push")) return;
        vehicle.setVelocity(pusher.getLocation().getDirection().setY(0).normalize().multiply(0.3f));
    }

    private String getRandomElemFromStringList(List<String> list) {
        return list.get((int) (Math.random() * list.size()));
    }
    @EventHandler
    private void onPlayerDamageEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        if (e.getEntity() instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) e.getEntity();
            if (frame.isVisible() || !frame.isEmpty()) return;
            frame.getWorld().dropItemNaturally(frame.getLocation(), frame.getItem());
            frame.remove();
            return;
        }

        Player pusher = (Player) e.getDamager();
        Entity target = e.getEntity();
        ItemStack item = pusher.getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) return;
        String itemUUID = ActionPanelUtil.getActionUUID(item);
        if (!ActionPanelUtil.isActionItem(item, plugin)) return;
        if (!itemUUID.contains("#push")) return;
        if (!(target instanceof LivingEntity) || target.getType().equals(EntityType.ARMOR_STAND)) return;
        target.setVelocity(pusher.getLocation().getDirection().setY(0).normalize().multiply(1));
        e.setCancelled(true);
        if (!(target instanceof Player)) return;
        String targetMsg = TextUtils.insertPlayerName(getRandomElemFromStringList(plugin.getTextConfig().getStringList("push_action.target_action_bar_messages")), pusher.getName());
        String playerMsg = getRandomElemFromStringList(plugin.getTextConfig().getStringList("push_action.player_action_bar_messages"));
        MyUtils.sendActionBarMsg(pusher, playerMsg);
        MyUtils.sendActionBarMsg((Player) target, targetMsg);
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        Player player = e.getPlayer();

        if (plugin.isPlayerInAction(player) || !plugin.isActionCoolDownExpired(player) || plugin.isLaying(player)) return;
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) return;
        String itemUUID = ActionPanelUtil.getActionUUID(item);
        if (itemUUID == null || !ActionPanelUtil.isActionItem(item, plugin)) return;

        if (e.getRightClicked() instanceof Player && itemUUID.contains("#rummage")) {
            Player target = (Player) e.getRightClicked();
            new Rummage(player, target, plugin);
        }
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() == null) return;
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        Action action = e.getAction();
        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) return;
        Block clicked = e.getClickedBlock();
        if (clicked != null && clicked.getType().isInteractable()) return;
        Player player = e.getPlayer();
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (!ActionPanelUtil.isActionItem(itemInMainHand, plugin)) return;
        if (player.isSneaking()) {
            player.openInventory(plugin.getRPActionPanel().getInventory());
            return;
        }
        if (!action.equals(Action.RIGHT_CLICK_BLOCK) || clicked == null) return;

        String itemUUID = ActionPanelUtil.getActionUUID(itemInMainHand);
        if (itemUUID != null && !itemUUID.contains("#put")) return;

        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
        if (itemInOffHand.getType().equals(Material.AIR)) return;

        BlockFace blockFace = e.getBlockFace();
        Block faced = clicked.getRelative(blockFace);
        e.setCancelled(true);
        World world = player.getWorld();
        ItemFrame invItemFrame = world.spawn(faced.getLocation(), ItemFrame.class);
        invItemFrame.setVisible(false);
        invItemFrame.setItem(itemInOffHand);
        itemInOffHand.setAmount(itemInOffHand.getAmount() - 1);
        invItemFrame.setFacingDirection(blockFace);
    }
}
