package hcDiscordBot.discord.textchannel;

import cn.nukkit.Server;
import hcDiscordBot.discord.manager.JDAManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class DiscordTextChannel {
    private JDAManager jdaManager;
    private String textChannelId;

    private TextChannel textChannel;

    public DiscordTextChannel(JDAManager jdaManager, String textChannelId){
        this.jdaManager = jdaManager;
        this.textChannelId = textChannelId;

        checkTextChannelStatus();
    }

    public void checkTextChannelStatus(){
        if(this.textChannel != null) return;
        if(jdaManager.isJdaReady()){
            this.textChannel = jdaManager.getJda().getTextChannelById(this.textChannelId);
            if(textChannel == null) Server.getInstance().getLogger().warning("디스코드 텍스트 채널을 찾을 수 없습니다 : " + this.textChannelId);
        }
    }

    public MessageAction sendMessage(String message){
        this.checkTextChannelStatus();
        if(textChannel == null) return null;

        MessageAction action = textChannel.sendMessage(message);
        action.queue();

        return action;
    }

    public MessageAction sendEmbedMessage(MessageEmbed messageEmbed){
        this.checkTextChannelStatus();
        if(textChannel == null) return null;

        MessageAction action = textChannel.sendMessage(messageEmbed);
        action.queue();

        return action;
    }
}
