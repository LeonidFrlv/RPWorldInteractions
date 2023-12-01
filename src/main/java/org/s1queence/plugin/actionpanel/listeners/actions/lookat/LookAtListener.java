package org.s1queence.plugin.actionpanel.listeners.actions.lookat;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.s1queence.plugin.RPWorldInteractions;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static java.util.Optional.ofNullable;
import static org.s1queence.api.S1Utils.sendActionBarMsg;
import static org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands.ViewPaintToolCommand.viewPaintTool;
import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.getActionUUID;
import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.isActionItem;
import static org.s1queence.plugin.utils.TextUtils.getMsg;
import static org.s1queence.plugin.utils.TextUtils.getRowsList;

public class LookAtListener implements Listener {
    private final RPWorldInteractions plugin;
    public LookAtListener(RPWorldInteractions plugin) {
        this.plugin = plugin;
    }

    private String getStringLocation(Location loc) {
        return String.join("_", String.valueOf(loc.getBlockX()), String.valueOf(loc.getBlockY()), String.valueOf(loc.getBlockZ()));
    }

    private boolean isViewPaintTool(ItemStack item) {
        ItemStack vpt = viewPaintTool(plugin);
        if (!item.hasItemMeta()) return false;
        ItemMeta im = item.getItemMeta();
        if (im == null) return false;
        if (!im.hasDisplayName()) return false;
        if (!item.getType().equals(vpt.getType())) return false;
        return vpt.getItemMeta().getDisplayName().equals(im.getDisplayName());
    }

    @Deprecated
    @EventHandler
    private void onPlayerMove(PlayerMoveEvent e) throws IOException {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective o = sb.registerNewObjective("look_at_info", "dummy");
        o.setDisplaySlot(DisplaySlot.SIDEBAR);
        YamlDocument lookAtConfig = plugin.getLookAtConfig();
        String title = ChatColor.translateAlternateColorCodes('&', lookAtConfig.getString("scoreboard.title"));
        o.setDisplayName(title);
        if (!isActionItem(item, plugin) || !getActionUUID(item).contains("#lookat")) {
            player.setScoreboard(sb);
            return;
        }
        int range = lookAtConfig.getInt("range");
        int maxRowLength = lookAtConfig.getInt("scoreboard.max_row_length");
        Block targetBlock = player.getTargetBlock(null, range);
        if (targetBlock.getType().equals(Material.AIR)) {
            player.setScoreboard(sb);
            return;
        }

        Location location = targetBlock.getLocation();
        String strLocation = getStringLocation(location);


        String inMarketBlocks = lookAtConfig.getString(String.join(".", "market_blocks", strLocation, "view"));
        String inDefaultBlocks = lookAtConfig.getString(String.join(".", "default_blocks", targetBlock.getType().toString()));
        String nullText = getMsg("lookat.no_block_view", plugin.getTextConfig());
        String output = ofNullable(ofNullable(inMarketBlocks).orElse(inDefaultBlocks)).orElse(nullText);
        String material = lookAtConfig.getString(String.join(".", "market_blocks", strLocation, "material"));

        if (inMarketBlocks != null && !material.equals(targetBlock.getType().toString())) {
            lookAtConfig.set(String.join(".", "market_blocks", strLocation), null);
            lookAtConfig.save();
            player.setScoreboard(sb);
            return;
        }

        List<String> rows = getRowsList(output, maxRowLength);
        Collections.reverse(rows);
        for (int i = 0; i < rows.size(); i++) {
            String current = rows.get(i);
            o.getScore(ChatColor.RED + "" + i + " " + ChatColor.RESET + current).setScore(i);
        }

        player.setScoreboard(sb);
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
        String strLocation = getStringLocation(e.getBlock().getLocation());
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
        sendActionBarMsg(player, getMsg("lookat.block_view_add", plugin.getTextConfig()));
    }

    @EventHandler
    private void onPlayerInteractBlock(PlayerInteractEvent e) throws IOException {
        Player player = e.getPlayer();
        if (!player.getGameMode().equals(GameMode.CREATIVE)) return;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (!isViewPaintTool(itemStack)) return;
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (e.getClickedBlock() == null) return;
        String strLocation = getStringLocation(e.getClickedBlock().getLocation());
        YamlDocument lookAtCfg = plugin.getLookAtConfig();
        lookAtCfg.set(String.join(".", "market_blocks", strLocation), null);
        lookAtCfg.save();
        sendActionBarMsg(player, getMsg("lookat.block_view_remove", plugin.getTextConfig()));
    }

    @EventHandler
    private void onVehicleEnter(VehicleEnterEvent e) {
        if (!(e.getEntered() instanceof Player)) return;
        Player player = (Player) e.getEntered();
        ItemStack item = player.getInventory().getItemInMainHand();
        String actionUUID = getActionUUID(item);
        if (!isActionItem(item, plugin) || actionUUID != null && !actionUUID.contains("#lookat")) return;
        e.setCancelled(true);
    }

}


