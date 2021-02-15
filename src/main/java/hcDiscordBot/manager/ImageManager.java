package hcDiscordBot.manager;

import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import hcDiscordBot.HcDiscordBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;

// TODO
public class ImageManager extends ListenerAdapter {
    public static final String IMAGE_PATH = "/var/www/html/discordAccountSync/";

    public AccountManager accountManager;
    public LinkedHashMap<String, Object> data;
    public String verifyTCID = "705971666769018930";

    private final HcDiscordBot plugin;

    public ImageManager() {
        this.plugin = HcDiscordBot.instant;
        Config imageConfig = new Config(plugin.getDataFolder().getAbsolutePath() + "/imageConfig.yml", Config.YAML);

        this.data = (LinkedHashMap<String, Object>) imageConfig.getAll();
    }

    public void userRequested(String userID, PrivateMessageReceivedEvent event) {

    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent ev) {
        AccountManager accountManager = plugin.getAccountManager();
        String userID = ev.getAuthor().getId();

        if (!ev.getMessage().getAttachments().isEmpty()) {
            if (!accountManager.isLinkedById(userID)) {
                ev.getChannel().sendMessage("사진은 서버에 업로드하려면 먼저 계정을 연동해야합니다!\n``^인증`` 명령어를 입력하세요.").queue();
                return;
            }

            List<Message.Attachment> attachments = ev.getMessage().getAttachments();
            Message.Attachment attachment = attachments.get(0);
            PrivateChannel privateChannel = ev.getChannel();

            if (!attachment.isImage()) return;
            Server.getInstance().getLogger().info(attachment.getFileExtension() + "ddddd");
            if (!("png".equals(attachment.getFileExtension())) && !("jpg".equals(attachment.getFileExtension()))) {
                privateChannel.sendMessage("이미지 확장자명은 png 또는 jpg 파일만 지원합니다.").queue();
                return;
            }

            if (attachment.getSize() / 1000000 > 3) {
                privateChannel.sendMessage("이미지 크기는 최대 3메가를 넘을 수 없습니다.").queue();
                return;
            }

            ev.getChannel().sendMessage("크기 : " + attachment.getSize() + "\n확장자 : " + attachment.getFileExtension() + "\n 이미지 ? :" + attachment.isImage() + "\n"
                    + attachment.getHeight()).queue();
            File directory = new File(plugin.getDataFolder().getAbsolutePath() + "/userImg/" + accountManager.getUserNameById(userID).toLowerCase() + "/");
            attachment.downloadToFile( directory.getAbsolutePath() + "test.png");
            ev.getChannel().sendMessage("서버에서 ``/이미지`` 를 입력해보세요.").queue();
        }
    }
}
