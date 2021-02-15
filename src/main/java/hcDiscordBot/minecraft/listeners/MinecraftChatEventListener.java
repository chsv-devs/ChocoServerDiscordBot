package hcDiscordBot.minecraft.listeners;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.discord.manager.JDAManager;
import hcDiscordBot.manager.AccountManager;
import hcDiscordBot.manager.ApiManager;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;

@SuppressWarnings("unused")
public class MinecraftChatEventListener implements Listener {
    public static final String[] BAD_WORDS = {
            "시발", "병신", "애미", "뒤진", "쉑", "ㅈㄲ", "ㅈㄹ", "지랄", "좆", "ㅄ"
            , "ㅂㅅ", "후장", "ㅅㅂ", "섻", "닥쳐", "섹스", "섹", "븅", "썅", "씨발"
            , "ㅆㅂ","앰뒤","호로","tlqkf", "SSIBAL", "자위", "ㅗ", "새꺄"
            , "ㅆ바", "존나", "ㅈㄴ", "새끼", "개소리", "뒤질"
    };
    public final Color embedColor = new Color(255, 82, 121);
    public HcDiscordBot plugin;
    public JDAManager jdaManager;

    public MinecraftChatEventListener(){
        this.plugin = HcDiscordBot.instant;
        this.jdaManager = this.plugin.getJdaManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(PlayerChatEvent ev){
        ApiManager apiManager = plugin.getApiManager();
        ArrayList<String> usedBadWords= null;
        AccountManager accountManager = this.plugin.getAccountManager();

        if(apiManager.getHFriend() != null){
            if(apiManager.getHFriend().chatMode.containsKey(ev.getPlayer().getName())) return;
        }
        if(apiManager.getHGuild() != null){
            if(apiManager.getHGuild().guildChat.containsKey(ev.getPlayer().getName())) return;
        }

        boolean isReplaced = false;
        String message = ev.getMessage();

        if(accountManager.isValidToken(message.trim())){
            accountManager.linkAccount(message.trim(), ev.getPlayer().getName());
            ev.getPlayer().sendMessage(HcDiscordBot.PREFIX + "성공적으로 연동되었습니다");
            ev.setCancelled();
            return;
        }

        apiManager.getTodayDB().addCount("dc_count_chat", 1);

        // 욕설 감지 부분, 성능 문제를 최소화 하기 위하여 아주 기본적인 방법으로만 검사함
        for(String badWord : BAD_WORDS){
            if(message.indexOf(badWord) > -1){
                if(message.equals("ㅈ")) return;
                if(badWord.equals("ㅈ")){
                    if(message.contains("ㅈㅔ")) return;
                }
                if(badWord.equals("ㅗ")){
                    if(message.contains("ㅗㅜㅑ")) return;
                }
                if(badWord.equals("호로")){
                    if(message.contains("칭호로")) return;
                }

                if(usedBadWords == null) usedBadWords = new ArrayList<>();
                usedBadWords.add(badWord);

                StringBuilder stringBuilder = new StringBuilder();
                for(int i = 0; i < badWord.length(); i++){
                    stringBuilder.append('★');
                }

                message = StringUtils.replace(message, badWord, stringBuilder.toString());
                isReplaced = true;
            }
        }

        if(usedBadWords != null) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("욕설 감지 : " + ev.getPlayer().getName());
            embedBuilder.addField("내용", message, false);
            embedBuilder.addField("감지된 욕설", usedBadWords.toString(), false);
            embedBuilder.setColor(this.embedColor);
            this.plugin.getJdaManager()
                    .getTextChannelManager().getBadWordReportTextChannel()
                    .sendEmbedMessage(embedBuilder.build());
        }

        if(isReplaced){
            ev.setMessage(message);
        }
    }
}
