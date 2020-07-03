package me.libraryaddict.disguise.commands.utils;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import me.libraryaddict.disguise.utilities.LibsPremium;
import me.libraryaddict.disguise.utilities.SkinUtils;
import me.libraryaddict.disguise.utilities.parser.DisguiseParseException;
import me.libraryaddict.disguise.utilities.translations.LibsMsg;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Arrays;

/**
 * Created by libraryaddict on 28/12/2019.
 */
public class SaveDisguiseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (sender instanceof Player && !sender.isOp() &&
                (!LibsPremium.isPremium() || LibsPremium.getPaidInformation() == LibsPremium.getPluginInformation())) {
            sender.sendMessage(ChatColor.RED + "Please purchase Lib's Disguises to enable player commands");
            return true;
        }

        if (!sender.hasPermission("libsdisguises.savedisguise")) {
            DisguiseUtilities.sendMessage(sender, LibsMsg.NO_PERM);
            return true;
        }

        if (strings.length == 0) {
            sendHelp(sender);
            return true;
        }

        strings = DisguiseUtilities.split(StringUtils.join(strings, " "));

        String name = strings[0];
        String[] args = Arrays.copyOfRange(strings, 1, strings.length);

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                DisguiseUtilities.sendMessage(sender, LibsMsg.NO_CONSOLE);
                return true;
            }

            Disguise disguise = DisguiseAPI.getDisguise((Entity) sender);

            if (disguise == null) {
                DisguiseUtilities.sendMessage(sender, LibsMsg.NOT_DISGUISED);
                return true;
            }

            String disguiseString = DisguiseAPI.parseToString(disguise);

            try {
                DisguiseAPI.addCustomDisguise(name, disguiseString);

                DisguiseUtilities.sendMessage(sender, LibsMsg.CUSTOM_DISGUISE_SAVED, name);
            }
            catch (DisguiseParseException e) {
                if (e.getMessage() != null) {
                    DisguiseUtilities.sendMessage(sender, e.getMessage());
                } else {
                    DisguiseUtilities.sendMessage(sender, LibsMsg.PARSE_CANT_LOAD);
                }
            }

            return true;
        }

        // If going to be doing a player disguise...
        if (args.length >= 2 && args[0].equalsIgnoreCase("player")) {
            int i = 2;

            for (; i < args.length; i++) {
                if (!args[i].equalsIgnoreCase("setskin"))
                    continue;

                break;
            }

            // Make array larger, and some logic incase 'setskin' was the last arg
            // Player Notch = 2 - Add 2
            // player Notch setskin = 2 - Add 1
            // player Notch setskin Notch = 2 - Add 0
            if (args.length < i + 1) {
                args = Arrays.copyOf(args, Math.max(args.length, i + 2));
                i = args.length - 2;

                args[i] = "setSkin";
                args[i + 1] = args[1];
            }

            int skinId = i + 1;

            if (!args[skinId].startsWith("{")) {
                String usable = SkinUtils.getUsableStatus();

                if (usable != null) {
                    DisguiseUtilities.sendMessage(sender, usable);
                    return true;
                }

                String[] finalArgs = args;

                SkinUtils.grabSkin(args[skinId], new SkinUtils.SkinCallback() {
                    private BukkitTask runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            DisguiseUtilities.sendMessage(sender, LibsMsg.PLEASE_WAIT);
                        }
                    }.runTaskTimer(LibsDisguises.getInstance(), 100, 100);

                    @Override
                    public void onError(LibsMsg msg, Object... args) {
                        runnable.cancel();

                        DisguiseUtilities.sendMessage(sender, msg, args);
                    }

                    @Override
                    public void onInfo(LibsMsg msg, Object... args) {
                        DisguiseUtilities.sendMessage(sender, msg, args);
                    }

                    @Override
                    public void onSuccess(WrappedGameProfile profile) {
                        runnable.cancel();

                        finalArgs[skinId] = DisguiseUtilities.getGson().toJson(profile);

                        saveDisguise(sender, name, finalArgs);
                    }
                });
            } else {
                saveDisguise(sender, name, args);
            }
        } else {
            saveDisguise(sender, name, args);
        }

        return true;
    }

    private void saveDisguise(CommandSender sender, String name, String[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = DisguiseUtilities.quote(args[i]);
        }

        String disguiseString = StringUtils.join(args, " ");

        try {
            DisguiseAPI.addCustomDisguise(name, disguiseString);
            DisguiseUtilities.sendMessage(sender, LibsMsg.CUSTOM_DISGUISE_SAVED, name);

            DisguiseUtilities.setSaveDisguiseCommandUsed();
        }
        catch (DisguiseParseException e) {
            if (e.getMessage() != null) {
                DisguiseUtilities.sendMessage(sender, e.getMessage());
            } else {
                DisguiseUtilities.sendMessage(sender, LibsMsg.PARSE_CANT_LOAD);
            }
        }
    }

    private void sendHelp(CommandSender sender) {
        DisguiseUtilities.sendMessage(sender, LibsMsg.SAVE_DISG_HELP_1);
        DisguiseUtilities.sendMessage(sender, LibsMsg.SAVE_DISG_HELP_2);
        DisguiseUtilities.sendMessage(sender, LibsMsg.SAVE_DISG_HELP_3);
        DisguiseUtilities.sendMessage(sender, LibsMsg.SAVE_DISG_HELP_4);
        DisguiseUtilities.sendMessage(sender, LibsMsg.SAVE_DISG_HELP_5);
        DisguiseUtilities.sendMessage(sender, LibsMsg.SAVE_DISG_HELP_6);
    }
}
