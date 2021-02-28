package hcDiscordBot.manager;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.item.ItemItemFrame;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hancho.plugin.imageplugin.ImagePlugin;
import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.discord.entity.ImageCheckResult;
import hcDiscordBot.discord.entity.UserAddImageRequest;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

// TODO
public class ImageManager extends ListenerAdapter {
    public static final int MAIN_FORM = 48129;
    public static final HttpClient client = HttpClientBuilder.create().build();
    public static final Gson gson = new GsonBuilder().create();
    public LinkedHashMap<String, UserAddImageRequest> requestDataMap = new LinkedHashMap<>();
    public LinkedHashMap<String, ArrayList<Integer>> imageSizeDataMap;
    public LinkedHashMap<String, ArrayList<String>> placedImageLocationMap;
    public LinkedHashMap<String, String> imageIdMap;

    private final HcDiscordBot plugin;

    public ImageManager() {
        this.plugin = HcDiscordBot.instant;
        Config config = new Config(plugin.getDataFolder().getAbsolutePath() + "/imageConfig.yml", Config.YAML);

        this.imageSizeDataMap = new LinkedHashMap<>(config.get("imageSizeData", new LinkedHashMap<>()));
        this.placedImageLocationMap = new LinkedHashMap<>(config.get("placed", new LinkedHashMap<>()));
        this.imageIdMap = new LinkedHashMap<>(config.get("imageId", new LinkedHashMap<>()));
    }

    public void save() {
        Config config = new Config(plugin.getDataFolder().getAbsolutePath() + "/imageConfig.yml", Config.YAML);
        config.set("imageSizeData", this.imageSizeDataMap);
        config.set("placed", this.placedImageLocationMap);
        config.set("imageId", this.imageIdMap);
        config.save();
    }

    @Override
    public void onMessageReactionAdd(@Nonnull MessageReactionAddEvent event) {
        if(event.getChannel().getId().equals(plugin.getJdaManager().getTextChannelManager().getImageUploadedChannel().TEXT_CHANNEL_ID)){
            event.getTextChannel().retrieveMessageById(event.getMessageId()).queue((message) -> {
                String id = message.getContentRaw().split("_")[0];
                String playerName = id.split(",")[1];
                ArrayList<String> locationList = this.placedImageLocationMap.get(id);

                for (String stringLocation : locationList) {
                    String[] args = stringLocation.split(",");
                    int x = Integer.parseInt(args[0]);
                    int y = Integer.parseInt(args[1]);
                    int z = Integer.parseInt(args[2]);
                    Level level = plugin.getServer().getLevelByName(args[3]);

                    Location location = new Location(x, y, z, level);
                    try {
                        location.getChunk().load(false);
                    } catch (IOException e) {
                        plugin.getLogger().error(id + "를 처리할 수 없음", e);
                    }

                    BlockEntity entity = location.getLevel().getBlockEntity(location);
                    if(entity != null && entity instanceof BlockEntityItemFrame) {
                        ImagePlugin.breakImage((BlockEntityItemFrame) entity);
                    }
                }

                if(locationList.size() == 0) {
                    event.getTextChannel().sendMessage("이미지 제거를 시도하였으나 아직 이미지를 설치하지 않은 것 같습니다.").queue();
                }else{
                    event.getTextChannel().sendMessage("관리자의 요청으로 제거됨 :ok:").queue();
                    message.editMessage(message.getContentRaw() + "\n(제거됨)").queue();
                }
                plugin.getLogger().info("이미지 " + id + "가 " + event.getUser().getName() + "(디스코드)님에 의해 제거되었습니다.");
            });
        }
    }

