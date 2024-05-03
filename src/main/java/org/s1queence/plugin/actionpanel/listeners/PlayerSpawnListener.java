package org.s1queence.plugin.actionpanel.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.RPActionPanel;

import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.addDefaultActionItem;
import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.isActionItem;

public class PlayerSpawnListener implements Listener {
    private final RPWorldInteractions plugin;
    public PlayerSpawnListener(RPWorldInteractions plugin) {this.plugin = plugin;}

    @EventHandler
    private void onPlayerSpawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();
        addDefaultActionItem(player, plugin);
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (!isActionItem(player.getInventory().getItem(8))) addDefaultActionItem(player, plugin);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String uuid = player.getUniqueId().toString();
        plugin.getPlayersAndPanels().put(uuid, new RPActionPanel(player, plugin));
        if (plugin.getOptionsConfig().getBoolean("set_resource_pack")) player.setResourcePack(plugin.getOptionsConfig().getString("resource_pack"));
        addDefaultActionItem(player, plugin);
    }
}
