package hcDiscordBot;

import java.text.SimpleDateFormat;
import java.util.*;

import hcDiscordBot.manager.AccountManager;
import hcDiscordBot.manager.ImageManager;
import hcDiscordBot.manager.SubAccountManager;

import bandMaster.BandMaster;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import com.hancho.hguild.HGuild.HGuild;
import hFriend.HFriend;
import hancho.todayDB.TodayDB;
import hcDiscordBot.command.commands.RebootCommand;
import hcDiscordBot.command.commands.SeeMoneyCommand;
import hcDiscordBot.command.commands.ServerStatusCommand;
import hcDiscordBot.listeners.ChatEvent;
import hcDiscordBot.listeners.EventListener;
import hcDiscordBot.manager.*;
import me.onebone.economyapi.EconomyAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

@SuppressWarnings({"unchecked", "unused"})
public class HcDiscordBot extends PluginBase {
	public static final String PREFIX = "§l§f[ §b! §f] ";
	public static HcDiscordBot INSTANCE;

	public boolean isJDALoaded = false;

	public String today;
	public long todayTimestamp;

	public LinkedHashMap<String, Object> serverData;
	public LinkedHashMap<String, Object> linkAccountData;

	public EconomyAPI economyAPI;
	public BandMaster bandMaster;
	public TodayDB todayDB;
	public HFriend hFriend;
	public HGuild hGuild;

	private SubAccountManager subAccountManager;
	private ImageManager imageManager;
	private AccountManager accountManager;
	private CommandManager commandManager;
	private JDAManager jdaManager;

	private EventListener eventListener;

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");

	@Override
	public void onEnable() {
		if(INSTANCE == null) INSTANCE = this;
		saveDefaultConfig();

		this.today = sdf.format(System.currentTimeMillis());
		this.todayTimestamp = System.currentTimeMillis();

		Config serverDataConfig = new Config(this.getDataFolder().getPath() + "/data.yml", Config.YAML);
		Config subAccountConfig = new Config(this.getDataFolder().getPath() + "/subAccount.yml", Config.YAML);
		Config discordAccountConfig = new Config(this.getDataFolder().getAbsolutePath() + "/discordAccount.yml", Config.YAML);
		Config imageConfig = new Config(this.getDataFolder().getAbsolutePath() + "/imageConfig.yml", Config.YAML);

		this.serverData = (LinkedHashMap<String, Object>) serverDataConfig.getAll();
		this.linkAccountData = (LinkedHashMap<String, Object>) discordAccountConfig.getAll();
		LinkedHashMap<String, LinkedHashMap<String, ArrayList<String>>> subAccountData = new LinkedHashMap(subAccountConfig.getAll());
		LinkedHashMap<String, String> isWarnedData = (LinkedHashMap<String, String>) serverData.getOrDefault("warnedData", new LinkedHashMap<String, String>());

		// Prepare second API plugins
		this.economyAPI = (EconomyAPI) this.getServer().getPluginManager().getPlugin("EconomyAPI");
		this.bandMaster = (BandMaster) this.getServer().getPluginManager().getPlugin("BandMaster");
		this.todayDB = (TodayDB) this.getServer().getPluginManager().getPlugin("TodayDB");
		this.hFriend = (HFriend) this.getServer().getPluginManager().getPlugin("HFriend");
		this.hGuild = (HGuild) this.getServer().getPluginManager().getPlugin("HGuild");

		// Initializing
		this.jdaManager = new JDAManager();
		this.accountManager = new AccountManager();
		this.subAccountManager = new SubAccountManager(this, subAccountData, isWarnedData);
		this.imageManager = new ImageManager((LinkedHashMap<String, Object>) imageConfig.getAll());
		this.commandManager = new CommandManager();
		this.eventListener = new EventListener();

		this.checkJDA();
		this.initCommands();

		this.scheduleTasks();
		this.getServer().getPluginManager().registerEvents(this.eventListener, this);
		this.getServer().getPluginManager().registerEvents(new ChatEvent(), this);
	}

	@Override
	public void onDisable() {
		this.save(false);
		this.jdaManager.jda.shutdownNow();
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
		Config subAccountConfig = new Config(this.getDataFolder().getPath() + "/subAccount.yml", Config.YAML);
		Config discordAccountConfig = new Config(this.getDataFolder().getAbsolutePath() + "/discordAccount.yml", Config.YAML);
		Config imageConfig = new Config(this.getDataFolder().getAbsolutePath() + "/imageConfig.yml", Config.YAML);

		this.serverData.put("warnedData", this.subAccountManager.isWarnedData);

		imageConfig.setAll(this.imageManager.data);
		imageConfig.save();

		serverDataConfig.setAll(this.serverData);
		serverDataConfig.save();

		subAccountConfig.setAll(new LinkedHashMap<>(this.subAccountManager.subAccountData));
		subAccountConfig.save();

		discordAccountConfig.save();
	}

