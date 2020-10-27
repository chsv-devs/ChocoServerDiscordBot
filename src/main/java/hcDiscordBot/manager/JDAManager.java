package hcDiscordBot.manager;

import cn.nukkit.utils.Config;
import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.listeners.DiscordListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;

public class JDAManager {
    public JDA jda;
    public JDABuilder jb;
    public HcDiscordBot plugin;

    public Guild mainGuild;

    public TextChannel adminTextChannel;
    public TextChannel todayReportChannel;
    public TextChannel badWordReportChannel;

    private final String token;

    public JDAManager(){
        this.plugin = HcDiscordBot.INSTANCE;

        Config config = plugin.getConfig();
        token = config.getString("token");
        if (token == null || token.isEmpty()) {
            plugin.getLogger().error("Config 파일에 token이 설정되어있지 않습니다.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
    }

    public boolean loadJDA(){
        jb = new JDABuilder(token);
        jb.setAutoReconnect(true);
        jb.setStatus(OnlineStatus.ONLINE);
        jb.setActivity(Activity.watching("시즌3 시범운영"));
        jb.addEventListeners(new DiscordListener());
        jb.addEventListeners(this.plugin.getImageManager());

        try {
            jda = jb.build();
        } catch (LoginException e) {
            this.plugin.getLogger().error("", e);
            return false;
        }
        Config config = plugin.getConfig();
        this.mainGuild = jda.getGuildById(config.getString("guildID"));
        this.adminTextChannel = jda.getTextChannelById(config.getString("adminChannel"));
        this.todayReportChannel = jda.getTextChannelById(config.getString("todayReportChannel"));
        this.badWordReportChannel = jda.getTextChannelById(config.getString("badWordReportChannel"));

        return true;
    }

    public JDA getJda() {
        return jda;
    }

    public JDABuilder getJb() {
        return jb;
    }

    public Guild getMainGuild() {
        return mainGuild;
    }

    public TextChannel getAdminTextChannel() {
        return adminTextChannel;
    }

    public TextChannel getTodayReportChannel() {
        return todayReportChannel;
    }

    public TextChannel getBadWordReportChannel() {
        return badWordReportChannel;
    }

    public void sendMessage(String textChannelId, String content){
        if(!this.plugin.isJDALoaded){
            this.plugin.getLogger().warning(content);
            return;
        }
        TextChannel tc = this.mainGuild.getTextChannelById(textChannelId);
        this.sendMessage(this.mainGuild, tc, content);
    }

    public void sendMessage(Guild guild, TextChannel tc, String content){
        if(tc == null) return;
        tc.sendMessage(content).queue();
    }

    public void sendEmbedMessage(String textChannelId, MessageEmbed messageEmbed){
        if(!this.plugin.isJDALoaded) return;

        TextChannel tc = this.mainGuild.getTextChannelById(textChannelId);
        if(tc == null) return;

        tc.sendMessage(messageEmbed).queue();
    }

    public void sendMessageToAdminTC(String content) {
        if(!this.plugin.isJDALoaded){
            this.plugin.getLogger().error(content);
            return;
        }
        this.adminTextChannel.sendMessage(content).queue();
    }

    public ArrayList<Member> searchMemberByName(String name){
        name = name.trim().toLowerCase();
        ArrayList<Member> memberList = new ArrayList<>();
        for(Member member : this.mainGuild.getMembers()){
            if(member.getUser().getName().toLowerCase().startsWith(name)){
                memberList.add(member);
            }
        }
        return memberList;
    }
}
