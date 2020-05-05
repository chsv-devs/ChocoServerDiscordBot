package hcDiscordBot;

import cn.nukkit.Player;
import cn.nukkit.Server;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;

import static hcDiscordBot.HcDiscordBot.sdf;

public class SubAccountManager {
    public Server server;
    public HcDiscordBot plugin;
    public LinkedHashMap<String, Object> subAccountData;
    public LinkedHashMap<String, String> isWarnedData;

    public SubAccountManager(HcDiscordBot plugin, LinkedHashMap<String, Object> subAccountData, LinkedHashMap<String , String> isWarnedData){
        this.plugin = plugin;
        this.subAccountData = subAccountData;
        this.isWarnedData = isWarnedData;
        this.server = Server.getInstance();
    }

    public LinkedHashMap<String, ArrayList<String>> getIpMap(){
        return (LinkedHashMap<String, ArrayList<String>>) this.subAccountData.getOrDefault("ips_", new LinkedHashMap<String, ArrayList<String>>());
    }

    public LinkedHashMap<String, ArrayList<String>> getUUIDMap(){
        return (LinkedHashMap<String, ArrayList<String>>) this.subAccountData.getOrDefault("uuids_", new LinkedHashMap<String, ArrayList<String>>());
    }

    public ArrayList<String> getIpList(String ip){
        return getIpMap().getOrDefault(ip, new ArrayList<>());
    }

    public ArrayList<String> getUUIDList(String uuid){
        return getUUIDMap().getOrDefault(uuid, new ArrayList<>());
    }

    public String getWarnedDate(String name) {
        return this.isWarnedData.get(name);
    }

    public void warnPlayer(String name) {
        this.isWarnedData.put(name, sdf.format(new Date()));
    }

    public boolean isWarned(String name) {
        return this.isWarnedData.containsKey(name);
    }

    public boolean hasSubAccount(String name, ArrayList<String> list) {
        int size = list.size();
        if (size > 0) {
            if (size == 1) {
                if (!list.get(0).equals(name)) {
                    return  true;
                }else{
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void checkSubAccount(Player player) {
        if(plugin.isJDALoaded == false) return;
        String name = player.getName();
        String uuid = player.getUniqueId().toString();
        String ip = player.getAddress();

        LinkedHashMap<String, ArrayList<String>> ipMap = getIpMap();
        LinkedHashMap<String, ArrayList<String>> uuidMap = getUUIDMap();
        ArrayList<String> ipList = getIpList(ip);
        ArrayList<String> uuidList = getUUIDList(uuid);

        boolean ipWarn = hasSubAccount(name, ipList);
        boolean uuidWarn = hasSubAccount(name, uuidList);

        if (!uuidList.contains(name)) {
            uuidList.add(name);
        }
        if (!ipList.contains(name)) {
            ipList.add(name);
        }

        if (!isWarned(name)) {
            String title = null;
            EmbedBuilder eb = new EmbedBuilder();

            if (uuidWarn) {
                title = "부계정 - uuid동일";
            } else if (ipWarn) {
                title = "부계정 - IP동일";
            }

            if(title != null) {
                TextChannel tc = plugin.guild.getTextChannelById("590963879190986752");
                warnPlayer(name);
                eb.setTitle(title);
                eb.addField("정보", "닉네임 : " + player.getName() + "\nIP : " + ip + "\nUUID : " + uuid
                        + "\n\n동일 IP플레이어 : " + Arrays.toString(ipList.toArray()), false);
                tc.sendMessage(eb.build()).queue();
                tc.sendMessage("//whois " + player.getName()).queue();
            }
        }

        ipMap.put(ip, ipList);
        uuidMap.put(uuid, uuidList);
        subAccountData.put("ips_", ipMap);
        subAccountData.put("uuids_", uuidMap);

        plugin.getLogger().info(ipList.toString());
        plugin.getLogger().info(player.getName() + "의 정보\nip : " + ip + "\nuuid : " + uuid + "\n부계 알림 여부 : "
                + isWarned(player.getName()));
    }
}
