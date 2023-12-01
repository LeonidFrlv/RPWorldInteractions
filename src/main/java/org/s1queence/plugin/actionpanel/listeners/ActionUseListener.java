package org.s1queence.plugin.actionpanel.listeners;

import dev.dejvokep.boostedyaml.YamlDocument;
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
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.RPActionPanel;
import org.s1queence.plugin.actionpanel.listeners.actions.coop.Rummage;
import org.s1queence.plugin.actionpanel.utils.ActionPanelUtil;

import java.util.List;

import static org.s1queence.api.S1Booleans.isAllowableInteraction;
import static org.s1queence.api.S1Utils.sendActionBarMsg;
import static org.s1queence.plugin.utils.TextUtils.*;

public class ActionUseListener implements Listener {

    private final RPWorldInteractions plugin;
    public ActionUseListener(RPWorldInteractions plugin) {this.plugin = plugin;}

    private void moveActionToInventory(Player player, ItemStack is) {
        ActionPanelUtil.insertLoreBeforeUUID(is, plugin.getItemUsage());
        player.getInventory().setItem(8, is);
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        RPActionPanel rpAP = plugin.getPlayersAndPanels().get(player.getUniqueId().toString());
        if (!e.getClickedInventory().equals(rpAP.getInventory())) return;
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

        Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());

        if (itemUUID.contains("#sit")) {
            GSitAPI.createSeat(block, player);
            if (plugin.isSitSound()) player.playSound(player.getLocation(), "rpwi.sit-lay-crawl", 0.9f, 1.0f);
        }

        if (itemUUID.contains("#lay")) {
            GSitAPI.createPose(block, player, Pose.SLEEPING);
            if (plugin.isLaySound()) player.playSound(player.getLocation(), "rpwi.sit-lay-crawl", 0.9f, 1.0f);
        }

        if (itemUUID.contains("#crawl")) {
            GSitAPI.startCrawl(player);
            if (plugin.isCrawlSound()) player.playSound(player.getLocation(), "rpwi.sit-lay-crawl", 0.9f, 1.0f);
        }

        if (itemUUID.contains("#perm") || itemUUID.contains("#temp")) {
            sendPlayerViewToPlayer(player, player.getName(), plugin);
        }

        if (!itemUUID.contains("#crawl") && !itemUUID.contains("#lay") && !itemUUID.contains("#sit") && !itemUUID.contains("#perm") && !itemUUID.contains("#temp")) {
            if (!itemUUID.contains("#close")) moveActionToInventory(player, clicked.clone());
            if (plugin.isSelectActionItemSound()) player.playSound(player.getLocation(), "rpwi.select_action-item", 0.9f, 1.0f);
        }

        player.closeInventory();
        e.setCancelled(true);
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

