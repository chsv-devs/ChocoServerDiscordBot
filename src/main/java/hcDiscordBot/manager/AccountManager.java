package hcDiscordBot.manager;

import cn.nukkit.utils.Config;
import hcDiscordBot.HcDiscordBot;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@SuppressWarnings({"unchecked", "unused"})
public class AccountManager {
    public static final int MAIN_FORM = 7192;

    private HcDiscordBot plugin;
    private LinkedHashMap<String, String> tokenMap;       //key -> token, value -> discord user id
    private LinkedHashMap<String, String> linkedAccountMap;      //key -> discord user id, value -> player name

    public AccountManager(){
        this.plugin = HcDiscordBot.instant;

        Config discordAccountConfig = new Config(plugin.getDataFolder().getAbsolutePath() + "/linkedAccount.json", Config.JSON);
        this.tokenMap = new LinkedHashMap<>(discordAccountConfig.get("tokenMap", new LinkedHashMap<>()));
        this.linkedAccountMap = new LinkedHashMap<>(discordAccountConfig.get("linkedAccount", new LinkedHashMap<>()));
    }

    public static String createToken(){
        return Integer.toString(ThreadLocalRandom.current().nextInt(100000) + 10000);
    }

    public void save(){
        Config discordAccountConfig = new Config(plugin.getDataFolder().getAbsolutePath() + "/linkedAccount.json", Config.JSON);
        discordAccountConfig.set("tokenMap", this.tokenMap);
        discordAccountConfig.set("linkedAccount", this.linkedAccountMap);
        discordAccountConfig.save();
    }

    public String getUserNameById(String id){
        return this.linkedAccountMap.get(id);
    }

    public String registerNewToken(String dcUserId){
        String token = createToken();
        this.tokenMap.put(token, dcUserId);

        return token;
    }

    public void linkAccount(String token, String username){
        String dcUserId = this.tokenMap.get(token);
        username = username.toLowerCase();

        this.tokenMap.remove(token);
        this.linkedAccountMap.put(dcUserId, username);
    }

    public String unlinkAccountById(String dcUserId){
        return this.linkedAccountMap.remove(dcUserId);
    }

    public boolean isValidToken(String token){
        return this.tokenMap.containsKey(token);
    }

    public boolean isLinkedById(String id){
        return this.linkedAccountMap.containsKey(id);
    }
}
