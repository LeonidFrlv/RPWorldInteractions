package org.s1queence.plugin.actionpanel;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;

public class ActionPanelCommand implements CommandExecutor {
    private final RPWorldInteractions plugin;
    public ActionPanelCommand(RPWorldInteractions plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length != 0) return false;
        if (!(commandSender instanceof Player)) return true;
        Player sender = (Player) commandSender;
        sender.openInventory(plugin.getRPActionPanel().getInventory());
        return true;
    }
}
