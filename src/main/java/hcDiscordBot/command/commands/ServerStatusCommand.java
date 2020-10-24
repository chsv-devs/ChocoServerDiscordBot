package hcDiscordBot.command.commands;

import cn.nukkit.Nukkit;
import cn.nukkit.Server;
import hcDiscordBot.command.BaseCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

public class ServerStatusCommand extends BaseCommand {
    public static final String UPTIME_FORMAT = "%d일 %d시간 %d분 %d초";

    private final LinkedHashMap<String, String> customLevelName = new LinkedHashMap<>();

    public ServerStatusCommand(String name) {
        super(name);
        this.customLevelName.put("spawn24", "스폰");
        this.customLevelName.put("cave", "돌광산");
    }

    @Override
    public void onCommand(TextChannel tc, Message message) {
        LinkedHashMap<String, StringBuilder> map = new LinkedHashMap<>();

        Server.getInstance().getOnlinePlayers().forEach((UUID, player) -> {
            StringBuilder sb = map.getOrDefault(player.getLevel().getName(), new StringBuilder());
            sb.append(player.getName()).append(",");
            map.put(player.getLevel().getName(), sb);
        });

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("--------현재 서버상태--------");
        eb.setColor(Color.ORANGE);
        eb.addField("서버 정보", "최고 동접 : " + (int) this.plugin.serverData.getOrDefault("maxPlayers", 20), false);
        eb.addField("동접", this.plugin.getServer().getOnlinePlayers().size() + "명", false);
        eb.addField("업타임", formatUptime(System.currentTimeMillis() - Nukkit.START_TIME), false);
        eb.addField("로드율", plugin.getServer().getTickUsage() + "%", false);
        map.forEach((levelName , bd) -> {
            eb.addField("월드 " + customLevelName.getOrDefault(levelName, levelName), bd.toString(), false);
        });
        tc.sendMessage(eb.build()).queue();
    }

    // From Nukkit project
    private static String formatUptime(long uptime) {
        long days = TimeUnit.MILLISECONDS.toDays(uptime);
        uptime -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(uptime);
        uptime -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime);
        uptime -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(uptime);
        return String.format(UPTIME_FORMAT, days, hours, minutes, seconds);
    }
}
