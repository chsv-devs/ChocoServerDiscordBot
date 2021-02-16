package hcDiscordBot.discord.textchannel;

import hcDiscordBot.discord.manager.JDAManager;

public class ImageUploadedChannel extends DiscordTextChannel{
    public ImageUploadedChannel(JDAManager jdaManager, String textChannelId) {
        super(jdaManager, textChannelId);
    }
}
