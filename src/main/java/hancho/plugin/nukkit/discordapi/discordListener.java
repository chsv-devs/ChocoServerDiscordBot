package hancho.plugin.nukkit.discordapi;

import java.awt.Color;
import java.util.LinkedHashMap;

import cn.nukkit.IPlayer;
import cn.nukkit.scheduler.AsyncTask;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class discordListener extends ListenerAdapter {
	discordapi plugin;
	public discordListener(discordapi plugin) {
		this.plugin = plugin;
	}
	@Override
	public void onMessageReceived(MessageReceivedEvent ev) {
		if(ev.getAuthor().isBot() == true) return;
		TextChannel tc = ev.getTextChannel();
		String msg = ev.getMessage().getContentRaw();
		if(msg.equals("^서버상태") == true) {
			plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
				@Override
				public void onRun() {
					sendStatus(tc);
				}
			});
		}
		if(msg.startsWith("^돈") == true) {
			String name = ev.getMessage().getContentRaw().substring(3);
			IPlayer player = plugin.getServer().getOfflinePlayer(name);
			if(player == null) {
				tc.sendMessage("해당 플레이어가 존재하지 않습니다.").queue();
				return;
			}
			tc.sendMessage(plugin.economyAPI.myMoney(player) + "원").queue();
		}
		if(msg.equals("^내돈")) {
			tc.sendMessage("아직 초코서버와 연동되지 않았습니다. 곧 추가될 기능입니다.").queue();;
		}
		
	}
	
	public void sendStatus(TextChannel tc) {
		LinkedHashMap<String, StringBuilder> map = new LinkedHashMap<String, StringBuilder>();
		this.plugin.getServer().getOnlinePlayers().forEach((UUID, player) -> {
			StringBuilder bd = map.getOrDefault(player.getLevel().getName(), new StringBuilder());
			bd.append(player.getName() + ",");
			map.put(player.getLevel().getName(), bd);
		});
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("현재 서버상태");
		eb.setColor(Color.ORANGE);
		eb.addField("서버 정보", "최고 동접 : " + (int) plugin.data.getOrDefault("maxPlayes", 20), false);
		eb.addBlankField(true);
		//eb.addField("현재 접속자", , true)
		map.forEach((levelName , bd) -> {
			eb.addField("월드 " + levelName, bd.toString(), false);
		});
		tc.sendMessage(eb.build()).queue();
		//tc.sendMessage("시즌3 온라인 : " + plugin.getServer().getOnlinePlayers().size() + "\n플레이어 : " + players.toString()).queue();
	}
}
