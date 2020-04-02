package hancho.plugin.nukkit.discordapi;

import java.util.ArrayList;

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
		if(ev.getMessage().getContentRaw().equals("^서버상태") == true) {
			ArrayList<String> players = new ArrayList<>();
			this.plugin.getServer().getOnlinePlayers().forEach((UUID, player) -> {
				players.add(player.getName());
			});
			tc.sendMessage("시즌3 온라인 : " + plugin.getServer().getOnlinePlayers().size() + "\n플레이어 : " + players.toString()).queue();
		}
	}
}
