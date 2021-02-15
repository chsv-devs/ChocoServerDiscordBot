package hcDiscordBot.manager;

import cn.nukkit.utils.Config;
import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.discord.manager.JDAManager;
import hcDiscordBot.discord.textchannel.AdminTextChannel;
import hcDiscordBot.discord.textchannel.BadWordReportTextChannel;
import hcDiscordBot.discord.textchannel.TodayReportTextChannel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextChannelManager {
    private final HcDiscordBot plugin;
    private AdminTextChannel adminTextChannel;
    private BadWordReportTextChannel badWordReportTextChannel;
    private TodayReportTextChannel todayReportTextChannel;

    public TextChannelManager(JDAManager jdaManager){
        this.plugin = HcDiscordBot.instant;
        Config config = plugin.getConfig();

        this.adminTextChannel = new AdminTextChannel(jdaManager, config.getString("adminChannel"));
        this.todayReportTextChannel = new TodayReportTextChannel(jdaManager, config.getString("todayReportChannel"));
        this.badWordReportTextChannel = new BadWordReportTextChannel(jdaManager, config.getString("badWordReportChannel"));
    }
}
