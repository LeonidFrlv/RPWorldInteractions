package org.s1queence.plugin;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.geco.gsit.api.GSitAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.s1queence.plugin.actionpanel.listeners.ActionUseListener;
import org.s1queence.plugin.utils.BarrierClickListener;
import org.s1queence.plugin.actionpanel.ActionPanelCommand;
import org.s1queence.plugin.actionpanel.RPActionPanel;
import org.s1queence.plugin.actionpanel.listeners.PlayerRespawnListener;
import org.s1queence.plugin.actionpanel.listeners.PreventDefaultForActionItems;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RPWorldInteractions extends JavaPlugin {
    private RPActionPanel rp_action_panel;
    private final Map<Player, Player> playersInRPAction = new HashMap<>();
    public static final Float PLAYER_RP_ACTION_SPEED = 0.040000005F;
    private final Map<Player, Player> itemActionCoolDown = new HashMap<>();
    private List<String> item_usage;
    private YamlDocument actionInventory;
    public void onEnable() {
        getServer().getLogger().info("RPWorldInteractions is enabled!");
        try {
            actionInventory = YamlDocument.create(new File(getDataFolder(), "action_inventory.yml"), getResource("action_inventory.yml"));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        String title = actionInventory.getString("action_inv.title");
        rp_action_panel = new RPActionPanel(title == null ? "Действия" : title, actionInventory.getSection("action_inv.actions").getStringRouteMappedValues(true));
        item_usage = actionInventory.getStringList("action_inv.item_usage");

        getServer().getPluginManager().registerEvents(new ActionUseListener(this), this);
        getServer().getPluginManager().registerEvents(new PreventDefaultForActionItems(this), this);
        getServer().getPluginManager().registerEvents(new PlayerRespawnListener(this), this);
        getServer().getPluginManager().registerEvents(new BarrierClickListener(), this);
        getServer().getPluginCommand("actionpanel").setExecutor(new ActionPanelCommand(this));
    }

    public List<String> getItemUsage() {return item_usage;}
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
