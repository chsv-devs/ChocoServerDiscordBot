package hcDiscordBot.Listeners;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.scheduler.AsyncTask;
import hcDiscordBot.HcDiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

public class EventListeners implements Listener{
	HcDiscordBot main;
	
	public EventListeners(HcDiscordBot owner) {
		this.main = owner;
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent ev) {
		if (main.todayPlayer.containsKey(ev.getPlayer().getName()) != true) {
			main.todayPlayer.put(ev.getPlayer().getName(), true);
		}
		int playersSize = main.getServer().getOnlinePlayers().size();
		if ((int) main.todayData.getOrDefault("maxPlayers", 0) < playersSize) {
			main.todayData.put("maxPlayers", playersSize);
		}
		if ((int) main.data.getOrDefault("maxPlayes", 20) < playersSize) {
			main.data.put("maxPlayers", playersSize);
			TextChannel tc = main.jda.getGuildById(508167852042485760L).getTextChannelById(586795896977489920L);
			EmbedBuilder embed = new EmbedBuilder();
			embed.setTitle("최고동접 " + playersSize + "명 감사합니다.");
			StringBuilder sb = new StringBuilder();
			main.getServer().getOnlinePlayers().forEach((uuid, player) -> {
				sb.append(player.getName() + "님, ");
			});
			embed.addField("접속해 주신 분들", sb.toString(), false);
			tc.sendMessage(embed.build()).queue();
		}
		main.getServer().getScheduler().scheduleAsyncTask(main, new AsyncTask() {

			@Override
			public void onRun() {
				main.checkSubAccount(ev.getPlayer());
			}
		});
	}

}
