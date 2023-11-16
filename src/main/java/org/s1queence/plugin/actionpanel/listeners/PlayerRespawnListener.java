package org.s1queence.plugin.actionpanel.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.utils.ActionPanelUtil;

public class PlayerRespawnListener implements Listener {

    private final RPWorldInteractions plugin;
    public PlayerRespawnListener(RPWorldInteractions plugin) {this.plugin = plugin;}

    @EventHandler
    private void onPlayerSpawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        ItemStack cloned = plugin.getRPActionPanel().getActionsList().get(0).clone();
        ActionPanelUtil.insertLoreBeforeEnd(cloned, plugin.getInfoLore());
        player.getInventory().setItem(8, cloned);
        if (player.getWalkSpeed() == RPWorldInteractions.PLAYER_RP_ACTION_SPEED) player.setWalkSpeed(0.2f);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        ItemStack eightItem = player.getInventory().getItem(8);
        if (player.getWalkSpeed() == RPWorldInteractions.PLAYER_RP_ACTION_SPEED) player.setWalkSpeed(0.2f);
        if (eightItem != null && ActionPanelUtil.isActionItem(eightItem, plugin)) return;
        ItemStack cloned = plugin.getRPActionPanel().getActionsList().get(0).clone();
        ActionPanelUtil.insertLoreBeforeEnd(cloned, plugin.getInfoLore());
        player.getInventory().setItem(8, cloned);
    }
}
