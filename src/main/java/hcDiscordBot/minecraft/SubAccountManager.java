package hcDiscordBot.minecraft;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.discord.textchannel.AdminTextChannel;
import hcDiscordBot.minecraft.entity.IpData;
import hcDiscordBot.minecraft.entity.UuidData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.*;

import static hcDiscordBot.HcDiscordBot.sdf;

@SuppressWarnings("unused")
public class SubAccountManager {
    public HcDiscordBot plugin;
    public LinkedHashMap<String, IpData> ipMap = new LinkedHashMap<>();
    public LinkedHashMap<String, UuidData> uuidMap = new LinkedHashMap<>();
    public LinkedHashMap<String, String> warnedPlayerMap;

    public SubAccountManager(){
        this.plugin = HcDiscordBot.instant;
        Config config = new Config(plugin.getDataFolder().getPath() + "/subAccountData.json", Config.JSON);

        LinkedHashMap<String, Object> ipMapData = new LinkedHashMap<>(config.get("ipMap", new LinkedHashMap<>()));
        ipMapData.forEach((key, data) -> {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>((Map) data);
            IpData ipData = new IpData(key, (ArrayList<String>) map.get("playerList"));
            this.ipMap.put(key, ipData);
        });

        LinkedHashMap<String, Object> uuidMapData = new LinkedHashMap<>(config.get("uuidMap", new LinkedHashMap<>()));
        uuidMapData.forEach((key, data) -> {
            LinkedHashMap<String, Object> map = new LinkedHashMap<>((Map) data);
            UuidData uuidData = new UuidData(key, (ArrayList<String>) map.get("playerList"));
            this.uuidMap.put(key, uuidData);
        });

        warnedPlayerMap = new LinkedHashMap<>(config.get("warnedPlayerMap", new LinkedHashMap<>()));
    }

    public void save(){
        Config config = new Config(plugin.getDataFolder().getPath() + "/subAccountData.json", Config.JSON);

        config.set("ipMap", ipMap);
        config.set("uuidMap", uuidMap);
        config.set("warnedPlayerMap", warnedPlayerMap);
        config.save();
    }

    public void checkSubAccount(Player player) {
        if(!plugin.getJdaManager().isJdaReady()) return;
        String playerName = player.getName();

        IpData ipData = this.getIpData(player.getAddress());
        UuidData uuidData = this.getUuidData(player.getUniqueId().toString());

        boolean ipWarn = hasSubAccount(playerName, ipData.getPlayerList());
        boolean uuidWarn = hasSubAccount(playerName, uuidData.getPlayerList());

        if (!ipData.getPlayerList().contains(playerName)) {
            ipData.getPlayerList().add(playerName);
        }
        if (!uuidData.getPlayerList().contains(playerName)) {
            uuidData.getPlayerList().add(playerName);
        }

        if (!isWarned(playerName)) {
            String title = null;
            EmbedBuilder embedBuilder = new EmbedBuilder();

            if (uuidWarn) {
                title = "부계정 - uuid동일";
            } else if (ipWarn) {
                title = "부계정 - IP동일";
            }

            if(title != null) {
                AdminTextChannel textChannel = plugin.getJdaManager().getTextChannelManager().getAdminTextChannel();

                embedBuilder.setTitle(title);
                embedBuilder.addField("정보",
                        "닉네임 : " + player.getName() +
                                "\nIP : " + ipData.getIP() +
                                "\nUUID : " + uuidData.getUUID()
                                + "\n\n동일 IP플레이어 : "
                                + ipData.getPlayerList().toString(),
                        false);
                textChannel.sendEmbedMessage(embedBuilder.build());

                this.addWarnedPlayer(playerName);
            }
        }

        //ipMap.put(ipData.getIP(), ipList);
        //uuidMap.put(uuid, uuidList);

        plugin.getLogger().info(ipData.getPlayerList().toString());
        plugin.getLogger().info(player.getName() + "의 정보\nip : " + ipData.getIP() + "\nuuid : " + uuidData.getUUID() + "\n부계 알림 여부 : "
                + isWarned(player.getName()));
    }

    public boolean hasSubAccount(String name, ArrayList<String> list) {
        int size = list.size();
        if (size > 0) {
            if (size == 1) {
                return !(list.get(0).equals(name));
            }
            return true;
        }
        return false;
    }

    public void addWarnedPlayer(String playerName){
        this.warnedPlayerMap.put(playerName, sdf.format(System.currentTimeMillis()));
    }

    public boolean isWarned(String playerName){
        return this.warnedPlayerMap.containsKey(playerName);
    }

    public IpData getIpData(String ip){
        if(!this.ipMap.containsKey(ip)) this.ipMap.put(ip, new IpData(ip, new ArrayList<>()));
        return this.ipMap.get(ip);
    }

    public UuidData getUuidData(String uuid){
        if(!this.uuidMap.containsKey(uuid)) this.uuidMap.put(uuid, new UuidData(uuid, new ArrayList<>()));
        return this.uuidMap.get(uuid);
    }
}
