package org.s1queence.plugin.actionpanel.listeners.actions.lookat;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.ActionItemUUID;
import org.s1queence.plugin.libs.YamlDocument;

import java.io.IOException;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.s1queence.api.S1TextUtils.*;
import static org.s1queence.api.S1Utils.sendActionBarMsg;
import static org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands.ViewPaintToolCommand.viewPaintTool;
import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.getActionUUID;
import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.isActionItem;

public class LookAtListener implements Listener {
    private final RPWorldInteractions plugin;
    public LookAtListener(RPWorldInteractions plugin) {
        this.plugin = plugin;
    }

    private boolean isViewPaintTool(ItemStack item) {
        ItemStack vpt = viewPaintTool(plugin);
        if (vpt.getItemMeta() == null) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta im = item.getItemMeta();
        if (im == null) return false;
        if (!im.hasDisplayName()) return false;
        if (!item.getType().equals(vpt.getType())) return false;
        return vpt.getItemMeta().getDisplayName().equals(im.getDisplayName());
    }

    private String getBlockView(Block block) {
        String strLocation = getStringLocation("_", block.getLocation());
        YamlDocument lookAtConfig = plugin.getLookAtConfig();
        String inMarketBlocks = lookAtConfig.getString(String.join(".", "market_blocks", strLocation, "view"));
        String inDefaultBlocks = lookAtConfig.getString(String.join(".", "default_blocks", block.getType().toString()));
        String nullText = getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.no_block_view", plugin.getName());
        return ofNullable(ofNullable(inMarketBlocks).orElse(inDefaultBlocks)).orElse(nullText);
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent e) throws IOException {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        YamlDocument lookAtConfig = plugin.getLookAtConfig();
        String actionUUID = getActionUUID(item);
        if (actionUUID == null || !actionUUID.equals(ActionItemUUID.LOOK_AT.toString())) return;

        int range = lookAtConfig.getInt("range");
        Block targetBlock = player.getTargetBlock(null, range);

        if (targetBlock.getType().equals(Material.AIR)) {
            sendActionBarMsg(player, " ");
            return;
        }

        String strLocation = getStringLocation("_", targetBlock.getLocation());

        String inMarketBlocks = lookAtConfig.getString(String.join(".", "market_blocks", strLocation, "view"));

        String textContent = getBlockView(targetBlock);
        String material = lookAtConfig.getString(String.join(".", "market_blocks", strLocation, "material"));

        if (inMarketBlocks != null && !material.equals(targetBlock.getType().toString())) {
            lookAtConfig.set(String.join(".", "market_blocks", strLocation), null);
            lookAtConfig.save();
            return;
        }

        int limit = lookAtConfig.getInt("max_row_length");
        String overflowSymbol = ChatColor.translateAlternateColorCodes('&', lookAtConfig.getString("overflow_symbol"));
        String output = textContent.length() > limit ? textContent.substring(0, limit) : textContent;
        boolean isOutputSub = output.length() == textContent.length();
        if (!isOutputSub && output.charAt(output.length() - 1) == ' ') output = output.substring(0, output.length() - 1);
        sendActionBarMsg(player, isOutputSub ? output : output + overflowSymbol);
    }

    @EventHandler
    private void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getHand() == null) return;
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
        YamlDocument lookAtConfig = plugin.getLookAtConfig();
        Player player = e.getPlayer();
        if (player.isSneaking()) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        String actionUUID = getActionUUID(item);
        if (actionUUID == null || !actionUUID.equals(ActionItemUUID.LOOK_AT.toString())) return;
        int range = lookAtConfig.getInt("range");
        Block targetBlock = ofNullable(e.getClickedBlock()).orElse(player.getTargetBlock(null, range));
        if (targetBlock.getType().equals(Material.AIR)) return;
        player.sendMessage(getBlockView(targetBlock));
        player.playSound(player.getLocation(), plugin.getOptionsConfig().getString("sounds.look_at"), 0.9f, 1.0f);
    }


    @EventHandler
    private void onPlayerDestroyBlock(BlockBreakEvent e) throws IOException {
        Player player = e.getPlayer();
        if (!player.getGameMode().equals(GameMode.CREATIVE)) return;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (!isViewPaintTool(itemStack)) return;
        ItemMeta im = itemStack.getItemMeta();
        if (im == null) return;
        if (!im.hasLore() || (im.getLore() != null && im.getLore().isEmpty())) {
            e.setCancelled(true);
            return;
        }
        e.setCancelled(true);
        String strLocation = getStringLocation("_", e.getBlock().getLocation());
        StringBuilder blockView = new StringBuilder();
        List<String> lore = im.getLore();
        for (int i = 0; i < lore.size(); i++) {
            String current = lore.get(i);
            if (i == lore.size() - 1) {
                blockView.append(current);
                break;
            }
            blockView.append(current).append(' ');
        }
        if (blockView.toString().isEmpty()) return;
        YamlDocument lookAtCfg = plugin.getLookAtConfig();
        lookAtCfg.set(String.join(".", "market_blocks", strLocation, "view"), blockView.toString());
        lookAtCfg.set(String.join(".", "market_blocks", strLocation, "material"), e.getBlock().getType().toString());
        lookAtCfg.save();
        sendActionBarMsg(player, getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.block_view_add", plugin.getName()));

        for (Entity entity : player.getWorld().getNearbyEntities(e.getBlock().getLocation(), 15.0d, 7.5d, 15.0d)) {
            if (!(entity instanceof Player)) continue;
            Player p = (Player) entity;
            if (p.equals(player)) continue;
            sendActionBarMsg(p, getConvertedTextFromConfig(plugin.getTextConfig(), "lookat.some_changes_near", plugin.getName()));
        }
    }

    @EventHandler
    private void onPlayerInteractBlock(PlayerInteractEvent e) throws IOException {
        if (e.getHand() == null) return;
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        Player player = e.getPlayer();
        if (!player.getGameMode().equals(GameMode.CREATIVE)) return;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (!isViewPaintTool(itemStack)) return;
        if (e.getClickedBlock() == null) return;
        String strLocation = getStringLocation("_", e.getClickedBlock().getLocation());
        YamlDocument lookAtCfg = plugin.getLookAtConfig();
        lookAtCfg.set(String.join(".", "market_blocks", strLocation), null);
        lookAtCfg.save();
        sendActionBarMsg(player, getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.block_view_remove", plugin.getName()));
    }

    @EventHandler
    private void onVehicleEnter(VehicleEnterEvent e) {
        if (!(e.getEntered() instanceof Player)) return;
        Player player = (Player) e.getEntered();
        ItemStack item = player.getInventory().getItemInMainHand();
        String actionUUID = getActionUUID(item);
        if (!isActionItem(item, player, plugin) || actionUUID != null && !actionUUID.equals(ActionItemUUID.LOOK_AT.toString())) return;
        e.setCancelled(true);
    }

}


