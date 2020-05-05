package hcDiscordBot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.LinkedHashMap;

public class ImageManager {
    public Guild guild;
    public AccountManager accountManager;
    public LinkedHashMap<String, Object> data;
    public TextChannel verifyTC;

    public ImageManager(LinkedHashMap<String, Object> data, Guild guild, AccountManager accountManager){
        this.data = data;
        this.guild = guild;
        this.accountManager = accountManager;
        this.verifyTC = guild.getTextChannelById("705971666769018930");
    }

    public void userRequested(String userid, PrivateMessageReceivedEvent event){

    }

}
