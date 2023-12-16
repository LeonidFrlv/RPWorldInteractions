package org.s1queence.plugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.RPActionPanel;
import org.s1queence.plugin.actionpanel.utils.ActionPanelUtil;
import org.s1queence.plugin.libs.YamlDocument;
import org.s1queence.plugin.spots.SpotsHandler;

import java.io.File;
import java.util.Objects;

import static org.s1queence.api.S1TextUtils.consoleLog;
import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.countdown.CountDownAction.getDoubleRunnableActionHandlers;
import static org.s1queence.api.countdown.CountDownAction.getPreprocessActionHandlers;
import static org.s1queence.plugin.actionpanel.listeners.actions.rummage.Rummage.getRummageHandlers;

public class RPWICommand implements CommandExecutor {
    private final RPWorldInteractions plugin;
    public RPWICommand(RPWorldInteractions plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length != 1) return false;
        String action = args[0];
        boolean isSenderPlayer = sender instanceof Player;


        if (isSenderPlayer && !sender.hasPermission("rpwi.perms.reload")) {
            sender.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"no_permission_alert", plugin.getName()));
            return true;
        }

        if (!action.equalsIgnoreCase("reload")) {
            sender.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"only_reload_msg", plugin.getName()));
            return true;
        }

        try {
            File textConfigFile = new File(plugin.getDataFolder(), "text.yml");
            File actionInventoryConfigFile = new File(plugin.getDataFolder(), "action_inventory.yml");
            File optionsConfig = new File(plugin.getDataFolder(), "options.yml");
            File lookatConfigFile = new File(plugin.getDataFolder(), "lookat.yml");
            File spotsConfigFile = new File(plugin.getDataFolder(), "spots.yml");

            if (!textConfigFile.exists()) plugin.setTextConfig(YamlDocument.create(new File(plugin.getDataFolder(), "text.yml"), Objects.requireNonNull(plugin.getResource("text.yml"))));
            if (!actionInventoryConfigFile.exists()) plugin.setActionInventoryConfig(YamlDocument.create(new File(plugin.getDataFolder(), "action_inventory.yml"), Objects.requireNonNull(plugin.getResource("action_inventory.yml"))));
            if (!optionsConfig.exists()) plugin.setOptionsConfig(YamlDocument.create(new File(plugin.getDataFolder(), "options.yml"), Objects.requireNonNull(plugin.getResource("options.yml"))));
            if (!lookatConfigFile.exists()) plugin.setLookAtConfig(YamlDocument.create(new File(plugin.getDataFolder(), "lookat.yml"), Objects.requireNonNull(plugin.getResource("lookat.yml"))));
            if (!spotsConfigFile.exists()) plugin.setSpotsConfig(YamlDocument.create(new File(plugin.getDataFolder(), "spots.yml"), Objects.requireNonNull(plugin.getResource("spots.yml"))));

            if (plugin.getActionInventoryConfig().hasDefaults()) Objects.requireNonNull(plugin.getActionInventoryConfig().getDefaults()).clear();
            plugin.getActionInventoryConfig().reload();
            plugin.getTextConfig().reload();
            plugin.getOptionsConfig().reload();
            plugin.getLookAtConfig().reload();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        YamlDocument actionInventoryConfig = plugin.getActionInventoryConfig();
        YamlDocument optionsConfig = plugin.getOptionsConfig();

        plugin.setItemUsage(actionInventoryConfig.getStringList("action_inv.item_usage"));
        plugin.setIsPanelCommandEnable(actionInventoryConfig.getBoolean("action_inv.command_enable"));

        plugin.setIsRummageCommand(optionsConfig.getBoolean("rummage.command"));

        getPreprocessActionHandlers().clear();
        getDoubleRunnableActionHandlers().clear();
        getRummageHandlers().clear();

        plugin.getPlayersAndPanels().clear();

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            plugin.getPlayersAndPanels().put(p.getUniqueId().toString(), new RPActionPanel(p, plugin));
        }

        SpotsHandler.fill(plugin);

        String reloadMsg = getConvertedTextFromConfig(plugin.getTextConfig(),"onReload_msg", plugin.getName());
        if (isSenderPlayer) sender.sendMessage(reloadMsg);
        consoleLog(reloadMsg, plugin);

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            onlinePlayer.closeInventory();
            ActionPanelUtil.addDefaultActionItem(onlinePlayer, plugin);
        }

        return true;
    }
}
