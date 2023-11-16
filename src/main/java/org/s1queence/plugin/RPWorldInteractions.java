package org.s1queence.plugin;

import dev.geco.gsit.api.GSitAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.s1queence.plugin.actionpanel.listeners.ActionUseListener;
import org.s1queence.plugin.utils.BarrierClickListener;
import org.s1queence.plugin.actionpanel.ActionPanelCommand;
import org.s1queence.plugin.actionpanel.RPActionPanel;
import org.s1queence.plugin.actionpanel.listeners.PlayerRespawnListener;
import org.s1queence.plugin.actionpanel.listeners.PreventDefaultForActionItems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPWorldInteractions extends JavaPlugin {
    private RPActionPanel rp_action_panel;
    private final Map<Player, Player> playersInRPAction = new HashMap<>();

    public static final Float PLAYER_RP_ACTION_SPEED = 0.040000005F;

    private final Map<Player, Player> itemActionCoolDown = new HashMap<>();

    private List<String> info_lore;
    public void onEnable() {
        getServer().getLogger().info("RPWorldInteractions is enabled!");
        saveDefaultConfig();
        saveConfig();
        String title = getConfig().getString("rp_inv.title");
        rp_action_panel = new RPActionPanel(title == null ? "Действия" : title, getConfig().getConfigurationSection("rp_inv.items").getValues(true));
        info_lore = getConfig().getStringList("rp_inv.info_lore");

        getServer().getPluginManager().registerEvents(new ActionUseListener(this), this);
        getServer().getPluginManager().registerEvents(new PreventDefaultForActionItems(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new BarrierClickListener(), this);
        getServer().getPluginCommand("actionpanel").setExecutor(new ActionPanelCommand(this));
    }

    public List<String> getInfoLore() {return info_lore;}
    public RPActionPanel getRPActionPanel() {return rp_action_panel;}

    public Map<Player, Player> getPlayersInAction() {
        return playersInRPAction;
    }
    public boolean isPlayerInAction(Player player) {
        return getPlayersInAction().containsKey(player) || getPlayersInAction().containsValue(player);
    }

    public Map<Player, Player> getItemActionCoolDown() {
        return itemActionCoolDown;
    }

    public boolean isActionCoolDownExpired(Player p) {
        return !itemActionCoolDown.containsKey(p) && !itemActionCoolDown.containsValue(p);
    }

    public boolean isLaying(Player player) {
        return GSitAPI.isPosing(player) || GSitAPI.isCrawling(player);
    }

}
