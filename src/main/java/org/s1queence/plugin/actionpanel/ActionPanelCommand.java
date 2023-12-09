package org.s1queence.plugin.actionpanel;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;

import static org.s1queence.plugin.utils.TextUtils.getTextFromCfg;

public class ActionPanelCommand implements CommandExecutor {
    private final RPWorldInteractions plugin;
    public ActionPanelCommand(RPWorldInteractions plugin) {this.plugin = plugin;}
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length != 0) return false;
        if (!(sender instanceof Player)) return true;
        if (!plugin.isPanelCommandEnable()) {
            sender.sendMessage(getTextFromCfg("command_disabled", plugin.getTextConfig()));
            return true;
        }

        RPActionPanel rpAP = new RPActionPanel((Player)sender, plugin);
        ((Player)sender).openInventory(rpAP.getInventory());
        return true;
    }
}
