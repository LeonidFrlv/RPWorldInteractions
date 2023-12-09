package org.s1queence.plugin;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.geco.gsit.api.GSitAPI;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.s1queence.plugin.actionpanel.RPActionPanel;
import org.s1queence.plugin.actionpanel.listeners.actions.ActionUseListener;
import org.s1queence.plugin.actionpanel.listeners.actions.lookat.LookAtListener;
import org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands.ViewCommand;
import org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands.ViewPaintToolCommand;
import org.s1queence.plugin.actionpanel.listeners.actions.rummage.RummageCommand;
import org.s1queence.plugin.commands.RPWICommand;
import org.s1queence.plugin.utils.BarrierClickListener;
import org.s1queence.plugin.actionpanel.ActionPanelCommand;
import org.s1queence.plugin.actionpanel.listeners.PlayerSpawnListener;
import org.s1queence.plugin.actionpanel.listeners.PreventDefaultForActionItems;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.s1queence.plugin.utils.TextUtils.getTextFromCfg;

public class RPWorldInteractions extends JavaPlugin implements Listener {
    private final Map<String, RPActionPanel> playersAndPanels = new HashMap<>();
    public static final String PLUGIN_TITLE = "[" + ChatColor.GOLD + "RPWorldInteractions" + ChatColor.WHITE + "]";
    private List<String> item_usage;
    private YamlDocument actionInventoryConfig;
    private YamlDocument textConfig;
    private YamlDocument optionsConfig;
    private YamlDocument lookAtConfig;
    private boolean open_sound;
    private boolean command_enable;
    private boolean sit_sound;
    private boolean lay_sound;
    private boolean crawl_sound;
    private boolean select_actionItem_sound;
    private boolean lookat_sound;
    private boolean rummage_command;

    public void onEnable() {
        try {
            File serverAICFile = new File(getDataFolder(), "action_inventory.yml");
            actionInventoryConfig = serverAICFile.exists() ? YamlDocument.create(serverAICFile) : YamlDocument.create(new File(getDataFolder(), "action_inventory.yml"), getResource("action_inventory.yml"));
            textConfig = YamlDocument.create(new File(getDataFolder(), "text.yml"), getResource("text.yml"));
            optionsConfig = YamlDocument.create(new File(getDataFolder(), "options.yml"), getResource("options.yml"));
            lookAtConfig = YamlDocument.create(new File(getDataFolder(), "lookat.yml"), getResource("lookat.yml"));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        log(getTextFromCfg("onEnable_msg", textConfig));

        item_usage = actionInventoryConfig.getStringList("action_inv.item_usage");

        command_enable = actionInventoryConfig.getBoolean("action_inv.command_enable");
        open_sound = actionInventoryConfig.getBoolean("action_inv.open_sound");
        sit_sound = optionsConfig.getBoolean("sounds.sit");
        lay_sound = optionsConfig.getBoolean("sounds.lay");
        crawl_sound = optionsConfig.getBoolean("sounds.crawl");
        select_actionItem_sound = optionsConfig.getBoolean("sounds.select_actionItem");
        lookat_sound = optionsConfig.getBoolean("sounds.lookat_sound");
        rummage_command = optionsConfig.getBoolean("rummage.command");

        getServer().getPluginManager().registerEvents(new ActionUseListener(this), this);
        getServer().getPluginManager().registerEvents(new PreventDefaultForActionItems(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new BarrierClickListener(this), this);
        getServer().getPluginManager().registerEvents(new LookAtListener(this), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginCommand("actionpanel").setExecutor(new ActionPanelCommand(this));
        getServer().getPluginCommand("rpworldinteractions").setExecutor(new RPWICommand(this));
        getServer().getPluginCommand("vpt").setExecutor(new ViewPaintToolCommand(this));
        getServer().getPluginCommand("view").setExecutor(new ViewCommand(this));
        getServer().getPluginCommand("rummage").setExecutor(new RummageCommand(this));
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        String uuid = player.getUniqueId().toString();
        playersAndPanels.put(uuid, new RPActionPanel(player, this));
        player.setResourcePack(getOptionsConfig().getString("resource_pack"));
    }

    public void onDisable() {
        log(getTextFromCfg("onDisable_msg", textConfig));
    }

    public void log(String msgToConsoleLog) {
        getServer().getConsoleSender().sendMessage(msgToConsoleLog);
    }

    public List<String> getItemUsage() {return item_usage;}
    public void setItemUsage(List<String> newState) {item_usage = newState;}

    public Map<String, RPActionPanel> getPlayersAndPanels() {
        return playersAndPanels;
    }

    public boolean isPanelCommandEnable() {
        return command_enable;
    }
    public boolean isOpenSound() {
        return open_sound;
    }

    public boolean isRummageCommand() {return rummage_command;}
    public void setIsRummageCommand(boolean newState) {rummage_command = newState;}

    public boolean isSelectActionItemSound() {return select_actionItem_sound;}
    public void setIsSelectActionItemSound(boolean newState) {select_actionItem_sound = newState;}
    public boolean isLookAtSound() {return lookat_sound;}
    public void setIsLookAtSound(boolean newState) {lookat_sound = newState;}
    public boolean isSitSound() {return sit_sound;}
    public void setIsSitSound(boolean newState) {sit_sound = newState;}
    public boolean isLaySound() {return lay_sound;}
    public void setIsLaySound(boolean newState) {lay_sound = newState;}
    public boolean isCrawlSound() {return crawl_sound;}
    public void setIsCrawlSound(boolean newState) {crawl_sound = newState;}

    public void setIsPanelCommandEnable(boolean newState) {
        command_enable = newState;
    }
    public void setIsOpenSound(boolean newState) {
        open_sound = newState;
    }

    public boolean isLaying(Player player) {
        return GSitAPI.isPosing(player) || GSitAPI.isCrawling(player);
    }

    public YamlDocument getActionInventoryConfig() {return actionInventoryConfig;}
    public void setActionInventoryConfig(YamlDocument newState) {actionInventoryConfig = newState;}
    public YamlDocument getOptionsConfig() {return optionsConfig;}
    public void setOptionsConfig(YamlDocument newState) {optionsConfig  = newState;}
    public YamlDocument getLookAtConfig() {return lookAtConfig;}
    public void setLookAtConfig(YamlDocument newState) {lookAtConfig  = newState;}
    public YamlDocument getTextConfig() {return textConfig;}
    public void setTextConfig(YamlDocument newState) {textConfig = newState;}
}
