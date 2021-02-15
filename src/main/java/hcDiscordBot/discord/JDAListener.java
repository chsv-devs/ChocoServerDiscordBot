package hcDiscordBot.discord;

import cn.nukkit.Server;
import hcDiscordBot.discord.manager.BotCommandManager;
import hcDiscordBot.HcDiscordBot;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class JDAListener extends ListenerAdapter {
	public static final String COMMAND_PREFIX = "^";

	private final BotCommandManager botCommandManager;

	public JDAListener() {
		this.botCommandManager = HcDiscordBot.instant.getBotCommandManager();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent ev) {
		if(ev.getAuthor().isBot()) return;
		String content = ev.getMessage().getContentRaw();

		if(content.startsWith(COMMAND_PREFIX)) {
			if(ev.isFromType(ChannelType.PRIVATE)) {
				this.botCommandManager.processMessage(ev.getPrivateChannel(), ev.getMessage());
			}else{
				this.botCommandManager.processMessage(ev.getTextChannel(), ev.getMessage());
			}
		}
	}
}
