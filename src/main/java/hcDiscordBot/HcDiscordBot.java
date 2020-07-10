package hcDiscordBot;

import java.text.SimpleDateFormat;
import java.util.*;

import javax.security.auth.login.LoginException;

import bandMaster.BandMaster;
import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import hancho.todayDB.TodayDB;
import hcDiscordBot.Listeners.ChatEvent;
import hcDiscordBot.Listeners.EventListeners;
import hcDiscordBot.Listeners.discordListener;
import me.onebone.economyapi.EconomyAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;

@SuppressWarnings("unchecked")
public class HcDiscordBot extends PluginBase {
	public static final String PREFIX = "§l§f[ §b! §f] ";
	public static final String BUTTON_LINKACCOUNT_TEXT = "계정 연동하기";
	public static final String guildID = "713044757172977756";
	//JDA
	public JDA jda;
	public JDABuilder jb;
	public  boolean isJDALoaded = false;
	public Guild guild;
	public TextChannel adminChannel;
	//today
	public String today;
	public long todayMillis;
	//Data
	public LinkedHashMap<String, Object> serverData;
	public LinkedHashMap<String, Object> LinkAccountData;
	//API
	public EconomyAPI economyAPI;
	public BandMaster bandMaster;
	public TodayDB todayDB;
	//Managers
	public SubAccountManager subAccountManager;
	public ImageManager imageManager;
	public AccountManager accountManager;
	public EventListeners eventListeners;

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");

	@Override
	public void onEnable() {
		this.today = sdf.format(System.currentTimeMillis());
		this.todayMillis = System.currentTimeMillis();
		saveDefaultConfig();
		Config config = getConfig();
		Config serverDataConfig = new Config(this.getDataFolder().getPath() + "/data.yml", Config.YAML);
		Config subAccountConfig = new Config(this.getDataFolder().getPath() + "/subAccount.yml", Config.YAML);
		Config discordAccountConfig = new Config(this.getDataFolder().getAbsolutePath() + "/discordAccount.yml", Config.YAML);
		Config imageConfig = new Config(this.getDataFolder().getAbsolutePath() + "/imageConfig.yml", Config.YAML);
		String token = config.getString("token");
		LinkedHashMap<String, Object> subAccountData = (LinkedHashMap<String, Object>) subAccountConfig.getAll();
		this.serverData = (LinkedHashMap<String, Object>) serverDataConfig.getAll();
		LinkedHashMap<String, String> isWarnedData = (LinkedHashMap<String, String>) serverData.getOrDefault("warnedData", new LinkedHashMap<String, String>());
		this.LinkAccountData = (LinkedHashMap<String, Object>) discordAccountConfig.getAll();
		if (token.equals("")) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		//JDA 초기화
		jb = new JDABuilder(token);
		jb.setAutoReconnect(true);
		jb.setStatus(OnlineStatus.ONLINE);
		jb.setActivity(Activity.watching("시즌3 시범운영"));
		jb.addEventListeners(new discordListener(this));
		try {
			jda = jb.build().awaitReady();
		} catch (LoginException | InterruptedException e) {
			e.printStackTrace();
			this.isJDALoaded = false;
		} finally {
			//Preparing second API plugins
			this.economyAPI = (EconomyAPI) this.getServer().getPluginManager().getPlugin("EconomyAPI");
			this.bandMaster = (BandMaster) this.getServer().getPluginManager().getPlugin("BandMaster");
			this.todayDB = (TodayDB) this.getServer().getPluginManager().getPlugin("TodayDB");

			//initializing
			this.accountManager = new AccountManager(this);
			this.eventListeners = new EventListeners(this);
			this.subAccountManager = new SubAccountManager(this,subAccountData, isWarnedData);
			this.imageManager = new ImageManager((LinkedHashMap<String, Object>) imageConfig.getAll(), this);
			this.scheduling();
			this.getServer().getPluginManager().registerEvents(eventListeners, this);
			this.getServer().getPluginManager().registerEvents(new ChatEvent(this), this);
		}
		this.isJDALoaded = true;

		this.guild = jda.getGuildById(guildID);
		this.adminChannel = jda.getTextChannelById("590963879190986752");
	}

