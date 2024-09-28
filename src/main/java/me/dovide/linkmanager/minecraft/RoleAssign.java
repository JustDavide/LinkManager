package me.dovide.linkmanager.minecraft;

import me.dovide.linkmanager.LinkMain;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RoleAssign implements CommandExecutor {

    private final LinkMain instance;

    public RoleAssign(LinkMain instance){
        this.instance = instance;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {

        // Only console - /assignrole player roleID

        if(!(commandSender instanceof ConsoleCommandSender)){
            commandSender.sendMessage("This command is CONSOLE only");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        String RoleID = args[1];
        Guild guild = instance.getJda().getGuildById(instance.getConfig().getString("bot.guild_id"));

        if (instance.getLinkedAccounts().contains(target.getName())){

            Member targetMember = guild.retrieveMemberById(instance.getLinkedAccounts().getString(target.getName())).complete();
            Role targetRole = guild.getRoleById(RoleID);

            try {
                guild.addRoleToMember(targetMember, targetRole).queue();
            }catch (IllegalArgumentException err){
                err.printStackTrace();
                commandSender.sendMessage(instance.getLinkedAccounts().getString(target.getName()) + " is null as a user");
            }

            return true;
        }

        return true;
    }
}
