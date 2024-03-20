package org.s1queence.plugin.actionpanel.listeners.actions;

import dev.geco.gsit.api.GSitAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
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
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.s1queence.api.countdown.progressbar.ProgressBar;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.ActionItemID;
import org.s1queence.plugin.actionpanel.RPActionPanel;
import org.s1queence.plugin.actionpanel.listeners.actions.rummage.Rummage;
import org.s1queence.plugin.actionpanel.utils.ActionPanelUtil;
import org.s1queence.plugin.libs.YamlDocument;

import java.util.List;

import static org.s1queence.api.S1Booleans.isNotAllowableInteraction;
import static org.s1queence.api.S1TextUtils.*;
import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.S1Utils.sendActionBarMsg;
import static org.s1queence.api.countdown.CountDownAction.*;
import static org.s1queence.plugin.actionpanel.listeners.PreventDefaultForActionItems.*;
import static org.s1queence.plugin.actionpanel.listeners.PreventDefaultForActionItems.isEntityHolder;
import static org.s1queence.plugin.actionpanel.listeners.actions.rummage.Rummage.getRummageHandlers;
import static org.s1queence.plugin.actionpanel.listeners.actions.rummage.Rummage.updateRummageInventory;
import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.getActionItemID;
import static org.s1queence.plugin.utils.TextUtils.*;

public class ActionUseListener implements Listener {

    private final RPWorldInteractions plugin;
    public ActionUseListener(RPWorldInteractions plugin) {this.plugin = plugin;}

    private void moveActionToInventory(Player player, ItemStack is) {
        ActionPanelUtil.insertUsageToLore(is, plugin.getItemUsage());
        player.getInventory().setItem(8, is);
    }