    @EventHandler
    private void onPlayerPlaceBlock(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        String errorText = isAllowableInteraction(player, block.getLocation());
        if (errorText != null && !player.getGameMode().equals(GameMode.CREATIVE)) {
            sendActionBarMsg(player, errorText);
            e.setCancelled(true);
            return;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack item = inv.getItemInMainHand();
        BlockData clonedBlockData = e.getBlock().getBlockData().clone();
        Material blockType = e.getBlock().getType();
        String itemUUID = ActionPanelUtil.getActionUUID(item);
        if (!ActionPanelUtil.isActionItem(item, plugin)) return;
        if (itemUUID.contains("#put")) {
            e.setCancelled(true);
            return;
        }
        if (!blockType.isOccluding() && !blockType.toString().contains("STAIRS")) return;
        if (item.getType().equals(Material.AIR)) return;
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
        String targetMsg = insertPlayerName(getRandomElemFromStringList(plugin.getTextConfig().getStringList("push_action.target_action_bar_messages")), pusher.getName());
        String playerMsg = ChatColor.translateAlternateColorCodes('&', getRandomElemFromStringList(plugin.getTextConfig().getStringList("push_action.player_action_bar_messages")));
        sendActionBarMsg(pusher, playerMsg);
        sendActionBarMsg((Player) target, targetMsg);
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        Player player = e.getPlayer();

        String errorText = isAllowableInteraction(player, e.getRightClicked().getLocation());
        if (errorText != null && !player.getGameMode().equals(GameMode.CREATIVE)) {
            sendActionBarMsg(player, errorText);
            e.setCancelled(true);
            return;
        }

        if (plugin.isPlayerInAction(player) || !plugin.isActionCoolDownExpired(player) || plugin.isLaying(player)) return;
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) return;
        String itemUUID = ActionPanelUtil.getActionUUID(item);
        if (itemUUID == null || !ActionPanelUtil.isActionItem(item, plugin)) return;

        Entity entity = e.getRightClicked();
        String eType = entity.getType().toString();

        if (entity instanceof Player) {
            Player target = (Player) e.getRightClicked();
            if (itemUUID.contains("#rummage")) new Rummage(player, target, plugin);
            if (itemUUID.contains("#lookat")) sendPlayerViewToPlayer(player, target.getName(), plugin);
            return;
        }

        if (itemUUID.contains("#lookat")) {
            if (entity instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) e.getRightClicked();
                ItemStack frameItem = frame.getItem();

                if (frameItem.getType().equals(Material.AIR) || !frameItem.hasItemMeta() || frameItem.getItemMeta() == null) {
                    sendEntityViewToPlayer(player, eType, plugin);
                    return;
                }

                player.sendMessage(getMsg("lookat.entity_view_item_frame_text", plugin.getTextConfig()));

                ItemMeta itemMeta = frameItem.getItemMeta();
                if (itemMeta.hasDisplayName()) {
                    String name = itemMeta.getDisplayName();
                    player.sendMessage(getMsg("lookat.entity_view_item_frame_text_item_name", plugin.getTextConfig()) + ChatColor.RESET + name);
                }

                if (itemMeta.hasLore() && itemMeta.getLore() != null && !itemMeta.getLore().isEmpty()) {
                    List<String> lore = itemMeta.getLore();
                    player.sendMessage(getMsg("lookat.entity_view_item_frame_text_item_lore", plugin.getTextConfig()));
                    for (String current : lore) {
                        player.sendMessage(" " + current);
                    }
                }

                if (plugin.isLookAtSound()) player.playSound(player.getLocation(), "rpwi.lookat", 0.9f, 1.0f);

                return;
            }
            sendEntityViewToPlayer(player, eType, plugin);
        }
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() == null) return;
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        Action action = e.getAction();
        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.RIGHT_CLICK_AIR)) return;
        Block clicked = e.getClickedBlock();
        Player player = e.getPlayer();
        boolean isInCreative = player.getGameMode().equals(GameMode.CREATIVE);
        ItemStack itemInMainHand = player.getInventory().getItemInMainHand();
        if (!ActionPanelUtil.isActionItem(itemInMainHand, plugin)) return;
        if (player.isSneaking()) {
            if (clicked != null && clicked.getType().isInteractable()) return;
            if (plugin.isOpenSound()) player.playSound(player.getLocation(), "rpwi.inv_open", 0.9f, 1.0f);
            player.openInventory(plugin.getPlayersAndPanels().get(player.getUniqueId().toString()).getInventory());
            return;
        }

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) || clicked == null) return;

        String errorText = isAllowableInteraction(player, e.getClickedBlock().getLocation());
        if (errorText != null && !isInCreative) {
            sendActionBarMsg(player, errorText);
            e.setCancelled(true);
            return;
        }

        String itemUUID = ActionPanelUtil.getActionUUID(itemInMainHand);
        if (itemUUID != null && !itemUUID.contains("#put")) return;

        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
        if (itemInOffHand.getType().equals(Material.AIR)) return;
        if (itemInOffHand.getType().toString().contains("MINECART") || itemInOffHand.getType().toString().contains("BOAT")) return;
        BlockFace blockFace = e.getBlockFace();
        Block faced = clicked.getRelative(blockFace);
        e.setCancelled(true);
        World world = player.getWorld();
        ItemFrame invItemFrame = world.spawn(faced.getLocation(), ItemFrame.class);
        invItemFrame.setVisible(false);
        invItemFrame.setItem(itemInOffHand);
        if (!isInCreative) itemInOffHand.setAmount(itemInOffHand.getAmount() - 1);
        invItemFrame.setFacingDirection(blockFace);
    }
}
