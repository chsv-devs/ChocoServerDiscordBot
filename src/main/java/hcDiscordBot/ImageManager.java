package hcDiscordBot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

import java.util.LinkedHashMap;

public class ImageManager {
    public AccountManager accountManager;
    public LinkedHashMap<String, Object> data;
    public String  verifyTCID = "705971666769018930";
    HcDiscordBot plugin;

    public ImageManager(LinkedHashMap<String, Object> data, HcDiscordBot pl){
        this.plugin = pl;
        this.data = data;
        this.accountManager = pl.accountManager;
    }

    public void userRequested(String userid, PrivateMessageReceivedEvent event){

    }

}
