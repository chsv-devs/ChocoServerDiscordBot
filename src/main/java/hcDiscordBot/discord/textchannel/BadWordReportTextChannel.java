package hcDiscordBot.discord.textchannel;

import hcDiscordBot.discord.manager.JDAManager;

public class BadWordReportTextChannel extends DiscordTextChannel {
    public BadWordReportTextChannel(JDAManager jdaManager, String textChannelId) {
        super(jdaManager, textChannelId);
    }
}
