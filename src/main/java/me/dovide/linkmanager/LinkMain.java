package me.dovide.linkmanager;

import me.dovide.linkmanager.discord.SlashCommandListener;
import me.dovide.linkmanager.minecraft.LinkAccount;
import me.dovide.linkmanager.minecraft.RoleAssign;
import me.dovide.linkmanager.utils.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.EnumSet;
import java.util.logging.Logger;

public final class LinkMain extends JavaPlugin {

    private Config config;
    private Config linkedAccounts;
    private final Logger logger = Logger.getLogger("LinkManager");
    private JDA jda;
    private Permission perms = null;

    @Override
    public void onEnable() {

        if (getServer().getPluginManager().getPlugin("Vault") == null){
            logger.severe("Vault hasn't been found on the server. Please download Vault, then restart the server.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupPermissions();


        config = createConfig("config.yml");
        linkedAccounts = createConfig("linkedaccounts.yml");


        jda = JDABuilder.createLight(config.getString("bot.token"),
                        EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.DIRECT_MESSAGES))
                .addEventListeners(new SlashCommandListener(this))
                .build();

        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                Commands.slash("link", "Links the discord account to a minecraft one")
                        .addOption(OptionType.STRING, "code", "Code given ingame", true)
                        .setGuildOnly(true)
        ).queue();

        getCommand("link").setExecutor(new LinkAccount(this));
        //getCommand("linkadmin").setExecutor(new AdminCommand(this)); TODO: Create an admin command for management
        getCommand("roleassign").setExecutor(new RoleAssign(this));

    }

    @Override
    public void onDisable(){
        logger.info("Shutting Down the bot");

        if (jda != null) {
            try {
                jda.shutdown();
                jda.awaitShutdown(Duration.ofSeconds(10));
                logger.info("Bot Shut Down");
            } catch (IllegalStateException | InterruptedException err) {
                logger.severe("Failed to shut down JDA.");
                err.printStackTrace();
            } catch (NoClassDefFoundError ignore) {}
        }

        logger.info("Plugin Disabled");
    }

    public Config createConfig(String name) {
        File fc = new File(getDataFolder(), name);
        if (!fc.exists()) {
            fc.getParentFile().mkdir();
            saveResource(name, false);
        }
        Config config = new Config();
        try {
            config.load(fc);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        return config;
    }

    public void saveLinkedAccounts() {
        File fc = new File(getDataFolder(), "linkedaccounts.yml");
        try {
            linkedAccounts.save(fc);
        }catch (IOException err){
            throw new RuntimeException(err);
        }
    }

    @Override
    public Config getConfig(){
        return config;
    }

    public Config getLinkedAccounts(){
        return linkedAccounts;
    }

    public Permission getPermissions() {
        return perms;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public void updateConfig(){
        config = createConfig("config.yml");
    }

    public void updateLinked(){
        linkedAccounts = createConfig("linkedaccounts.yml");
    }

    public JDA getJda(){
        return jda;
    }
}
