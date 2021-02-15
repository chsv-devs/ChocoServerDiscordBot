package hcDiscordBot.minecraft.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class IpData {
    private final String IP;
    private ArrayList<String> playerList;

    public IpData(String ip, ArrayList<String> playerList){
        this.IP = ip;
        this.playerList = playerList;
    }
}
