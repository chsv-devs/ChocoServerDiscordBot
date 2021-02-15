package hcDiscordBot.discord.textchannel;

import hcDiscordBot.discord.manager.JDAManager;

public class TodayReportTextChannel extends DiscordTextChannel{
    public TodayReportTextChannel(JDAManager jdaManager, String textChannelId) {
        super(jdaManager, textChannelId);
    }
}
