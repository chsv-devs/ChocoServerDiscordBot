package hcDiscordBot.minecraft.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class UuidData {
    private final String UUID;
    private ArrayList<String> playerList;

    public UuidData(String uuid, ArrayList<String> playerList){
        this.UUID = uuid;
        this.playerList = playerList;
    }
}
