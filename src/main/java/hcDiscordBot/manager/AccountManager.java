package hcDiscordBot.manager;

import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import hcDiscordBot.HcDiscordBot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static hcDiscordBot.HcDiscordBot.PREFIX;

@SuppressWarnings({"unchecked", "unused"})
public class AccountManager {
    public static final String[] strings = {"S","D","G","E"};
    public static final int MAIN_FORM = 7192;
    public static final int ENTER_NAME_LINK_FORM = MAIN_FORM + 1;
    public static final int SELECT_LINK_FORM = MAIN_FORM + 2;

    private final LinkedHashMap<String, Object> accountData;

    public AccountManager(){
        this.accountData = HcDiscordBot.INSTANCE.linkAccountData;
    }

    public void showMainForm(Player player){
        ArrayList<ElementButton> buttons = new ArrayList<>();
        buttons.add(new ElementButton("계정 연동하기"));

        String content = PREFIX + "계정 연동 상태 : " +
                (this.isLinkedByName(player.getName()) ? "네" : "아니오");

        FormWindowSimple form = new FormWindowSimple("§0§l디스코드", content, buttons);
        player.showFormWindow(form, MAIN_FORM);

        /*
        ---------------------------
        |   discordAccountConfig  |
        ---------------------------
              |
               ->  user1 <String(name), Object>...
               ->  user2 <String(name), Object>...
                                            |
                                             -> (String) id
                                             -> (String) date
               ->  link_discordID_with_Nickname <String(name), String(id)>
               ->  token_byName <String(name), String(token)>
               ->  token_data <String(token), Object>
                                                |
                                                 ->  (boolean) isWaiting
                                                 -> (String) name
         */
    }

    public LinkedHashMap<String, Object> getUserData(String name){
        name = name.toLowerCase();
        return (LinkedHashMap<String, Object>) this.accountData.getOrDefault(name, new LinkedHashMap<>());
    }

    public String getLinkedDate(String gameNickname){
        gameNickname = gameNickname.toLowerCase();
        return (String) this.getUserData(gameNickname).getOrDefault("date", "0");
    }

    public LinkedHashMap<String, String> getIdMap(){
        return (LinkedHashMap<String, String>) this.accountData.getOrDefault("link_discordID_with_Nickname", new LinkedHashMap<>());
    }

    public String getID(String name){
        name = name.toLowerCase();
        LinkedHashMap<String, Object> userData = this.getUserData(name);
        return this.getID(userData);
    }

    public String getID(LinkedHashMap<String, Object> m){
        return (String) m.get("id");
    }

    public void setIdMap(LinkedHashMap<String, String> m){
        this.accountData.put("link_discordID_with_Nickname", m);
    }

    public String getGameNickname(String discordId){
        return this.getIdMap().get(discordId);
    }

    public boolean isLinkedByName(String name){
        name = name.toLowerCase();
        return this.accountData.containsKey(name);
    }

    public boolean isLinkedById(String id){
        return this.getIdMap().containsKey(id);
    }

    public LinkedHashMap<String, String> getTokenByNameMap(){
        return (LinkedHashMap<String, String>) this.accountData.getOrDefault("token_byName", new LinkedHashMap<>());
    }

    public LinkedHashMap<String, Object> getTokenDataMap(){
        return (LinkedHashMap<String, Object>) this.accountData.getOrDefault("token_data", new LinkedHashMap<>());
    }

    public void setTokenByNameMap(LinkedHashMap<String, String> m){
        this.accountData.put("token_byName", m);
    }

    public void setTokenDataMap(LinkedHashMap<String, Object> m){
        this.accountData.put("token_data", m);
    }

    public void addNewTokenData(String name, String token){
        LinkedHashMap<String, String> m = this.getTokenByNameMap();
        m.put(name, token);
        this.setTokenByNameMap(m);
        LinkedHashMap<String, Object> tokenDataMap = this.getTokenDataMap();
        LinkedHashMap<String, Object> tm = new LinkedHashMap<>();
        tm.put("name", name);
        tokenDataMap.put(token, tm);
        this.setTokenDataMap(tokenDataMap);
    }

    public LinkedHashMap<String, Object> getTokenData(String token){
        return (LinkedHashMap<String, Object>) this.getTokenDataMap().getOrDefault(token, new LinkedHashMap<>());
    }

    public void setTokenData(String token, LinkedHashMap<String, Object> m){
        LinkedHashMap<String, Object> tokenMap = this.getTokenDataMap();
        tokenMap.put(token, m);
        this.setTokenDataMap(tokenMap);
    }

    public boolean isExistToken(String token){
        return this.getTokenDataMap().containsKey(token);
    }

    public String getTokenByName(String name){
        name = name.toLowerCase();
        return this.getTokenByNameMap().get(name);
    }

    public void removeToken(String token){
        LinkedHashMap<String, Object> tokenMap = this.getTokenDataMap();
        LinkedHashMap<String, Object> data = getTokenData(token);
        LinkedHashMap<String, String> byNameMap = this.getTokenByNameMap();
        byNameMap.remove(data.getOrDefault("name", ""));
        this.setTokenByNameMap(byNameMap);
        tokenMap.remove(token);
        this.setTokenDataMap(tokenMap);

    }

    public String createToken(){
        return ThreadLocalRandom.current().nextInt(100000) +
                strings[ThreadLocalRandom.current().nextInt(strings.length)];
    }

    public void resetToken(String name){
        name = name.toLowerCase();
        String token = this.getTokenByName(name);

        this.removeToken(token);
        this.addNewTokenData(name, this.createToken());
    }

    public void toggleAwaitState(String token){
        LinkedHashMap<String, Object> data = this.getTokenData(token);

        data.put("isWaiting", !((boolean) data.getOrDefault("isWaiting", false)));
        this.setTokenData(token, data);
    }

    public void linkAccount(String name, String id){
        name = name.toLowerCase();
        LinkedHashMap<String, Object> m = new LinkedHashMap<>();
        m.put("date", System.currentTimeMillis());
        this.accountData.put(name, m);

        LinkedHashMap<String, String> idMap = this.getIdMap();
        idMap.put(id, name);
        this.resetToken(name);
        this.setIdMap(idMap);
    }

    public void unLinkAccountByName(String name){
        name = name.toLowerCase();

        LinkedHashMap<String, Object> userData = this.getUserData(name);
        String id = this.getID(userData);

        LinkedHashMap<String, String> idMap = this.getIdMap();
        idMap.remove(id);

        this.setIdMap(idMap);
        this.accountData.remove(name);
    }
}
