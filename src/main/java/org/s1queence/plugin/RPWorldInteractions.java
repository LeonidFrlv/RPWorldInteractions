package org.s1queence.plugin;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.geco.gsit.api.GSitAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.s1queence.plugin.actionpanel.listeners.ActionUseListener;
import org.s1queence.plugin.commands.RPWICommand;
import org.s1queence.plugin.utils.BarrierClickListener;
import org.s1queence.plugin.actionpanel.ActionPanelCommand;
import org.s1queence.plugin.actionpanel.RPActionPanel;
import org.s1queence.plugin.actionpanel.listeners.PlayerSpawnListener;
import org.s1queence.plugin.actionpanel.listeners.PreventDefaultForActionItems;
import org.s1queence.plugin.utils.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RPWorldInteractions extends JavaPlugin {
    private RPActionPanel rp_action_panel;
    private final Map<Player, Player> playersInRPAction = new HashMap<>();
    public static final Float PLAYER_RP_ACTION_SPEED = 0.040010135F;
    public static final String PLUGIN_TITLE = "[" + ChatColor.GOLD + "RPWorldInteractions" + ChatColor.WHITE + "]";
    private final Map<Player, Player> itemActionCoolDown = new HashMap<>();
    private List<String> item_usage;
    private YamlDocument actionInventoryConfig;
    private YamlDocument textConfig;
    private boolean command_enable;

    public void onEnable() {
        try {
            actionInventoryConfig = YamlDocument.create(new File(getDataFolder(), "action_inventory.yml"), getResource("action_inventory.yml"));
            textConfig = YamlDocument.create(new File(getDataFolder(), "text.yml"), getResource("text.yml"));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        log(TextUtils.getMsg("onEnable_msg", this));

        String title = actionInventoryConfig.getString("action_inv.title");
        rp_action_panel = new RPActionPanel(title == null ? "Действия" : title, actionInventoryConfig.getSection("action_inv.actions").getStringRouteMappedValues(true), this);
        item_usage = actionInventoryConfig.getStringList("action_inv.item_usage");
        command_enable = actionInventoryConfig.getBoolean("action_inv.command_enable");

        getServer().getPluginManager().registerEvents(new ActionUseListener(this), this);
        getServer().getPluginManager().registerEvents(new PreventDefaultForActionItems(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new BarrierClickListener(this), this);
        getServer().getPluginCommand("actionpanel").setExecutor(new ActionPanelCommand(this));
        getServer().getPluginCommand("rpworldinteractions").setExecutor(new RPWICommand(this));
    }

    public void onDisable() {
        log(TextUtils.getMsg("onDisable_msg", this));
    }

    public void log(String msgToConsoleLog) {
        getServer().getConsoleSender().sendMessage(msgToConsoleLog);
    }

    public List<String> getItemUsage() {return item_usage;}
    public void setItemUsage(List<String> newState) {item_usage = newState;}
    public RPActionPanel getRPActionPanel() {return rp_action_panel;}
    public void setRPActionPanel(RPActionPanel newState) {rp_action_panel = newState;}

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
    public boolean isPanelCommandEnable() {
        return command_enable;
    }

    public void setIsPanelCommandEnable(boolean newState) {
        command_enable = newState;
    }

    public boolean isLaying(Player player) {
        return GSitAPI.isPosing(player) || GSitAPI.isCrawling(player);
    }

    public YamlDocument getActionInventoryConfig() {return actionInventoryConfig;}
    public void setActionInventoryConfig(YamlDocument newState) {textConfig = newState;}
    public YamlDocument getTextConfig() {return textConfig;}
    public void setTextConfig(YamlDocument newState) {textConfig = newState;}
}
