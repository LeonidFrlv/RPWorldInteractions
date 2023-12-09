package org.s1queence.plugin.actionpanel.listeners.actions.rummage;

import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.s1queence.api.countdown.progressbar.ProgressBar;
import org.s1queence.plugin.RPWorldInteractions;

import static org.s1queence.S1queenceLib.getLib;
import static org.s1queence.api.S1Booleans.isAllowableInteraction;
import static org.s1queence.api.S1Utils.sendActionBarMsg;
import static org.s1queence.plugin.utils.TextUtils.getTextFromCfg;

public class RummageCommand implements CommandExecutor {
    private final RPWorldInteractions plugin;
    public RummageCommand(RPWorldInteractions plugin) {this.plugin = plugin;}

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return true;
        Player sender = (Player) commandSender;
        if (!plugin.isRummageCommand()) {
            sender.sendMessage(getTextFromCfg("command_disabled", plugin.getTextConfig()));
            return true;
        }
        if (args.length != 1) return false;
        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null || sender.equals(target)) {
            sender.sendMessage(getTextFromCfg("player_not_found", plugin.getTextConfig()));
            return true;
        }

        String errorText = isAllowableInteraction(sender, target.getLocation(), getLib());
        if (errorText != null) {
            sendActionBarMsg(sender, errorText);
            return true;
        }

        YamlDocument cfg = plugin.getTextConfig();
        YamlDocument optionsCfg = plugin.getOptionsConfig();

        new Rummage(
                sender,
                target,
                optionsCfg.getInt("rummage.seconds"),
                true,
                new ProgressBar(
                        0,
                        1,
                        optionsCfg.getInt("progress_bar.max_bars"),
                        optionsCfg.getString("progress_bar.symbol"),
                        ChatColor.translateAlternateColorCodes('&', optionsCfg.getString("progress_bar.border_left")),
                        ChatColor.translateAlternateColorCodes('&', optionsCfg.getString("progress_bar.border_right")),
                        ChatColor.getByChar(optionsCfg.getString("progress_bar.color")),
                        ChatColor.getByChar(optionsCfg.getString("progress_bar.complete_color")),
                        ChatColor.getByChar(optionsCfg.getString("progress_bar.percent_color"))
                ),
                plugin,
                plugin,
                getTextFromCfg("rummage_action.preprocess.every_tick.action_bar_both", cfg),
                getTextFromCfg("rummage_action.preprocess.every_tick.player.title", cfg),
                getTextFromCfg("rummage_action.preprocess.every_tick.player.subtitle", cfg),
                getTextFromCfg("rummage_action.preprocess.every_tick.target.title", cfg),
                getTextFromCfg("rummage_action.preprocess.every_tick.target.subtitle", cfg),
                getTextFromCfg("rummage_action.preprocess.complete.action_bar_both", cfg),
                getTextFromCfg("rummage_action.preprocess.complete.player.title", cfg),
                getTextFromCfg("rummage_action.preprocess.complete.player.subtitle", cfg),
                getTextFromCfg("rummage_action.preprocess.complete.target.title", cfg),
                getTextFromCfg("rummage_action.preprocess.complete.target.subtitle", cfg),
                getTextFromCfg("rummage_action.preprocess.cancel.action_bar_both", cfg),
                getTextFromCfg("rummage_action.preprocess.cancel.player.title", cfg),
                getTextFromCfg("rummage_action.preprocess.cancel.player.subtitle", cfg),
                getTextFromCfg("rummage_action.preprocess.cancel.target.title", cfg),
                getTextFromCfg("rummage_action.preprocess.cancel.target.subtitle", cfg)
        );

        return true;
    }
}
