package org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;

import static org.s1queence.api.S1TextUtils.createItemFromMap;
import static org.s1queence.plugin.utils.TextUtils.getTextFromCfg;

public class ViewPaintToolCommand implements CommandExecutor {
    private final RPWorldInteractions plugin;
    public ViewPaintToolCommand(RPWorldInteractions plugin) {this.plugin = plugin;}

    public static ItemStack viewPaintTool(RPWorldInteractions plugin) {
        return createItemFromMap(plugin.getOptionsConfig().getSection("view_paint_tool").getStringRouteMappedValues(true));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return true;
        if (args.length != 0) return false;
        commandSender.sendMessage(getTextFromCfg("lookat.vpt_received", plugin.getTextConfig()));
        Player sender = (Player) commandSender;
        if (!sender.hasPermission("rpwi.perms.vpt")) {
            sender.sendMessage(getTextFromCfg("no_permission_alert", plugin.getTextConfig()));
            return true;
        }
        sender.getInventory().addItem(viewPaintTool(plugin));
        return true;
    }
}