	public AccountManager getAccountManager(){
		return this.accountManager;
	}

	public SubAccountManager getSubAccountManager() {
		return subAccountManager;
	}

	public ImageManager getImageManager() {
		return imageManager;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	public JDAManager getJdaManager() {
		return jdaManager;
	}

	public EventListener getEventListener() {
		return eventListener;
	}

	public void checkJDA(){
		if(!isJDALoaded){
			this.isJDALoaded = jdaManager.loadJDA();
		}
	}

	public void initCommands(){
		this.commandManager.addCommand(new ServerStatusCommand("서버상태"));
		this.commandManager.addCommand(new SeeMoneyCommand("돈"));
		this.commandManager.addCommand(new RebootCommand("재부팅"));
	}

	public void scheduleTasks() {
		this.getServer().getScheduler().scheduleRepeatingTask(this, new AsyncTask() {

			@Override
			public void onRun() {
				jdaManager.getJda().getPresence().setActivity(
						Activity.playing((getServer().getOnlinePlayers().size()) + "명의 🍫"));
			}

		}, 20 * 5);

		this.getServer().getScheduler().scheduleRepeatingTask(this, new AsyncTask() {
			@Override
			public void onRun() {
				checkJDA();
			}
		}, 20 * 120);

		this.getServer().getScheduler().scheduleDelayedRepeatingTask(this, new AsyncTask() {

			@Override
			public void onRun() {
				if (!today.equals(sdf.format(System.currentTimeMillis()))) {
					checkJDA();

					TextChannel tc = jdaManager.getTodayReportChannel();
					EmbedBuilder eb = new EmbedBuilder();

					StringBuilder playerListBuilder = new StringBuilder();
					StringBuilder bandContentBuilder = new StringBuilder();
					String mainContents;

					LinkedHashMap<String, Object>  data = todayDB.getData(todayTimestamp);
					HashSet<String> todayPlayers = (HashSet<String>) data.getOrDefault("dc_today_pl", new HashSet<String>());
					int todayMax = (int) data.getOrDefault("dc_today_max", 0);

					todayPlayers.forEach((player) -> playerListBuilder.append(player).append("님\n"));
					mainContents = playerListBuilder.toString();

					eb.setTitle("데일리 리포트");
					eb.addField("오늘의 접속자",
							mainContents.substring(0, Math.min(mainContents.length(), 1000)),
							false);

					bandContentBuilder
							.append(today)
							.append("데일리 리포트\n")
							.append("\n서버 개요 : http://ccc1.kro.kr:19139/server/Chocoserver#tab-online-activity-overview&calendar-tab\n\n누적 최고접속 : ")
							.append(serverData.getOrDefault("maxPlayers", 20))
							.append("\n오늘자 최고접속 : ")
							.append(todayMax)
							.append("\n오늘 판매된 아이탬 개수 : ")
							.append((int) data.getOrDefault("hshop_sell", 0))
							.append("개\n오늘 구매된 아이템 개수 : ")
							.append((int) data.getOrDefault("hshop_buy", 0))
							.append("개\n오늘 구매된 섬 및 땅 개수 : ")
							.append((int) data.getOrDefault("sololand_new_island", 0))
							.append("개")
							.append("\n오늘 채팅 횟수 : ")
							.append((int) data.getOrDefault("dc_count_chat", 0))
							.append("회")
							.append("\n오늘의 접속자(")
							.append(todayPlayers.size())
							.append(")\n\n")
							.append(mainContents);

					bandMaster.postBand(bandContentBuilder.toString(), false, true);

					try {
						if (tc != null)
							tc.sendMessage(eb.build()).queue();
					}catch (Exception ignore){

					}

					today = sdf.format(System.currentTimeMillis());
					todayTimestamp = System.currentTimeMillis();
				}
			}
		}, 20 * 20, 20 * 20);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    if(cmd.getName().equals("디스코드")) {
			if(sender instanceof Player){
				this.accountManager.showMainForm((Player) sender);
			}
	    }
	    return true;
	}
}
