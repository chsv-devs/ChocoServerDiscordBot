package hcDiscordBot.discord.manager;

import cn.nukkit.Server;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.discord.JDAListener;
import hcDiscordBot.discord.textchannel.TodayReportTextChannel;
import hcDiscordBot.manager.TextChannelManager;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import javax.security.auth.login.LoginException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import static hcDiscordBot.HcDiscordBot.sdf;

@Getter
@Setter
public class JDAManager {
    public final HcDiscordBot plugin;
    public JDA jda;
    public JDABuilder jdaBuilder;
    public Guild guild;

    public TextChannelManager textChannelManager;

    public String today = new SimpleDateFormat("yyyy년 MM월 dd일").format(System.currentTimeMillis());
    public long todayTimestamp = System.currentTimeMillis();

    private boolean isJdaReady = false;
    private final String TOKEN;

    public JDAManager() {
        plugin = HcDiscordBot.instant;
        Config config = plugin.getConfig();
        TOKEN = config.getString("token");

        if (TOKEN == null || TOKEN.isEmpty()) {
            plugin.getLogger().error("Config 파일에 token이 설정되어있지 않습니다.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        } else {
            this.isJdaReady = this.loadJDA();
        }

        this.textChannelManager = new TextChannelManager(this);
    }

    public boolean loadJDA() {
        jdaBuilder = JDABuilder.createDefault(TOKEN);
        jdaBuilder.setAutoReconnect(true);
        jdaBuilder.setStatus(OnlineStatus.ONLINE);
        jdaBuilder.setActivity(Activity.watching("시즌3 시범운영"));
        jdaBuilder.addEventListeners(new JDAListener());
        jdaBuilder.addEventListeners(this.plugin.getImageManager());

        try {
            jda = jdaBuilder.build().awaitReady();
        } catch (LoginException | InterruptedException e) {
            this.plugin.getLogger().error("", e);
            return false;
        }

        Config config = plugin.getConfig();
        this.guild = jda.getGuildById(config.getString("guildID"));

        return true;
    }

    public void checkJDA() {
        if (!isJdaReady) {
            this.isJdaReady = loadJDA();
            if(this.isJdaReady) schedule();
        }
    }

    public ArrayList<Member> searchMemberByName(String name) {
        name = name.trim().toLowerCase();
        ArrayList<Member> memberList = new ArrayList<>();
        for (Member member : this.guild.getMembers()) {
            if (member.getUser().getName().toLowerCase().startsWith(name)) {
                memberList.add(member);
            }
        }
        return memberList;
    }

    public void schedule() {
        Server.getInstance().getScheduler().scheduleDelayedRepeatingTask(plugin, new AsyncTask() {

            @Override
            public void onRun() {
                if (!today.equals(sdf.format(System.currentTimeMillis()))) {
                    checkJDA();

                    TodayReportTextChannel textChannel = getTextChannelManager().getTodayReportTextChannel();
                    EmbedBuilder embedBuilder = new EmbedBuilder();

                    StringBuilder playerListStringBuilder = new StringBuilder();
                    StringBuilder bandContentBuilder = new StringBuilder();
                    String mainContents;

                    LinkedHashMap<String, Object> data = plugin.getApiManager().getTodayDB().getData(todayTimestamp);
                    HashSet<String> todayPlayers = (HashSet<String>) data.getOrDefault("dc_today_pl", new HashSet<String>());
                    int todayMax = (int) data.getOrDefault("dc_today_max", 0);

                    todayPlayers.forEach((player) -> playerListStringBuilder.append(player).append("님\n"));
                    mainContents = playerListStringBuilder.toString();

                    embedBuilder.setTitle("데일리 리포트");
                    embedBuilder.addField("오늘의 접속자",
                            mainContents.substring(0, Math.min(mainContents.length(), 1000)),
                            false);

                    bandContentBuilder
                            .append(today)
                            .append("데일리 리포트\n")
                            .append("\n서버 개요 : http://ccc1.kro.kr:19139/server/Chocoserver#tab-online-activity-overview&calendar-tab\n\n누적 최고접속 : ")
                            .append(plugin.serverData.getOrDefault("maxPlayers", 20))
                            .append("\n오늘자 최고접속 : ").append(todayMax)
                            .append("\n오늘 판매된 아이탬 개수 : ").append((int) data.getOrDefault("hshop_sell", 0))
                            .append("개\n오늘 구매된 아이템 개수 : ").append((int) data.getOrDefault("hshop_buy", 0))
                            .append("개\n오늘 구매된 섬 및 땅 개수 : ").append((int) data.getOrDefault("sololand_new_island", 0)).append("개")
                            .append("\n오늘 채팅 횟수 : ").append((int) data.getOrDefault("dc_count_chat", 0)).append("회")
                            .append("\n오늘의 접속자(").append(todayPlayers.size()).append(")\n\n")
                            .append(mainContents);

                    plugin.getApiManager().getBandMaster().postBand(bandContentBuilder.toString(), false, true);

                    try {
                        if (textChannel != null)
                            textChannel.sendEmbedMessage(embedBuilder.build());
                    } catch (Exception e) {
                        Server.getInstance().getLogger().error("", e);
                    }

                    today = sdf.format(System.currentTimeMillis());
                    todayTimestamp = System.currentTimeMillis();
                }
            }
        }, 20 * 20, 20 * 20, true);

        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new AsyncTask() {

            @Override
            public void onRun() {
                getJda().getPresence().setActivity(
                        Activity.playing((Server.getInstance().getOnlinePlayers().size()) + "명의 🍫"));
            }

        }, 20 * 5, true);

        Server.getInstance().getScheduler().scheduleRepeatingTask(plugin, new AsyncTask() {
            @Override
            public void onRun() {
                checkJDA();
            }
        }, 20 * 120, true);
    }

    public boolean isJdaReady() {
        return this.isJdaReady;
    }
}
