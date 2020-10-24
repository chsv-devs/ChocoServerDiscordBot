package hcDiscordBot.listeners;

import hcDiscordBot.manager.CommandManager;
import hcDiscordBot.HcDiscordBot;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordListener extends ListenerAdapter {
	public static final String COMMAND_PREFIX = "^";

	private final CommandManager commandManager;

	public DiscordListener() {
		this.commandManager = HcDiscordBot.INSTANCE.getCommandManager();
	}

	@Override
	public void onMessageReceived(MessageReceivedEvent ev) {
		if(ev.getAuthor().isBot() || !ev.isFromGuild()) return;
		TextChannel tc = ev.getTextChannel();
		String content = ev.getMessage().getContentRaw();

		if(content.startsWith(COMMAND_PREFIX)) {
			this.commandManager.processMessage(tc, ev.getMessage());
		}
	}

}
