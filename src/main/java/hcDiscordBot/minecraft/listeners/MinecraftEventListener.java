package hcDiscordBot.minecraft.listeners;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.scheduler.AsyncTask;
import hcDiscordBot.discord.textchannel.DiscordTextChannel;
import hcDiscordBot.discord.textchannel.TodayReportTextChannel;
import hcDiscordBot.manager.AccountManager;
import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.discord.manager.JDAManager;
import hcDiscordBot.manager.ApiManager;
import hcDiscordBot.minecraft.SubAccountManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static hcDiscordBot.HcDiscordBot.PREFIX;

@SuppressWarnings("unchecked")
public class MinecraftEventListener implements Listener{
	public static final int IS_YOU_MODAL_FORM = 8216;

	private final AccountManager accountManager;
	private final SubAccountManager subAccountManager;
	private final JDAManager jdaManager;
	private final HcDiscordBot plugin;

	public MinecraftEventListener() {
		this.plugin = HcDiscordBot.instant;
		this.accountManager = plugin.getAccountManager();
		this.subAccountManager = plugin.getSubAccountManager();
		this.jdaManager = plugin.getJdaManager();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent ev) {
		Player player = ev.getPlayer();
		ApiManager apiManager = this.plugin.getApiManager();

		HashSet<String> todayPlayers = (HashSet<String>) apiManager.todayDB.getHashSet("dc_today_pl");
		int todayMax = apiManager.getTodayDB().getInt("dc_today_max");

		if (!todayPlayers.contains(player.getName())) {
			todayPlayers.add(player.getName());
			apiManager.getTodayDB().put("dc_today_pl", todayPlayers);
		}

		int playersSize = plugin.getServer().getOnlinePlayers().size();
		if (todayMax < playersSize) {
			apiManager.getTodayDB().put("dc_today_max", playersSize);
		}

		if ((int) plugin.serverData.getOrDefault("maxPlayers", 20) < playersSize) {
			plugin.serverData.put("maxPlayers", playersSize);

			DiscordTextChannel tc = plugin.getJdaManager().getTextChannelManager().getTodayReportTextChannel();
			EmbedBuilder embed = new EmbedBuilder();
			StringBuilder sb = new StringBuilder();

			embed.setTitle("최고동접 " + playersSize + "명 감사합니다.");
			plugin.getServer().getOnlinePlayers().forEach((uuid, pl) -> {
				sb.append(pl.getName() + "님, ");
			});
			embed.addField("접속해 주신 분들", sb.toString(), false);

			tc.sendEmbedMessage(embed.build()).queue();
		}
		plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {

			@Override
			public void onRun() {
				subAccountManager.checkSubAccount(ev.getPlayer());
			}
		});
	}

	@EventHandler
	public void onPlayerFormResponded(PlayerFormRespondedEvent ev){
		if(ev.getWindow() == null) return;
		if(ev.getResponse() == null) return;
		Player player = ev.getPlayer();

		int id = ev.getFormID();
		if(ev.getWindow() instanceof FormWindowSimple){
			FormWindowSimple window = (FormWindowSimple) ev.getWindow();
			FormResponseSimple response = window.getResponse();

		}else if(ev.getWindow() instanceof FormWindowCustom){
			FormWindowCustom window = (FormWindowCustom) ev.getWindow();
			FormResponseCustom response = window.getResponse();


		}else if(ev.getWindow() instanceof FormWindowModal){
			FormWindowModal window = (FormWindowModal) ev.getWindow();
			FormResponseModal response = window.getResponse();

			int clickedID = response.getClickedButtonId();

		}
	}
}
