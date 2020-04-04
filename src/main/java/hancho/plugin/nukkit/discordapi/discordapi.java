package hancho.plugin.nukkit.discordapi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;

import javax.security.auth.login.LoginException;

import bandMaster.BandMaster;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import me.onebone.economyapi.EconomyAPI;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;

public class discordapi extends PluginBase implements Listener {
	public JDA jda;
	public JDABuilder jb;
	public EconomyAPI economyAPI;
	public Config todayConfig;
	public Config dataConfig;
	public Config subAccountConfig;
	public LinkedHashMap<String, Object> todayData;
	public LinkedHashMap<String, Object> data;
	public LinkedHashMap<String, Boolean> todayPlayer;
	public LinkedHashMap<String, Object> subAccountData;
	public String today;

	public BandMaster bandMaster;

	public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");

	@Override
	public void onEnable() {
		saveDefaultConfig();
		Config config = getConfig();
		String token = config.getString("token");
		this.dataConfig = new Config(this.getDataFolder().getPath() + "/data.yml", Config.YAML);
		this.data = (LinkedHashMap<String, Object>) this.dataConfig.getAll();
		this.subAccountConfig = new Config(this.getDataFolder().getPath() + "/subAccount.yml", Config.YAML);
		this.subAccountData = (LinkedHashMap<String, Object>) this.subAccountConfig.getAll();
		if (token.equals("")) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}
		jb = new JDABuilder(AccountType.BOT);
		jb.setAutoReconnect(true);
		jb.setStatus(OnlineStatus.ONLINE);
		jb.setToken(token);
		jb.setActivity(Activity.watching("시즌3 시범운영"));
		jb.addEventListeners(new discordListener(this));
		try {
			jda = jb.build();
		} catch (LoginException e) {
			e.printStackTrace();
		}
		this.economyAPI = (EconomyAPI) this.getServer().getPluginManager().getPlugin("EconomyAPI");
		this.bandMaster = (BandMaster) this.getServer().getPluginManager().getPlugin("BandMaster");
		this.fixToday();
		this.scheduling();
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public void onDisable() {
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		saveTodayData(false);
		save(false);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent ev) {
		if (this.todayPlayer.containsKey(ev.getPlayer()) != true) {
			this.todayPlayer.put(ev.getPlayer().getName(), true);
		}
		int playersSize = this.getServer().getOnlinePlayers().size();
		if ((int) this.todayData.getOrDefault("maxPlayers", 0) < playersSize) {
			this.todayData.put("maxPlayers", playersSize);
		}
		if ((int) this.data.getOrDefault("maxPlayes", 20) < playersSize) {
			this.data.put("maxPlayers", playersSize);
			TextChannel tc = jda.getGuildById(508167852042485760L).getTextChannelById(586795896977489920L);
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("최고동접 " + playersSize + "명 감사합니다.");
			StringBuilder sb = new StringBuilder();
			this.getServer().getOnlinePlayers().forEach((uuid, player) -> {
				sb.append(player.getName() + "님, ");
			});
			embed.addField("접속해 주신 분들", sb.toString(), false);
			tc.sendMessage(embed.build()).queue();
		}
		getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {

			@Override
			public void onRun() {
				checkSubAccount(ev.getPlayer());
			}
		});
	}

	public void onQuit(PlayerQuitEvent ev) {

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
		this.dataConfig.setAll(this.data);
		this.dataConfig.save();
		this.subAccountConfig.setAll(this.subAccountData);
		this.subAccountConfig.save();
	}

	public void saveTodayData(boolean async) {
		this.todayData.put("players", todayPlayer);
		this.todayConfig.setAll(todayData);
		this.todayConfig.save(async);
	}

	public boolean fixToday() {
		String todayString = sdf.format(new Date());
		if (this.today == null) {
			this.today = todayString;
			this.todayConfig = new Config(this.getDataFolder().getPath() + "/todays/" + todayString + ".yml",
					Config.YAML);
			this.todayData = (LinkedHashMap<String, Object>) todayConfig.getAll();
			this.todayPlayer = (LinkedHashMap<String, Boolean>) this.todayData.getOrDefault("players",
					new LinkedHashMap<String, Boolean>());
			return true;
		}
		if (!todayString.equals(this.today)) {
			this.today = todayString;
			this.saveTodayData(false);
			this.todayConfig = new Config(this.getDataFolder().getPath() + "/todays/" + todayString + ".yml",
					Config.YAML);
			this.todayData = (LinkedHashMap<String, Object>) todayConfig.getAll();
			this.todayPlayer = (LinkedHashMap<String, Boolean>) this.todayData.getOrDefault("players",
					new LinkedHashMap<String, Boolean>());
			return true;
		}
		return false;
	}

	public void checkSubAccount(Player player) {
		UUID playerUuid = player.getUniqueId();
		String playerIp = player.getAddress();
		LinkedHashMap<String, ArrayList<String>> ipMap = (LinkedHashMap<String, ArrayList<String>>) this.subAccountData
				.getOrDefault("ips_", new LinkedHashMap<String, ArrayList<String>>());
		LinkedHashMap<String, ArrayList<String>> uuidMap = (LinkedHashMap<String, ArrayList<String>>) this.subAccountData
				.getOrDefault("uuids_", new LinkedHashMap<String, ArrayList<String>>());
		LinkedHashMap<String, Object> userData = (LinkedHashMap<String, Object>) this.subAccountData
				.getOrDefault(player.getName(), new LinkedHashMap<String, Object>());
		boolean ipWarn = false;
		boolean uuidWarn = false;
		ArrayList<String> ipList = ipMap.getOrDefault(playerIp, new ArrayList<String>());
		/*
		 * Iterator<String> it = ipList.iterator(); while(it.hasNext()) { String
		 * player_name = it.next(); if(player_name.equals(player.getName()) != true) {
		 * ipWarn = true; break; } }
		 */

		ArrayList<String> idList = uuidMap.getOrDefault(playerUuid, new ArrayList<String>());
		/*
		 * Iterator<String> uuidIt = idList.iterator(); while(uuidIt.hasNext()) { String
		 * player_name = uuidIt.next(); if(player_name.equals(player.getName()) != true)
		 * { uuidWarn = true; break; } }
		 */
		if (ipList.size() > 0) {
			if (ipList.size() == 1) {
				if (ipList.get(0).equals(player.getName()) != true) {
					ipWarn = true;
				}
			} else {
				ipWarn = true;
			}
		}
		if (idList.size() > 0) {
			if (idList.size() == 1) {
				if (idList.get(0).equals(player.getName()) != true) {
					uuidWarn = true;
				}
			} else {
				uuidWarn = true;
			}
		}
		getLogger().info(ipList.toString());

		if (idList.contains(player.getName()) != true) {
			idList.add(player.getName());
		}

		if (ipList.contains(player.getName()) != true) {
			ipList.add(player.getName());
		}

		TextChannel tc = jda.getGuildById("508167852042485760").getTextChannelById("590963879190986752");
		if (uuidWarn && !(boolean) userData.getOrDefault("warn", false)) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("부계정 - uuid동일");
			eb.addField("정보", "닉네임 : " + player.getName() + "\nIP : " + playerIp + "\nUUID : " + playerUuid
					+ "\n\n동일 uuid플레이어 : " + Arrays.toString(idList.toArray()), false);
			tc.sendMessage(eb.build()).queue();
			userData.put("warn", true);
		} else if (ipWarn && !(boolean) userData.getOrDefault("warn", false)) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setTitle("부계정 - IP동일");
			eb.addField("정보", "닉네임 : " + player.getName() + "\nIP : " + playerIp + "\nUUID : " + playerUuid
					+ "\n\n동일 IP플레이어 : " + Arrays.toString(ipList.toArray()), false);
			tc.sendMessage(eb.build()).queue();
			userData.put("warn", true);
		}
		this.subAccountData.put(player.getName(), userData);
		ipMap.put(playerIp, ipList);
		uuidMap.put(playerUuid.toString(), idList);
		subAccountData.put("ips_", ipMap);
		subAccountData.put("uuids_", uuidMap);
		getLogger().info(player.getName() + "의 정보\nip : " + playerIp + "\nuuid : " + playerUuid + "\n부계 알림 여부 : "
				+ (boolean) userData.getOrDefault("warn", false));
	}

	public void scheduling() {
		this.getServer().getScheduler().scheduleRepeatingTask(this, new AsyncTask() {
			@Override
			public void onRun() {
				jda.getPresence().setActivity(Activity.playing((getServer().getOnlinePlayers().size()) + "명 온라인"));
			}
		}, 20 * 5);
		this.getServer().getScheduler().scheduleDelayedRepeatingTask(this, new AsyncTask() {

			@Override
			public void onRun() {
				if (today.equals(sdf.format(new Date())) != true) {
					TextChannel tc = jda.getGuildById(508167852042485760L).getTextChannelById(586795896977489920L);
					EmbedBuilder eb = new EmbedBuilder();
					eb.setTitle("데일리 리포트");
					StringBuilder sb = new StringBuilder();
					todayPlayer.keySet().forEach((player) -> {
						sb.append(player + "님\n");
					});
					String mainContents = sb.toString();
					bandMaster.postBand("데일리 리포트(시즌3 테스트)\n" + today
							+ "\n서버 개요 : http://ccc1.kro.kr:19139/server/Chocoserver#tab-online-activity-overview&calendar-tab\n\n누적 최고접속(테스트 값) : "
							+ data.getOrDefault("maxPlayers", 20) + "\n오늘자 최고접속 : " + todayData.get("maxPlayers") + "\n오늘의 접속자\n\n"
							+ mainContents, false, true);
					eb.addField("오늘의 접속자", mainContents, false);
					tc.sendMessage(eb.build()).queue();
					fixToday();
				}
			}
		}, 20 * 20, 20 * 20);
	}
}
