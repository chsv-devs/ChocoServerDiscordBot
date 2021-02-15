package hcDiscordBot.manager;

import bandMaster.BandMaster;
import cn.nukkit.Server;
import com.hancho.hguild.HGuild.HGuild;
import hFriend.HFriend;
import hancho.todayDB.TodayDB;
import lombok.Getter;
import lombok.Setter;
import me.onebone.economyapi.EconomyAPI;

@Getter
@Setter
public class ApiManager {
    private final Server server;

    public EconomyAPI economyAPI;
    public BandMaster bandMaster;
    public TodayDB todayDB;
    public HFriend hFriend;
    public HGuild hGuild;

    public ApiManager(){
        this.server = Server.getInstance();

        this.economyAPI = (EconomyAPI) server.getPluginManager().getPlugin("EconomyAPI");
        this.bandMaster = (BandMaster) server.getPluginManager().getPlugin("BandMaster");
        this.todayDB = (TodayDB) server.getPluginManager().getPlugin("TodayDB");
        this.hFriend = (HFriend) server.getPluginManager().getPlugin("HFriend");
        this.hGuild = (HGuild) server.getPluginManager().getPlugin("HGuild");
    }
}
