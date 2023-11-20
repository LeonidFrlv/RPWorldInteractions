package org.s1queence.plugin.commands;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;
import org.s1queence.plugin.actionpanel.RPActionPanel;
import org.s1queence.plugin.actionpanel.utils.ActionPanelUtil;

import java.io.File;
import java.util.Arrays;

import static org.s1queence.plugin.utils.TextUtils.getMsg;


public class RPWICommand implements CommandExecutor {
    private final RPWorldInteractions plugin;
    public RPWICommand(RPWorldInteractions plugin) {this.plugin = plugin;}

    private final String[] possiblesActions = new String[] {
        "reload"
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length != 1) return false;
        String action = args[0];

        if (!Arrays.asList(possiblesActions).contains(action.toLowerCase())) {
            sender.sendMessage(getMsg("unknown_rpwi_command_action_alert", plugin));
            return true;
        }

        if (action.equalsIgnoreCase("reload")) {
            try {
                File textConfigFile = new File(plugin.getDataFolder(), "text.yml");
                File actionInventoryConfigFile = new File(plugin.getDataFolder(), "action_inventory.yml");

                if (!textConfigFile.exists()) plugin.setTextConfig(YamlDocument.create(new File(plugin.getDataFolder(), "text.yml"), plugin.getResource("text.yml")));

                if (!actionInventoryConfigFile.exists()) plugin.setActionInventoryConfig(YamlDocument.create(new File(plugin.getDataFolder(), "action_inventory.yml"), plugin.getResource("action_inventory.yml")));

                plugin.getActionInventoryConfig().reload();
                plugin.getTextConfig().reload();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            YamlDocument actionInventoryConfig = plugin.getActionInventoryConfig();

            String title = actionInventoryConfig.getString("action_inv.title");
            plugin.setItemUsage(actionInventoryConfig.getStringList("action_inv.item_usage"));
            plugin.setRPActionPanel(new RPActionPanel(title == null ? "Действия" : title, actionInventoryConfig.getSection("action_inv.actions").getStringRouteMappedValues(true), plugin));
            plugin.setIsPanelCommandEnable(actionInventoryConfig.getBoolean("action_inv.command_enable"));

            plugin.getItemActionCoolDown().clear();
            plugin.getPlayersInAction().clear();

            String reloadMsg = getMsg("onReload_msg", plugin);
            if (sender instanceof Player) sender.sendMessage(reloadMsg);
            plugin.log(reloadMsg);

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                onlinePlayer.closeInventory();
                ActionPanelUtil.addDefaultActionItem(onlinePlayer, plugin);
            }
        }
        return true;
    }
}