    @EventHandler
    private void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        removePassengers(player);
        if (!getRummageHandlers().containsKey(player)) return;
        updateRummageInventory(player);
    }

    @EventHandler
    private void onInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();
        InventoryAction action = e.getAction();
        RPActionPanel rpAP = plugin.getPlayersAndPanels().get(player.getUniqueId().toString());
        if (isPlayerInDoubleRunnableAction(player) && !isPlayerMakingSoloCDAction(player)) {
            ItemStack item = player.getInventory().getItem(8);
            ActionItemID actionID = getActionItemID(item);
            if (actionID != null && actionID.equals(ActionItemID.RUMMAGE)) {
                Player rummageTarget = getDoubleRunnableActionHandlers().get(player);
                if (rummageTarget != null) updateRummageInventory(rummageTarget);
                return;
            }
        }

        if (!e.getClickedInventory().equals(rpAP.getInventory())) return;
        if (!action.equals(InventoryAction.PICKUP_ALL)) {
            e.setCancelled(true);
            return;
        }

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null) return;

        ActionItemID actionID = getActionItemID(clicked);
        if (actionID == null) {
            e.setCancelled(true);
            return;
        }

        Block block = player.getWorld().getBlockAt(player.getLocation().getBlockX(), player.getLocation().getBlockY() - 1, player.getLocation().getBlockZ());

        String actionSoundName = plugin.getOptionsConfig().getString("sounds." + actionID.toString().toLowerCase());

        switch (actionID) {
            case SIT: {
                GSitAPI.createSeat(block, player);
                break;
            }

            case LAY: {
                GSitAPI.createPose(block, player, Pose.SLEEPING);
                break;
            }

            case CRAWL: {
                GSitAPI.startCrawl(player);
                break;
            }

            case VIEW: {
                sendPlayerViewToPlayer(player, player.getName(), plugin);
                break;
            }

            case NOTIFY: {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (!p.hasPermission("rpwi.perms.notifyLog") || !p.isOp()) continue;
                    p.playSound(p.getLocation(), plugin.getOptionsConfig().getString("sounds.notify_all_admins"), 1.0f, 1.0f);
                    TextComponent msg = new TextComponent(ChatColor.RED + "" + ChatColor.UNDERLINE + player.getName());
                    msg.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + player.getName()));
                    msg.addExtra(getConvertedTextFromConfig(plugin.getTextConfig(),"notify.admin_additional_text", plugin.getName()));
                    p.spigot().sendMessage(msg);
                }
                player.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"notify.player_text", plugin.getName()));
                if (player.hasPermission("rpwi.perms.notifyLog")) {
                    actionSoundName = "none";
                    break;
                }
                actionSoundName = plugin.getOptionsConfig().getString("sounds.notify_sender");
                break;
            }

            case LIFT_AND_CARRY:
            case LOOK_AT:
            case PUT:
            case RUMMAGE:
            case PUSH:
            case DROP_BLOCK: {
                moveActionToInventory(player, clicked.clone());
                actionSoundName = plugin.getOptionsConfig().getString("sounds.on_user_select_item");
                break;
            }
        }

        if (actionSoundName != null && !actionSoundName.equalsIgnoreCase("none")) player.playSound(player.getLocation(), actionSoundName, 0.9f, 1.0f);

        player.closeInventory();
        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();
        RPActionPanel rpAP = plugin.getPlayersAndPanels().get(player.getUniqueId().toString());
        if (rpAP != null && !rpAP.getInventory().getViewers().contains(player)) return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerDropItem(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (!getRummageHandlers().containsKey(player)) return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();
        if (!getRummageHandlers().containsKey(player)) return;
        e.setCancelled(true);
    }

    @EventHandler
    private void onPlayerPlaceBlock(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        if (isNotAllowableInteraction(player, block.getLocation())) {
            e.setCancelled(true);
            return;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack item = inv.getItemInMainHand();
        BlockData clonedBlockData = e.getBlock().getBlockData().clone();
        Material blockType = e.getBlock().getType();
        ActionItemID actionID = getActionItemID(item);
        if (actionID == null) return;
        if (actionID.equals(ActionItemID.PUT)) {
            e.setCancelled(true);
            return;
        }
        if (!blockType.isOccluding() && !blockType.toString().contains("STAIRS") && !blockType.toString().contains("LADDER")) return;
        if (item.getType().equals(Material.AIR)) return;
        if (!actionID.equals(ActionItemID.DROP_BLOCK)) return;
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
        ActionItemID actionID = getActionItemID(item);
        if (actionID == null) return;
        if (!actionID.equals(ActionItemID.PUSH)) return;
        if (pusher.getAttackCooldown() == 1.0f) vehicle.setVelocity(pusher.getLocation().getDirection().setY(0).normalize().multiply(0.3f));
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
        ActionItemID actionID = getActionItemID(item);
        if (actionID != null && !actionID.equals(ActionItemID.PUSH)) return;
        if (!(target instanceof LivingEntity) || target.getType().equals(EntityType.ARMOR_STAND)) return;
        if (pusher.getAttackCooldown() == 1.0f) target.setVelocity(pusher.getLocation().getDirection().setY(0).normalize().multiply(1));
        e.setCancelled(true);
        if (!(target instanceof Player)) return;
        Player pTarget = (Player) target;
        if (!getDoubleRunnableActionHandlers().containsValue(pTarget)) getDoubleRunnableActionHandlers().remove(pTarget);
        getDoubleRunnableActionHandlers().remove(pusher);
        if (!getPreprocessActionHandlers().containsValue(pusher)) getPreprocessActionHandlers().remove(pTarget);
        getPreprocessActionHandlers().remove(pusher);
        pTarget.closeInventory();

        String targetMsg = getTextWithInsertedPlayerName(getRandomElemFromStringList(plugin.getTextConfig().getStringList("push_action.target_action_bar_messages")), pusher.getName());
        String playerMsg = ChatColor.translateAlternateColorCodes('&', getRandomElemFromStringList(plugin.getTextConfig().getStringList("push_action.player_action_bar_messages")));
        sendActionBarMsg(pusher, playerMsg);
        sendActionBarMsg(pTarget, targetMsg);
    }

    private void addPlayerPassenger(Player vehicle, Entity passenger) {
        if (passenger instanceof Player && !GSitAPI.isPosing((Player) passenger) && !GSitAPI.isEmoting((Player) passenger)) {
            GSitAPI.sitOnPlayer((Player) passenger, vehicle);
            return;
        }

        ArmorStand as = vehicle.getWorld().spawn(vehicle.getLocation(), ArmorStand.class);
        as.setVisible(false);
        as.setSmall(true);
        as.setCustomName("rpwi_entity_holder_XXX");
        as.setCustomNameVisible(false);
        as.addEquipmentLock(EquipmentSlot.HEAD, ArmorStand.LockType.REMOVING_OR_CHANGING);
        as.addEquipmentLock(EquipmentSlot.CHEST, ArmorStand.LockType.REMOVING_OR_CHANGING);
        as.addEquipmentLock(EquipmentSlot.LEGS, ArmorStand.LockType.REMOVING_OR_CHANGING);
        as.addEquipmentLock(EquipmentSlot.FEET, ArmorStand.LockType.REMOVING_OR_CHANGING);
        vehicle.addPassenger(as);
        as.addPassenger(passenger);
    }

    @EventHandler
    private void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        Player player = e.getPlayer();

        if (isNotAllowableInteraction(player, e.getRightClicked().getLocation())) {
            e.setCancelled(true);
            return;
        }

        if (isPlayerInCountDownAction(player) || plugin.isLaying(player)) return;
        ItemStack item = e.getPlayer().getInventory().getItemInMainHand();
        if (item.getType().equals(Material.AIR)) return;
        ActionItemID actionID = getActionItemID(item);
        if (actionID == null) return;

        Entity entity = e.getRightClicked();
        String eType = entity.getType().toString();

        boolean isPassengersEmpty = player.getPassengers().isEmpty() && entity.getPassengers().isEmpty();
        boolean isNotEntityHolder = !isEntityHolder(entity.getVehicle()) && !isEntityHolder(entity);
        boolean isNotItemFrame = !(entity instanceof ItemFrame);
        if (isPassengersEmpty && actionID.equals(ActionItemID.LIFT_AND_CARRY) && isNotEntityHolder && isNotItemFrame)
            addPlayerPassenger(player, entity);

        if (entity instanceof Player) {
            Player target = (Player) e.getRightClicked();
            if (actionID.equals(ActionItemID.RUMMAGE)) {
                YamlDocument cfg = plugin.getTextConfig();
                YamlDocument optionsCfg = plugin.getOptionsConfig();
                String pName = plugin.getName();
                new Rummage(
                        player,
                        target,
                        optionsCfg.getInt("rummage.seconds"),
                        true,
                        true,
                        new ProgressBar(
                                0,
                                1,
                                optionsCfg.getInt("progress_bar.max_bars"),
                                optionsCfg.getString("progress_bar.symbol"),
                                ChatColor.translateAlternateColorCodes('&', optionsCfg.getString("progress_bar.border_left")),
                                ChatColor.translateAlternateColorCodes('&', optionsCfg.getString("progress_bar.border_right")),
                                ChatColor.getByChar(optionsCfg.getString("progress_bar.color")),
                                ChatColor.getByChar(optionsCfg.getString("progress_bar.complete_color")),
                                ChatColor.getByChar(optionsCfg.getString("progress_bar.percent_color"))
                                ),
                        plugin,
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.every_tick.action_bar_both", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.every_tick.player.title", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.every_tick.player.subtitle", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.every_tick.target.title", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.every_tick.target.subtitle", pName),

                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.complete.action_bar_both", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.complete.player.title", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.complete.player.subtitle", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.complete.target.title", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.complete.target.subtitle", pName),

                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.cancel.action_bar_both", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.cancel.player.title", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.cancel.player.subtitle", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.cancel.target.title", pName),
                        getConvertedTextFromConfig(cfg,"rummage_action.preprocess.cancel.target.subtitle", pName)
                );
            }
            if (actionID.equals(ActionItemID.LOOK_AT)) sendPlayerViewToPlayer(player, target.getName(), plugin);
            return;
        }


        if (actionID.equals(ActionItemID.LOOK_AT)) {
            if (entity instanceof ItemFrame) {
                ItemFrame frame = (ItemFrame) e.getRightClicked();
                ItemStack frameItem = frame.getItem();

                if (frameItem.getType().equals(Material.AIR) || !frameItem.hasItemMeta() || frameItem.getItemMeta() == null) {
                    sendEntityViewToPlayer(player, eType, plugin);
                    return;
                }

                player.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.entity_view_item_frame_text", plugin.getName()));

                ItemMeta itemMeta = frameItem.getItemMeta();
                if (itemMeta.hasDisplayName()) {
                    String name = itemMeta.getDisplayName();
                    player.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.entity_view_item_frame_text_item_name", plugin.getName()) + ChatColor.RESET + name);

                }

                if (itemMeta.hasLore() && itemMeta.getLore() != null && !itemMeta.getLore().isEmpty()) {
                    List<String> lore = itemMeta.getLore();
                    player.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.entity_view_item_frame_text_item_lore", plugin.getName()));
                    for (String current : lore) {
                        player.sendMessage(" " + current);
                    }
                }

                player.playSound(player.getLocation(), plugin.getOptionsConfig().getString("sounds.look_at"), 0.9f, 1.0f);

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
        World world = player.getWorld();
        boolean isInCreative = player.getGameMode().equals(GameMode.CREATIVE);
        ItemStack item = player.getInventory().getItemInMainHand();

        ActionItemID actionID = getActionItemID(item);
        if (actionID == null) return;

        if (player.isSneaking()) {
            if (clicked != null && clicked.getType().isInteractable()) return;
            player.playSound(player.getLocation(), plugin.getOptionsConfig().getString("sounds.open_action_inv"), 0.9f, 1.0f);
            player.openInventory(plugin.getPlayersAndPanels().get(player.getUniqueId().toString()).getInventory());
            return;
        }

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) || clicked == null) return;

        if (!isInCreative && isNotAllowableInteraction(player, clicked.getLocation())) {
            e.setCancelled(true);
            return;
        }

        if (!actionID.equals(ActionItemID.PUT)) return;
        ItemStack itemInOffHand = player.getInventory().getItemInOffHand();
        if (itemInOffHand.getType().equals(Material.AIR)) return;
        if (itemInOffHand.getType().toString().contains("MINECART") || itemInOffHand.getType().toString().contains("BOAT")) return;
        BlockFace blockFace = e.getBlockFace();
        Block faced = clicked.getRelative(blockFace);
        e.setCancelled(true);
        ItemFrame invItemFrame = world.spawn(faced.getLocation(), ItemFrame.class);
        invItemFrame.setVisible(false);
        invItemFrame.setItem(itemInOffHand);
        if (!isInCreative) itemInOffHand.setAmount(itemInOffHand.getAmount() - 1);
        invItemFrame.setFacingDirection(blockFace);
    }
}
