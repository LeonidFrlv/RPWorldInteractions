package org.s1queence.plugin.actionpanel.listeners.actions.rummage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.s1queence.api.countdown.CountDownAction;
import org.s1queence.api.countdown.progressbar.ProgressBar;
import org.s1queence.plugin.RPWorldInteractions;

import java.util.HashMap;
import java.util.Map;

import static org.s1queence.api.S1TextUtils.getTextWithInsertedPlayerName;
import static org.s1queence.api.S1Utils.sendActionBarMsg;
import static org.s1queence.plugin.utils.BarrierClickListener.empty;
import static org.s1queence.plugin.utils.TextUtils.getTextFromCfg;

public class Rummage extends CountDownAction {
    private final RPWorldInteractions rpwi;

    private static final Map<Player, Inventory> rummageHandlers = new HashMap<>();

    public static Map<Player, Inventory> getRummageHandlers() {
        return rummageHandlers;
    }

    public Rummage(
            @NotNull Player player,
            @NotNull Player target,
            int seconds,
            boolean isDoubleRunnableAction,
            @NotNull ProgressBar pb,
            @NotNull JavaPlugin plugin,
            @NotNull RPWorldInteractions rpwi,

            @NotNull String everyTickBothActionBarMsg,
            @NotNull String everyTickPlayerTitle,
            @NotNull String everyTickPlayerSubtitle,
            @NotNull String everyTickTargetTitle,
            @NotNull String everyTickTargetSubtitle,

            @NotNull String completeBothActionBarMsg,
            @NotNull String completePlayerTitle,
            @NotNull String completePlayerSubtitle,
            @NotNull String completeTargetTitle,
            @NotNull String completeTargetSubtitle,

            @NotNull String cancelBothActionBarMsg,
            @NotNull String cancelPlayerTitle,
            @NotNull String cancelPlayerSubtitle,
            @NotNull String cancelTargetTitle,
            @NotNull String cancelTargetSubtitle
    )
    {
        super(
                player,
                target,
                seconds,
                isDoubleRunnableAction,
                pb,
                plugin,
                everyTickBothActionBarMsg,
                everyTickPlayerTitle,
                everyTickPlayerSubtitle,
                everyTickTargetTitle,
                everyTickTargetSubtitle,
                completeBothActionBarMsg,
                completePlayerTitle,
                completePlayerSubtitle,
                completeTargetTitle,
                completeTargetSubtitle,
                cancelBothActionBarMsg,
                cancelPlayerTitle,
                cancelPlayerSubtitle,
                cancelTargetTitle,
                cancelTargetSubtitle
        );

        this.rpwi = rpwi;

        actionCountDown();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isActionCanceled()) {
                    cancel();
                    return;
                }

