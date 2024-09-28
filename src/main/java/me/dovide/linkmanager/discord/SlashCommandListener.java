package me.dovide.linkmanager.discord;

import me.dovide.linkmanager.LinkMain;
import me.dovide.linkmanager.minecraft.LinkAccount;
import me.dovide.linkmanager.utils.Config;
import me.dovide.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.HierarchyException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.awt.*;

public class SlashCommandListener extends ListenerAdapter {

    private final LinkMain instance;
    private final Config config;
    private final Config linkedAccount;

    public SlashCommandListener(LinkMain instance) {
        this.instance = instance;
        config = instance.getConfig();
        linkedAccount = instance.getLinkedAccounts();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
        if (e.getName().equals("link")){

            Role linkedRole = e.getGuild().getRoleById(config.getString("bot.linked_role_id"));

            MessageEmbed embedAlready = new EmbedBuilder()
                    .setAuthor("LinkManager")
                    .setColor(Color.RED)
                    .setDescription("You already have the linked role. You cannot link again.")
                    .addField("", "**If this is an error contact Dovide **\n", false)
                    .build();

            if(hasVerifiedRole(e.getMember(), linkedRole.getId())){
                e.replyEmbeds(embedAlready).queue();
                return;
            }

            if(!config.getString("bot.guild_id").equals(e.getGuild().getId())){
                e.reply("You cannot use this bot in this server.").setEphemeral(true).queue();
                return;
            }

            if(!config.getString("bot.channel_id").equals(e.getChannelId())){
                e.reply("You cannot use this command in this channel.").setEphemeral(true).queue();
                return;
            }

            String content = e.getOption("code", OptionMapping::getAsString);

            if (content.length() != 4) {
                e.reply("The code has to be 4 characters").setEphemeral(true).queue();
                return;
            }

            Player player = LinkAccount.getActiveCodesFromCode().get(content);

            if (player == null){
                e.reply("This code does not exists or is expired.").setEphemeral(true).queue();
                return;
            }

            MessageEmbed embedSuccess = new EmbedBuilder()
                    .setAuthor("LinkManager")
                    .setColor(Color.GREEN)
                    .setDescription("You have successfully linked your discord account to your minecraft account: __" + player.getName() + "__")
                    .addField("", "**Your rewards are waiting ingame**\n" + e.getMember().getAsMention(), false)
                    .build();

            e.replyEmbeds(embedSuccess).queue();

            linkedAccount.set(player.getName(), e.getMember().getId());
            instance.saveLinkedAccounts();
            LinkAccount.getActiveCodesFromCode().remove(content);
            LinkAccount.getActiveCodesFromPlayer().remove(player);

            if (config.contains("discord." + instance.getPermissions().getPrimaryGroup(player))){
                Role foundRole = e.getGuild().getRoleById(config.getString("discord." + instance.getPermissions().getPrimaryGroup(player)));
                e.getGuild().addRoleToMember(e.getMember(), foundRole).queue();

                player.sendMessage(Util.cc("&9LinkManager &7Your &b" + instance.getPermissions().getPrimaryGroup(player)  + " &7rank got automatically linked to your discord account."));
            }

            e.getGuild().addRoleToMember(e.getMember(), linkedRole).queue();
            try {
                e.getMember().modifyNickname("(✓) | " + player.getName()).queue();
            }catch (HierarchyException err){
                player.sendMessage(Util.cc("&cLinkManager &7You got linked but your role is higher than the bot's. The nickname has not been changed"));
            }

            Bukkit.getScheduler().runTask(instance, () -> {
                for (String command : config.getStringList("rewards")) {
                    // Dispatch the command with the player's name
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName()));
                }
            });


        }
    }

    private boolean hasVerifiedRole(Member member, String IDToFind){
        for(Role role : member.getRoles()){
            return role.getId().equals(IDToFind);
        }
        return false;
    }

    
}