    @Override
    public void onPrivateMessageReceived(PrivateMessageReceivedEvent ev) {
        AccountManager accountManager = plugin.getAccountManager();
        String userId = ev.getAuthor().getId();
        String msgContent = ev.getMessage().getContentRaw();
        String trimedContent = msgContent.trim();

        if (!ev.getMessage().getAttachments().isEmpty()) {
            if (!accountManager.isLinkedById(userId)) {
                ev.getChannel().sendMessage("사진은 서버에 업로드하려면 먼저 계정을 연동해야합니다!\n``^인증`` 명령어를 입력하세요.").queue();
                return;
            }

            List<Message.Attachment> attachments = ev.getMessage().getAttachments();
            Message.Attachment attachment = attachments.get(0);
            PrivateChannel privateChannel = ev.getChannel();

            if (!attachment.isImage()) return;
            if (!("png".equals(attachment.getFileExtension())) && !("jpg".equals(attachment.getFileExtension()))) {
                privateChannel.sendMessage("이미지 확장자명은 png 또는 jpg 파일만 지원합니다.").queue();
                return;
            }

            if (attachment.getSize() / 1000000 > 3) {
                privateChannel.sendMessage("이미지 크기는 최대 3메가를 넘을 수 없습니다.").queue();
                return;
            }

            File dir = getUserImgDir(plugin.getAccountManager().getUserNameById(userId));
            if(dir.exists()){
                if(dir.listFiles().length > 10){
                    privateChannel.sendMessage("한번에 최대 10개까지만 업로드 할 수 있습니다.\n서버에서 이미지를 설치하여 공간을 확보하세요!").queue();
                }
            }

            UserAddImageRequest request = new UserAddImageRequest();
            request.setAttachment(attachment);
            this.requestDataMap.put(userId, request);
            ev.getChannel().sendMessage("파일의 이름은``" + attachment.getFileName() + "``입니다. \n변경하려면 파일 이름 수정 후 재전송해주세요. (한글은 지원하지 않습니다)").queue();
            ev.getChannel().sendMessage("등록을 진행하려면, 설치될 액자의 크기를 입력해주세요.\n최대 크기는 가로 ``5``, 세로 ``5``블럭입니다.\n```ex) 5x3```").queue();
            File directory = new File(plugin.getDataFolder().getAbsolutePath() + "/userImg/" + accountManager.getUserNameById(userId).toLowerCase() + "/");
            attachment.downloadToFile(directory.getAbsolutePath() + "test.png");
        }

        if (this.requestDataMap.containsKey(userId) && trimedContent.length() < 4 && (trimedContent.contains("x") || trimedContent.contains("X"))) {
            ev.getChannel().sendMessage("요청을 처리 중").queue();

            try {
                UserAddImageRequest request = this.requestDataMap.get(userId);
                Message.Attachment attachment = request.getAttachment();
                String playerName = accountManager.getUserNameById(userId);
                String[] args = trimedContent.toLowerCase().split("x");
                int[] sizes = new int[2];

                sizes[0] = (int) Double.parseDouble(args[0]);
                sizes[1] = (int) Double.parseDouble(args[1]);
                if(sizes[0] < 1 || sizes[1] < 1){
                    ev.getChannel().sendMessage("크기는 최소 1블럭 이상이어야 합니다.").queue();
                    this.requestDataMap.remove(userId);
                    return;
                }
                request.setSize(sizes);

                ImageCheckResult imageCheckResult = checkImg(attachment.getUrl());
                if(!imageCheckResult.isSuccess()){
                    ev.getChannel().sendMessage("요청 처리 중 오류가 발생하였거나 서버로부터 거부되었습니다.").queue();
                    Server.getInstance().getLogger().warning(playerName + "(인게임 닉네임) 님으로 부터 요청된 이미지를 처리할 수 없음. (" + attachment.getUrl() + ")");
                    this.requestDataMap.remove(userId);
                    return;
                }else{
                    String imgId = ThreadLocalRandom.current().nextInt(1913) + "," + playerName;
                    this.imageIdMap.put(playerName + attachment.getFileName(), imgId);
                    plugin.getJdaManager().getTextChannelManager().getImageUploadedChannel()
                            .sendMessage(imgId + "_ 이미지가 " + playerName + "님에 의해 등록되었습니다.\n" +
                                    "||" + attachment.getUrl() + "||\n```이미지 분석 결과\n일반적인 이미지일 확률 : " + (imageCheckResult.getNormal() * 100)
                                    + "%\n노출이 포함된 이미지일 확률 : " + (imageCheckResult.getSoft() * 100)
                                    + "%\n성인 이미지일 확률 : " + (imageCheckResult.getAdult() * 100)
                                    + "%\n\n대부분의 경우 AI에 의해 필터링되어 이미지 등록이 거부되지만 Adult값이 높은경우 여기에 이모션을 달아 제거시킬 수 있습니다.```");
                }

                File dir = getUserImgDir(playerName);
                dir.mkdirs();

                attachment.downloadToFile(dir.getAbsolutePath() + "/" + attachment.getFileName())
                        .thenAcceptAsync((file) -> {
                            ArrayList<Integer> sizeList = new ArrayList<>();
                            for (int size : sizes) {
                                sizeList.add(size);
                            }

                            imageSizeDataMap.put(playerName + "-" + attachment.getFileName(), sizeList);
                            ev.getChannel().sendMessage("작업이 성공적으로 완료되었습니다. 서버 내에서 ``/이미지`` 명령어를 입력해보세요!").queue();
                        });

            } catch (Exception e) {
                ev.getChannel().sendMessage("처리중 오류가 발생하였습니다").queue();
                Server.getInstance().getLogger().info(ev.getAuthor().getName() + "님의 이미지 처리 도중 예외 발생", e);
            } finally {
                this.requestDataMap.remove(userId);
            }
        }
    }