                if (isPlayerInDoubleRunnableAction(player) && isPlayerInDoubleRunnableAction(target)) {
                    start();
                    cancel();
                }

            }
        }.runTaskTimer(plugin, 0, 1);
    }
    private void start() {
        Player player = getPlayer();
        Player target = getTarget();
        String pName = player.getName();
        String tName = target.getName();
        ProgressBar progressBar = getProgressBar();

        Inventory targetInventory = target.getInventory();
        String invTitle = getTextWithInsertedPlayerName(rpwi.getOptionsConfig().getString("rummage.inv_title"), target.getName());
        Inventory newTargetInventory = Bukkit.createInventory(null, 54, invTitle);
        for (int i = 0; i < 54; i++) {
            if (i <= 40) {
                newTargetInventory.setItem(i, targetInventory.getItem(i));
                continue;
            }
            newTargetInventory.setItem(i, empty(rpwi));
        }

        player.openInventory(newTargetInventory);

        rummageHandlers.put(target, newTargetInventory);

        InventoryView targetInvView = player.getOpenInventory();

        final float ACTION_TIME = 2400.0f;
        new BukkitRunnable() {
            int time = (int)ACTION_TIME;
            @Override
            public void run() {
                if (isActionCanceled() || !player.getOpenInventory().equals(targetInvView) || time == 0 || !rummageHandlers.containsKey(target)) {
                    cancelAction(false);

                    String cancelRummagePlayerTitle = getTextWithInsertedPlayerName(getTextFromCfg("rummage_action.process.cancel.player.title", rpwi.getTextConfig()), tName);
                    String cancelRummagePlayerSubtitle = getTextWithInsertedPlayerName(getTextFromCfg("rummage_action.process.cancel.player.subtitle", rpwi.getTextConfig()), tName);
                    String cancelRummageTargetTitle = getTextWithInsertedPlayerName(getTextFromCfg("rummage_action.process.cancel.target.title", rpwi.getTextConfig()), pName);
                    String cancelRummageTargetSubtitle = getTextWithInsertedPlayerName(getTextFromCfg("rummage_action.process.cancel.target.subtitle", rpwi.getTextConfig()), pName);
                    String cancelRummageBothActionBarMsg = getTextFromCfg("rummage_action.process.cancel.action_bar_both", rpwi.getTextConfig());
                    player.sendTitle(cancelRummagePlayerTitle, cancelRummagePlayerSubtitle, 0, 75, 20);
                    target.sendTitle(cancelRummageTargetTitle, cancelRummageTargetSubtitle, 0, 75, 20);
                    sendActionBarMsg(player, cancelRummageBothActionBarMsg);
                    sendActionBarMsg(target, cancelRummageBothActionBarMsg);

                    if (target.isOnline() && !target.isDead()) updateRummageInventory(target);

                    rummageHandlers.remove(target);
                    cancel();
                    return;
                }

                target.closeInventory();
                time--;

                progressBar.setCurrent((int)ACTION_TIME - time);
                progressBar.setMax((int)ACTION_TIME);
                String stringedBar = progressBar.getProgressBar();
                String percent = progressBar.getPercent();

                String everyTickRummageTargetTitle = getTextWithInsertedProgressBar(getTextWithInsertedPlayerName(getTextFromCfg("rummage_action.process.every_tick.target.title", rpwi.getTextConfig()), pName), stringedBar, percent);
                String everyTickRummageTargetSubtitle = getTextWithInsertedProgressBar(getTextWithInsertedPlayerName(getTextFromCfg("rummage_action.process.every_tick.target.subtitle", rpwi.getTextConfig()), pName), stringedBar, percent);
                String everyTickRummageBothActionBar = getTextWithInsertedProgressBar(getTextFromCfg("rummage_action.process.every_tick.action_bar_both", rpwi.getTextConfig()), stringedBar, percent);

                target.sendTitle(everyTickRummageTargetTitle, everyTickRummageTargetSubtitle, 0, 75, 20);
                sendActionBarMsg(player, everyTickRummageBothActionBar);
                sendActionBarMsg(target, everyTickRummageBothActionBar);
            }
        }.runTaskTimer(rpwi, 0, 1);
    }

    public static void updateRummageInventory(Player target) {
        PlayerInventory mainInv = target.getInventory();
        Inventory newTargetInv = rummageHandlers.get(target);

        for (int i = 0; i <= 40; i++) {
            ItemStack currentItem = newTargetInv.getItem(i);
            if (currentItem == null) {
                mainInv.setItem(i, null);
                continue;
            }
            String mat = currentItem.getType().toString().toLowerCase();
            World world = target.getWorld();
            Location loc = target.getLocation();
            switch (i) {
                case 36: {
                    if (!mat.contains("boots")) {
                        world.dropItemNaturally(loc, currentItem);
                        newTargetInv.setItem(i, null);
                        currentItem = null;
                    }
                    break;
                }
                case 37: {
                    if (!mat.contains("leggings")) {
                        world.dropItemNaturally(loc, currentItem);
                        newTargetInv.setItem(i, null);
                        currentItem = null;
                    }
                    break;
                }
                case 38: {
                    if (!mat.contains("chestplate")) {
                        world.dropItemNaturally(loc, currentItem);
                        newTargetInv.setItem(i, null);
                        currentItem = null;
                    }
                    break;
                }
                case 39: {
                    if (!mat.contains("helmet") && !mat.equals("carved_pumpkin")) {
                        world.dropItemNaturally(loc, currentItem);
                        newTargetInv.setItem(i, null);
                        currentItem = null;
                    }
                    break;
                }
            }
            mainInv.setItem(i, currentItem);
        }
    }
}
