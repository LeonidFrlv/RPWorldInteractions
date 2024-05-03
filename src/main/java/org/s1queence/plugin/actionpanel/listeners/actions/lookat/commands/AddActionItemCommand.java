package org.s1queence.plugin.actionpanel.listeners.actions.lookat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.plugin.RPWorldInteractions;

import static org.s1queence.api.S1TextUtils.getConvertedTextFromConfig;
import static org.s1queence.api.S1TextUtils.getTextWithInsertedPlayerName;
import static org.s1queence.plugin.actionpanel.utils.ActionPanelUtil.addDefaultActionItem;

public class AddActionItemCommand implements CommandExecutor {
    private final RPWorldInteractions plugin;
    public AddActionItemCommand(RPWorldInteractions pl) {this.plugin = pl;}

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player target = plugin.getServer().getPlayer(args.length == 0 ? sender.getName() : args[0]);
        if (target == null) {
            sender.sendMessage(getConvertedTextFromConfig(plugin.getTextConfig(),"player_not_found", plugin.getName()));
            return true;
        }
        
        addDefaultActionItem(target, plugin);
        sender.sendMessage(getTextWithInsertedPlayerName(getConvertedTextFromConfig(plugin.getTextConfig(),"on_item_give", plugin.getName()), target.getName()));
        return true;
    }
}
