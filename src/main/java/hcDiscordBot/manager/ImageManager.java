package hcDiscordBot.manager;

import hcDiscordBot.HcDiscordBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.LinkedHashMap;
import java.util.List;

// TODO
public class ImageManager extends ListenerAdapter{
    public static final String IMAGE_PATH = "/var/www/html/discordAccountSync/";

    public AccountManager accountManager;
    public LinkedHashMap<String, Object> data;
    public String verifyTCID = "705971666769018930";

    private final HcDiscordBot plugin;

    public ImageManager(LinkedHashMap<String, Object> data){
        this.plugin = HcDiscordBot.INSTANCE;
        this.data = data;

        this.accountManager = this.plugin.getAccountManager();
    }

    public void userRequested(String userID, PrivateMessageReceivedEvent event){

    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent ev){
        if(this.accountManager.isExistToken(ev.getMessage().getContentRaw())){
            String token = ev.getMessage().getContentRaw();
            //this.plugin.accountManager.linkAccount((String) this.plugin.accountManager.getTokenData(token).get("name"), ev.getAuthor().getId());
            this.accountManager.toggleAwaitState(token);
            ev.getChannel().sendMessage("서버에 접속하여 ``/디스코드`` 명령어를 입력해보세요!").queue();
        }

        if(!ev.getMessage().getAttachments().isEmpty()){
            if(!this.accountManager.isLinkedById(ev.getAuthor().getId())){
                ev.getChannel().sendMessage("이미지 업로드 기능을 사용하려면 먼저 연동해야합니다!\n서버에서 ``/디스코드`` 를 입력해보세요.").queue();
                return;
            }

            List<Message.Attachment> attachments = ev.getMessage().getAttachments();
            Message.Attachment attachment = attachments.get(0);
            PrivateChannel pc = ev.getChannel();

            if(!attachment.isImage()) return;
            if(!("png".equals(attachment.getFileExtension())) || !("jpg".equals(attachment.getFileExtension()))){
                pc.sendMessage("이미지 확장자명은 png 또는 jpg 파일만 지원합니다.").queue();
                return;
            }

            if(attachment.getSize()/1000000 > 3){
                pc.sendMessage("이미지 크기는 최대 3메가를 넘을 수 없습니다.").queue();
                return;
            }

            ev.getChannel().sendMessage("크기 : " + attachment.getSize() + "\n확장자 : " + attachment.getFileExtension() + "\n 이미지 ? :" + attachment.isImage() + "\n"
                    + attachment.getHeight()).queue();
            plugin.getLogger().warning("크기 : " + attachment.getSize() + "\n확장자 : " + attachment.getFileExtension() + "\n 이미지 ? :" + attachment.isImage() + "\n"
                    + attachment.getHeight());
        }
    }
}
