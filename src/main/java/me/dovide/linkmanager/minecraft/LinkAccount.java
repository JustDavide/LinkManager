package me.dovide.linkmanager.minecraft;

import me.dovide.linkmanager.LinkMain;
import me.dovide.utils.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Random;

public class LinkAccount implements CommandExecutor {
    
    private static final HashMap<Player, String> activeCodesFromPlayer = new HashMap<>();
    private static final HashMap<String, Player> activeCodesFromCode = new HashMap<>();
    private final LinkMain instance;
    
    public LinkAccount(LinkMain instance){
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)){
            commandSender.sendMessage("You cannot use this command in console");
            return true;
        }

        Player player = (Player) commandSender;

        if (instance.getLinkedAccounts().contains(player.getName())){
            player.sendMessage(Util.cc("&9LinkManager &cYou already have a linked discord account"));
            return true;
        }

        if(!activeCodesFromPlayer.containsKey(player)) {
            Random random = new Random();
            String code = String.format("%04d", random.nextInt(10000));

            player.sendMessage(Util.format("&9LinkManager &7Here is your code: &b%s", code));
            player.sendMessage(Util.cc("&7Use this code in the #link chat on discord using the /link command"));

            activeCodesFromPlayer.put(player, code);
            activeCodesFromCode.put(code, player);
            return true;
        }

        player.sendMessage(Util.format("&9LinkManager &7You already have a code active. Use this code to link your account: &b%s", activeCodesFromPlayer.get(player)));
        player.sendMessage(Util.cc("&7Use this code in the #link chat on discord using the /link command"));

        return true;
    }

    public static HashMap<Player, String> getActiveCodesFromPlayer() {
        return activeCodesFromPlayer;
    }

    public static HashMap<String, org.bukkit.entity.Player> getActiveCodesFromCode() {
        return activeCodesFromCode;
    }
}