    public ImageCheckResult checkImg(String imgUrl) {
        ImageCheckResult imageCheckResult = new ImageCheckResult();

        try {
            HttpPost postRequest = new HttpPost("https://dapi.kakao.com/v2/vision/adult/detect");
            postRequest.setHeader("Authorization", "KakaoAK " + plugin.getConfig().get("kakaoApiKey", "")); // token 이용시
            postRequest.setHeader("Content-Type", "application/x-www-form-urlencoded");
            postRequest.setEntity(new StringEntity("image_url=" + imgUrl + ""));

            HttpResponse response = client.execute(postRequest);

            if (response.getStatusLine().getStatusCode() == 200) {
                ResponseHandler<String> handler = new BasicResponseHandler();
                String body = handler.handleResponse(response);

                LinkedHashMap<String, Object> result
                        = new LinkedHashMap((Map) gson.fromJson(body, LinkedHashMap.class).get("result"));
                double normal = (double) result.get("normal");
                double soft = (double) result.get("soft");
                double adult = (double) result.get("adult");

                imageCheckResult.setNormal(normal);
                imageCheckResult.setSoft(soft);
                imageCheckResult.setAdult(adult);
                imageCheckResult.setSuccess(normal > 0.6);

                return imageCheckResult;
            } else {
                ResponseHandler<String> handler = new BasicResponseHandler();
                String body = handler.handleResponse(response);
                Server.getInstance().getLogger().warning(body);
                return imageCheckResult;
            }
        } catch (Exception e){
            Server.getInstance().getLogger().error("", e);
            return imageCheckResult;
        }
    }

    // block
    public ArrayList<Integer> getImageSize(String playerName, String fileName){
        playerName = playerName.toLowerCase();
        return this.imageSizeDataMap.get(playerName + "-" + fileName);
    }

    public File getUserImgDir(String name){
        name = name.toLowerCase();
        return new File(plugin.getDataFolder().getAbsolutePath() + "/userImg/" + name + "/");
    }

    public void giveImgFrameItem(Player player, String filename){
        ItemItemFrame itemItemFrame = new ItemItemFrame();
        itemItemFrame.setCount(1);
        itemItemFrame.setCustomName("이미지 - " + filename);

        CompoundTag tag = itemItemFrame.getNamedTag();
        tag.putString("img_owner", player.getName());
        tag.putString("img_path", getUserImgDir(player.getName()).getAbsolutePath() + "/" + filename);
        tag.putString("img_name", filename);
        itemItemFrame.setNamedTag(tag);

        player.getInventory().addItem(itemItemFrame);
        player.sendMessage(HcDiscordBot.PREFIX + "아이템이 지급되엇습니다.");
    }

    public void showImgListForm(Player player){
        Server.getInstance().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
            @Override
            public void onRun() {
                ArrayList<ElementButton> buttons = new ArrayList<>();
                File dir = getUserImgDir(player.getName());
                File[] files = dir.exists() ? dir.listFiles() : new File[0];
                int fileCount = files.length;

                for (File img : files) {
                    buttons.add(new ElementButton(img.getName()));
                }

                FormWindowSimple form = new FormWindowSimple("이미지"
                        , HcDiscordBot.PREFIX + "등록된 이미지 " + fileCount + "개가 존재합니다.\n" +
                        HcDiscordBot.PREFIX + "이미지 등록방법은 /인증 명령어를 입력해주세요."
                        , buttons);
                player.showFormWindow(form, MAIN_FORM);
            }
        });
    }
}
