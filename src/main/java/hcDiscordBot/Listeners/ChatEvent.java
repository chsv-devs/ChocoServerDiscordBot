package hcDiscordBot.Listeners;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import hcDiscordBot.HcDiscordBot;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;

public class ChatEvent implements Listener {
    public static final String[] BAD_WORDS = {
            "시발", "병신", "애미", "뒤진", "쉑", "ㅈㄲ", "ㅈㄹ", "지랄", "좆", "ㅄ"
            , "ㅂㅅ", "후장", "ㅅㅂ", "섻", "닥쳐", "섹스", "섹", "븅", "썅", "씨발"
            , "ㅆㅂ","앰뒤","호로","tlqkf", "SSIBAL", "자위", "ㅗ", "새꺄"
            , "ㅆ바", "존나", "ㅈㄴ", "새끼", "개소리", "뒤질"
    };
    public final Color embedColor = new Color(255, 82, 121);
    public HcDiscordBot owner;

    public ChatEvent(HcDiscordBot owner){
        this.owner = owner;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(PlayerChatEvent ev){
        boolean isSent = false;
        boolean isReplaced = false;
        String message = ev.getMessage();
        this.owner.todayDB.addCount("dc_count_chat", 1);
        // 욕설 감지 부분, 성능 문제를 최소화 하기 위하여 아주 기본적인 방법으로만 검사함
        for(String s : BAD_WORDS){
            if(message.indexOf(s) > -1){
                if(message.equals("ㅈ")) return;
                if(s.equals("ㅈ")){
                    if(message.contains("ㅈㅔ")) return;
                }
                if(s.equals("ㅗ")){
                    if(message.contains("ㅗㅜㅑ")) return;
                }
                if(s.equals("호로")){
                    if(message.contains("칭호로")) return;
                }
                if(!isSent) {
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("욕설 감지 : " + ev.getPlayer().getName());
                    embedBuilder.addField("내용", message, false);
                    embedBuilder.addField("감지된 욕설", s, false);
                    embedBuilder.setColor(this.embedColor);
                    if (!this.owner.isJDALoaded) {
                        this.owner.getLogger().warning("욕설감지 : " + ev.getPlayer().getName());
                    } else {
                        this.owner.sendEmbedMessage("704679105664385087", embedBuilder.build());
                    }
                    isSent = true;
                }
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < s.length(); i++){
                    sb.append('★');
                }
                message = StringUtils.replace(message, s, sb.toString());
                isReplaced = true;
            }
        }
        if(isReplaced){
            ev.setMessage(message);
        }
    }
}
