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

import static org.s1queence.api.countdown.CountDownAction.getDoubleRunnableActionHandlers;
import static org.s1queence.api.countdown.CountDownAction.getPreprocessActionHandlers;
import static org.s1queence.plugin.utils.TextUtils.getTextFromCfg;


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
            sender.sendMessage(getTextFromCfg("unknown_rpwi_command_action_alert", plugin.getTextConfig()));
            return true;
        }

        if (action.equalsIgnoreCase("reload")) {
            try {
                File textConfigFile = new File(plugin.getDataFolder(), "text.yml");
                File actionInventoryConfigFile = new File(plugin.getDataFolder(), "action_inventory.yml");
                File optionsConfig = new File(plugin.getDataFolder(), "options.yml");
                File lookatConfigFile = new File(plugin.getDataFolder(), "lookat.yml");

                if (!textConfigFile.exists()) plugin.setTextConfig(YamlDocument.create(new File(plugin.getDataFolder(), "text.yml"), plugin.getResource("text.yml")));
                if (!actionInventoryConfigFile.exists()) plugin.setActionInventoryConfig(YamlDocument.create(new File(plugin.getDataFolder(), "action_inventory.yml"), plugin.getResource("action_inventory.yml")));
                if (!optionsConfig.exists()) plugin.setOptionsConfig(YamlDocument.create(new File(plugin.getDataFolder(), "options.yml"), plugin.getResource("options.yml")));
                if (!lookatConfigFile.exists()) plugin.setLookAtConfig(YamlDocument.create(new File(plugin.getDataFolder(), "lookat.yml"), plugin.getResource("lookat.yml")));

                if (plugin.getActionInventoryConfig().hasDefaults()) plugin.getActionInventoryConfig().getDefaults().clear();
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
            plugin.setIsOpenSound(actionInventoryConfig.getBoolean("action_inv.open_sound"));

            plugin.setIsSitSound(optionsConfig.getBoolean("sounds.sit"));
            plugin.setIsLaySound(optionsConfig.getBoolean("sounds.lay"));
            plugin.setIsCrawlSound(optionsConfig.getBoolean("sounds.crawl"));
            plugin.setIsSelectActionItemSound(optionsConfig.getBoolean("sounds.select_actionItem"));
            plugin.setIsLookAtSound(optionsConfig.getBoolean("sounds.lookat_sound"));

            getPreprocessActionHandlers().clear();
            getDoubleRunnableActionHandlers().clear();
            plugin.getPlayersAndPanels().clear();

            for (Player p : plugin.getServer().getOnlinePlayers()) {
                plugin.getPlayersAndPanels().put(p.getUniqueId().toString(), new RPActionPanel(p, plugin));
            }

            String reloadMsg = getTextFromCfg("onReload_msg", plugin.getTextConfig());
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