	@Override
	public void onDisable() {
		this.save(false);
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdownNow();
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
		subAccountConfig.setAll(this.subAccountManager.subAccountData);
		subAccountConfig.save();
		discordAccountConfig.save();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
	    if(cmd.getName().equals("디코") || cmd.getName().equals("디스코드") || cmd.getName().equals("dc") || cmd.getName().equals("discord")) {
			if(sender instanceof  Player){
				this.accountManager.showMainForm((Player) sender);
			}
	    }
	    return true;
	}

	public void sendAdministratorMessage(String content) {
		if(!this.isJDALoaded){
			this.getLogger().info(content);
			return;
		}
		this.adminChannel.sendMessage(content).queue();
	}

	public void sendMessage(String textChannelId, String content){
		if(!this.isJDALoaded){
			this.getLogger().warning(content);
			return;
		}
		TextChannel tc = this.guild.getTextChannelById(textChannelId);
		this.sendMessage(this.guild, tc, content);
	}

	public void sendMessage(Guild guild, TextChannel tc, String  content){
		if(tc == null) return;
		tc.sendMessage(content).queue();
	}

	public void sendEmbedMessage(String textChannelId, MessageEmbed messageEmbed){
		if(!this.isJDALoaded) return;
		TextChannel tc = this.guild.getTextChannelById(textChannelId);
		if(tc == null) return;
		tc.sendMessage(messageEmbed).queue();
	}

	public AccountManager getAccountManager(){
		return this.accountManager;
	}

	public ArrayList<Member> getMembersByName(String name){
		name = name.trim().toLowerCase();
		ArrayList<Member> memberlist = new ArrayList<>();
		Guild guild = jda.getGuildById(guildID);
		for(Member member : guild.getMembers()){
			if(member.getUser().getName().toLowerCase().startsWith(name)){
				memberlist.add(member);
			}
		}
		return memberlist;
	}

	public void checkJDA(){
		if(!isJDALoaded){
			Config config = getConfig();
			String token = config.getString("token");
			jb = new JDABuilder(token);
			jb.setAutoReconnect(true);
			jb.setStatus(OnlineStatus.DO_NOT_DISTURB);
			jb.addEventListeners(new discordListener(this));
			try {
				jda = jb.build().awaitReady();
			} catch (InterruptedException | LoginException e) {
				e.printStackTrace();
			}
			isJDALoaded = true;
			this.guild = jda.getGuildById(guildID);
			this.adminChannel = jda.getTextChannelById("590963879190986752");
		}
	}

	public void scheduling() {
		this.getServer().getScheduler().scheduleRepeatingTask(this, new AsyncTask() {

			@Override
			public void onRun() {
				jda.getPresence().setActivity(Activity.playing((getServer().getOnlinePlayers().size()) + "명의 🍫"));
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
					TextChannel tc = guild.getTextChannelById(586795896977489920L);
					EmbedBuilder eb = new EmbedBuilder();
					StringBuilder playerlistBuilder = new StringBuilder();
					StringBuilder bandContentBuilder = new StringBuilder();
					String mainContents;
					LinkedHashMap<String, Object>  data = todayDB.getData(todayMillis);
					int todayMax = (int) data.getOrDefault("dc_today_max", 0);
					HashSet todayPlayers = (HashSet) data.getOrDefault("dc_today_pl", new HashSet<String>());

					todayPlayers.forEach((player) -> {
						playerlistBuilder.append(player + "님\n");
					});
					mainContents = playerlistBuilder.toString();

					eb.setTitle("데일리 리포트");
					eb.addField("오늘의 접속자", mainContents, false);
					if(tc != null)
						tc.sendMessage(eb.build()).queue();

					bandContentBuilder
							.append(today)
							.append("데일리 리포트(시즌3 테스트)\n")
							.append("\n서버 개요 : http://ccc1.kro.kr:19139/server/Chocoserver#tab-online-activity-overview&calendar-tab\n\n누적 최고접속(테스트 값) : ")
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
					today = sdf.format(System.currentTimeMillis());
					todayMillis = System.currentTimeMillis();
				}
			}
		}, 20 * 20, 20 * 20);
	}
}
