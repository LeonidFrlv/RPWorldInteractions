package org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;

import static org.s1queence.api.S1TextUtils.createItemFromMap;
import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;

public class ViewPaintToolCommand implements CommandExecutor {
    private final RPWorldInteractions plugin;
    public ViewPaintToolCommand(RPWorldInteractions plugin) {this.plugin = plugin;}

    public static ItemStack viewPaintTool(RPWorldInteractions plugin) {
        ItemStack vpt = createItemFromMap(plugin.getOptionsConfig().getSection("view_paint_tool").getStringRouteMappedValues(true));
        NBTItem nbtItem = new NBTItem(vpt);
        nbtItem.setBoolean("rpwi_vpt", true);
        nbtItem.applyNBT(vpt);
        return vpt;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return true;
        if (args.length != 0) return false;
        Player sender = (Player) commandSender;

        if (!sender.hasPermission("rpwi.perms.vpt")) {
            sender.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"no_permission_alert", plugin.getName()));
            return true;
        }

        commandSender.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"lookat.vpt_received", plugin.getName()));
        sender.getInventory().addItem(viewPaintTool(plugin));
        return true;
    }
}
