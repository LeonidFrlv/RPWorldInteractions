package org.s1queence.plugin;

import dev.geco.gsit.api.GSitAPI;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.s1queence.plugin.actionpanel.RPActionPanel;
import org.s1queence.plugin.actionpanel.listeners.actions.ActionUseListener;
import org.s1queence.plugin.actionpanel.listeners.actions.lookat.LookAtListener;
import org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands.ViewCommand;
import org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands.ViewPaintToolCommand;
import org.s1queence.plugin.actionpanel.listeners.actions.rummage.RummageCommand;
import org.s1queence.plugin.commands.RPWICommand;
import org.s1queence.plugin.libs.YamlDocument;
import org.s1queence.plugin.spots.SpotsCommand;
import org.s1queence.plugin.spots.SpotsHandler;
import org.s1queence.plugin.utils.BarrierClickListener;
import org.s1queence.plugin.actionpanel.ActionPanelCommand;
import org.s1queence.plugin.actionpanel.listeners.PlayerSpawnListener;
import org.s1queence.plugin.actionpanel.listeners.PreventDefaultForActionItems;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.s1queence.api.S1TextUtils.*;

public class RPWorldInteractions extends JavaPlugin {
    private final Map<String, RPActionPanel> playersAndPanels = new HashMap<>();
    private List<String> item_usage;
    private YamlDocument actionInventoryConfig;
    private YamlDocument textConfig;
    private YamlDocument optionsConfig;
    private YamlDocument lookAtConfig;
    private YamlDocument spotsConfig;
    private boolean command_enable;
    private boolean rummage_command;

    public void onEnable() {
        try {
            File serverAICFile = new File(getDataFolder(), "action_inventory.yml");
            actionInventoryConfig = serverAICFile.exists() ? YamlDocument.create(serverAICFile) : YamlDocument.create(new File(getDataFolder(), "action_inventory.yml"), Objects.requireNonNull(getResource("action_inventory.yml")));
            textConfig = YamlDocument.create(new File(getDataFolder(), "text.yml"), Objects.requireNonNull(getResource("text.yml")));
            optionsConfig = YamlDocument.create(new File(getDataFolder(), "options.yml"), Objects.requireNonNull(getResource("options.yml")));
            lookAtConfig = YamlDocument.create(new File(getDataFolder(), "lookat.yml"), Objects.requireNonNull(getResource("lookat.yml")));
            spotsConfig = YamlDocument.create(new File(getDataFolder(), "spots.yml"), Objects.requireNonNull(getResource("spots.yml")));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        consoleLog(getConvertedTextFromConfig(textConfig,"onEnable_msg", this.getName()), this);

        saveResource("permissions.txt", true);

        item_usage = actionInventoryConfig.getStringList("action_inv.item_usage");

        command_enable = actionInventoryConfig.getBoolean("action_inv.command_enable");
        rummage_command = optionsConfig.getBoolean("rummage.command");

        getServer().getPluginManager().registerEvents(new ActionUseListener(this), this);
        getServer().getPluginManager().registerEvents(new PreventDefaultForActionItems(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new BarrierClickListener(this), this);
        getServer().getPluginManager().registerEvents(new LookAtListener(this), this);
        Objects.requireNonNull(getServer().getPluginCommand("actionpanel")).setExecutor(new ActionPanelCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("rpworldinteractions")).setExecutor(new RPWICommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("vpt")).setExecutor(new ViewPaintToolCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("view")).setExecutor(new ViewCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("rummage")).setExecutor(new RummageCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("spots")).setExecutor(new SpotsCommand(this));

        SpotsHandler.fill(this);
        SpotsHandler.run(this);
    }


    public void onDisable() {
        consoleLog(getConvertedTextFromConfig(textConfig,"onDisable_msg", this.getName()), this);
    }

    public List<String> getItemUsage() {return item_usage;}
    public void setItemUsage(List<String> newState) {item_usage = newState;}

    public Map<String, RPActionPanel> getPlayersAndPanels() {
        return playersAndPanels;
    }

    public boolean isPanelCommandEnable() {
        return command_enable;
    }

    public boolean isRummageCommand() {return rummage_command;}
    public void setIsRummageCommand(boolean newState) {rummage_command = newState;}


    public void setIsPanelCommandEnable(boolean newState) {
        command_enable = newState;
    }

    public boolean isLaying(Player player) {
        return GSitAPI.isPosing(player) || GSitAPI.isCrawling(player);
    }

    public YamlDocument getActionInventoryConfig() {return actionInventoryConfig;}
    public YamlDocument getSpotsConfig() {return spotsConfig;}
    public void setSpotsConfig(YamlDocument newState) {spotsConfig = newState;}
    public void setActionInventoryConfig(YamlDocument newState) {actionInventoryConfig = newState;}
    public YamlDocument getOptionsConfig() {return optionsConfig;}
    public void setOptionsConfig(YamlDocument newState) {optionsConfig  = newState;}
    public YamlDocument getLookAtConfig() {return lookAtConfig;}
    public void setLookAtConfig(YamlDocument newState) {lookAtConfig  = newState;}
    public YamlDocument getTextConfig() {return textConfig;}
    public void setTextConfig(YamlDocument newState) {textConfig = newState;}
}
