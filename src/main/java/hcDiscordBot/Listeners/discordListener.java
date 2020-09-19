package hcDiscordBot.Listeners;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.nukkit.IPlayer;
import cn.nukkit.Nukkit;
import cn.nukkit.scheduler.AsyncTask;
import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.ImageManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class discordListener extends ListenerAdapter {
	private static final String UPTIME_FORMAT = "%d일 %d시간 %d분 %d초";
	public final static String IMAGE_PATH = "/var/www/html/discordAccountSync/";
	private ImageManager imageManager;
	private final HcDiscordBot plugin;
	private final LinkedHashMap<String, String> levelNames = new LinkedHashMap<String, String>();

	public discordListener(HcDiscordBot plugin) {
		this.plugin = plugin;
		this.imageManager = plugin.imageManager;
		levelNames.put("spawn24", "스폰");
		levelNames.put("cave", "돌광산");
	}
	@Override
	public void onMessageReceived(MessageReceivedEvent ev) {
		if(ev.getAuthor().isBot()) return;
		if (!ev.isFromGuild()) return;
		TextChannel tc = ev.getTextChannel();
		String msg = ev.getMessage().getContentRaw();
		if(msg.equals("^서버상태")) {
			plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
				@Override
				public void onRun() {
					sendStatus(tc);
				}
			});
			return;
		}
		if(msg.startsWith("^돈")) {
			String name = ev.getMessage().getContentRaw().substring(3);
			IPlayer player = plugin.getServer().getOfflinePlayer(name);
			if(player == null) {
				tc.sendMessage("해당 플레이어가 존재하지 않습니다.").queue();
				return;
			}
			tc.sendMessage(plugin.economyAPI.myMoney(player) + "원").queue();
		}
		if(msg.equals("/재부팅")){
			tc.sendMessage("서버 종료하는 중...").queue();
			plugin.getServer().shutdown();
		}
		if(msg.equals("^내돈")) {
			tc.sendMessage("아직 초코서버와 연동되지 않았습니다. 곧 추가될 기능입니다.").queue();
		}
	}
	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent ev){
		if(this.plugin.accountManager.isExistToken(ev.getMessage().getContentRaw())){
			String token = ev.getMessage().getContentRaw();
			//this.plugin.accountManager.linkAccount((String) this.plugin.accountManager.getTokenData(token).get("name"), ev.getAuthor().getId());
			this.plugin.accountManager.toggleAwaitState(token);
			ev.getChannel().sendMessage("서버에 접속하여 ``/디스코드`` 명령어를 입력해보세요!").queue();
		}
		if(!ev.getMessage().getAttachments().isEmpty()){
			if(!this.plugin.accountManager.isLinkedById(ev.getAuthor().getId())){
				ev.getChannel().sendMessage("이미지 업로드 기능을 사용하려면 먼저 연동해야합니다!\n서버에서 ``/디스코드`` 를 입력해보세요.").queue();
				return;
			}
			List<Message.Attachment> attachments = ev.getMessage().getAttachments();
			Message.Attachment attachment = attachments.get(0);
			PrivateChannel pc = ev.getChannel();
			if(!attachment.isImage()) return;
			if(!attachment.getFileExtension().equals("png") || !attachment.getFileExtension().equals("jpg")){
				pc.sendMessage("이미지 확장자명은 png 또는 jpg 파일만 지원합니다.").queue();
				return;
			}
			if(attachment.getSize()/1000000 > 3){
				pc.sendMessage("이미지 크기는 최대 3메가를 넘을 수 없습니다.").queue();
				return;
			}

			ev.getChannel().sendMessage("크기 : " + attachment.getSize() + "\n확장자 : " + attachment.getFileExtension() + "\n 이미지 ? :" + attachment.isImage() + "\n"
		+ attachment.getHeight()).queue();
			plugin.getLogger().warning("크기 : " + attachment.getSize() + "\n확장자 : " + attachment.getFileExtension() + "\n 이미지 ? :" + attachment.isImage() + "\n"
					+ attachment.getHeight());
		}
	}
	
	public void sendStatus(TextChannel tc) {
		LinkedHashMap<String, StringBuilder> map = new LinkedHashMap<String, StringBuilder>();
		this.plugin.getServer().getOnlinePlayers().forEach((UUID, player) -> {
			StringBuilder bd = map.getOrDefault(player.getLevel().getName(), new StringBuilder());
			bd.append(player.getName()).append(",");
			map.put(player.getLevel().getName(), bd);
		});
		EmbedBuilder eb = new EmbedBuilder();
		eb.setTitle("현재 서버상태");
		eb.setColor(Color.ORANGE);
		eb.addField("서버 정보", "최고 동접 : " + (int) plugin.serverData.getOrDefault("maxPlayers", 20), false);
		eb.addField("동접", plugin.getServer().getOnlinePlayers().size() + "명", false);
		eb.addField("업타임", formatUptime(System.currentTimeMillis() - Nukkit.START_TIME), false);
		eb.addField("로드율", plugin.getServer().getTickUsage() + "%", false);
		map.forEach((levelName , bd) -> {
			eb.addField("월드 " + levelNames.getOrDefault(levelName, levelName), bd.toString(), false);
		});
		tc.sendMessage(eb.build()).queue();
	}

	//	From Nukkit project
	private static String formatUptime(long uptime) {
		long days = TimeUnit.MILLISECONDS.toDays(uptime);
		uptime -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(uptime);
		uptime -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime);
		uptime -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(uptime);
		return String.format(UPTIME_FORMAT, days, hours, minutes, seconds);
	}
}
