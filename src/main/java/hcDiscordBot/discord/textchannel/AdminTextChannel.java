package hcDiscordBot.discord.textchannel;

import hcDiscordBot.discord.manager.JDAManager;

public class AdminTextChannel extends DiscordTextChannel {
    public AdminTextChannel(JDAManager jdaManager, String textChannelId) {
        super(jdaManager, textChannelId);
    }
}
