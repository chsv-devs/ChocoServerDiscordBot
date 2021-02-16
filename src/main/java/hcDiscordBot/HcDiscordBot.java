package hcDiscordBot;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import hcDiscordBot.discord.manager.BotCommandManager;
import hcDiscordBot.discord.manager.JDAManager;
import hcDiscordBot.manager.AccountManager;
import hcDiscordBot.manager.ApiManager;
import hcDiscordBot.manager.ImageManager;
import hcDiscordBot.minecraft.SubAccountManager;
import hcDiscordBot.minecraft.listeners.MinecraftChatEventListener;
import hcDiscordBot.minecraft.listeners.MinecraftEventListener;
import lombok.Getter;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

@Getter
@Setter
@SuppressWarnings({"unchecked", "unused"})
public class HcDiscordBot extends PluginBase {
    public static final String PREFIX = "§l§f[ §b! §f] ";
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");
    public static HcDiscordBot instant;

    public LinkedHashMap<String, Object> serverData;

    private SubAccountManager subAccountManager;
    private ImageManager imageManager;
    private AccountManager accountManager;
    private BotCommandManager botCommandManager;
    private JDAManager jdaManager;
    private ApiManager apiManager;

    private MinecraftEventListener minecraftEventListener;

    @Override
    public void onEnable() {
        if (instant == null) instant = this;
        saveDefaultConfig();

        Config serverDataConfig = new Config(this.getDataFolder().getPath() + "/data.yml", Config.YAML);

        this.serverData = (LinkedHashMap<String, Object>) serverDataConfig.getAll();

        // Initialize managers
        this.imageManager = new ImageManager();
        this.accountManager = new AccountManager();
        this.subAccountManager = new SubAccountManager();
        this.botCommandManager = new BotCommandManager();
        this.minecraftEventListener = new MinecraftEventListener();
        this.apiManager = new ApiManager();
        this.jdaManager = new JDAManager();

        this.getServer().getPluginManager().registerEvents(this.minecraftEventListener, this);
        this.getServer().getPluginManager().registerEvents(new MinecraftChatEventListener(), this);
    }

    @Override
    public void onDisable() {
        this.save(false);
        this.getJdaManager().getJda().shutdownNow();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equals("이미지")) {
            if (sender instanceof Player) {
                this.getImageManager().showImgListForm((Player) sender);
            }
        }
        return true;
    }

	public void save(boolean async) {
		if (async) {
			this.getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {

				@Override
				public void onRun() {
					save(false);
				}
			});
			return;
		}

		Config serverDataConfig = new Config(this.getDataFolder().getPath() + "/data.yml", Config.YAML);

		serverDataConfig.setAll(this.serverData);
		serverDataConfig.save();

		this.getImageManager().save();
		this.getSubAccountManager().save();
		this.getAccountManager().save();
	}
}
