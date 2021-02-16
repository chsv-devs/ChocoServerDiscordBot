package hcDiscordBot.minecraft.listeners;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.level.Location;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.scheduler.AsyncTask;
import hancho.plugin.imageplugin.ImagePlugin;
import hancho.plugin.imageplugin.entity.RequestInformation;
import hcDiscordBot.discord.textchannel.DiscordTextChannel;
import hcDiscordBot.manager.AccountManager;
import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.discord.manager.JDAManager;
import hcDiscordBot.manager.ApiManager;
import hcDiscordBot.manager.ImageManager;
import hcDiscordBot.minecraft.SubAccountManager;
import net.dv8tion.jda.api.EmbedBuilder;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

@SuppressWarnings("unchecked")
public class MinecraftEventListener implements Listener{
	public static final int IS_YOU_MODAL_FORM = 8216;

	private final AccountManager accountManager;
	private final SubAccountManager subAccountManager;
	private final JDAManager jdaManager;
	private final HcDiscordBot plugin;

	public MinecraftEventListener() {
		this.plugin = HcDiscordBot.instant;
		this.accountManager = plugin.getAccountManager();
		this.subAccountManager = plugin.getSubAccountManager();
		this.jdaManager = plugin.getJdaManager();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent ev) {
		Player player = ev.getPlayer();
		ApiManager apiManager = this.plugin.getApiManager();

		HashSet<String> todayPlayers = (HashSet<String>) apiManager.todayDB.getHashSet("dc_today_pl");
		int todayMax = apiManager.getTodayDB().getInt("dc_today_max");

		if (!todayPlayers.contains(player.getName())) {
			todayPlayers.add(player.getName());
			apiManager.getTodayDB().put("dc_today_pl", todayPlayers);
		}

		int playersSize = plugin.getServer().getOnlinePlayers().size();
		if (todayMax < playersSize) {
			apiManager.getTodayDB().put("dc_today_max", playersSize);
		}

		if ((int) plugin.serverData.getOrDefault("maxPlayers", 20) < playersSize) {
			plugin.serverData.put("maxPlayers", playersSize);

			DiscordTextChannel tc = plugin.getJdaManager().getTextChannelManager().getTodayReportTextChannel();
			EmbedBuilder embed = new EmbedBuilder();
			StringBuilder sb = new StringBuilder();

			embed.setTitle("최고동접 " + playersSize + "명 감사합니다.");
			plugin.getServer().getOnlinePlayers().forEach((uuid, pl) -> {
				sb.append(pl.getName() + "님, ");
			});
			embed.addField("접속해 주신 분들", sb.toString(), false);

			tc.sendEmbedMessage(embed.build()).queue();
		}
		plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {

			@Override
			public void onRun() {
				subAccountManager.checkSubAccount(ev.getPlayer());
			}
		});
	}

	@EventHandler
	public void onPlaceBlock(BlockPlaceEvent ev){
		Block block = ev.getBlock();
		Player player = ev.getPlayer();

		if(block.getId() == BlockID.ITEM_FRAME_BLOCK){
			CompoundTag tag = ev.getItem().getNamedTag();
			if(tag != null && tag.exist("img_path")){
				ev.setCancelled();

				plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {
					@Override
					public void onRun() {
						try {
							player.sendMessage(HcDiscordBot.PREFIX + "이미지가 처리중입니다.");
							ArrayList<Integer> size = plugin.getImageManager().getImageSize(player.getName(), tag.getString("img_name"));
							RequestInformation info = new RequestInformation();
							String imageId;

							info.filePath = tag.getString("img_path");
							info.putData("img_owner", ev.getPlayer().getName());
							info.pos1 = ev.getBlock().getLocation();
							info.face = player.getDirection().getOpposite();
							imageId = plugin.getImageManager().imageIdMap.get(player.getName().toLowerCase() + FilenameUtils.getName(info.filePath));

							if (player.getDirection().getHorizontalIndex() == 0 || player.getDirection().getHorizontalIndex() == 2) {
								info.pos2 = info.pos1.add(size.get(0), size.get(1), 0);
							} else {
								info.pos2 = info.pos1.add(0, size.get(1), size.get(0));
							}

							ImagePlugin.placeImage(info, false);
							if(player.getGamemode() != 1) {
								File imgFile = new File(info.filePath);
								imgFile.delete();
							}

							ArrayList<String> locationList = plugin.getImageManager().placedImageLocationMap.getOrDefault(imageId, new ArrayList<>());
							Location blockLocation = block.getLocation();
							String locationHash
									= blockLocation.getFloorX() + "," +
									blockLocation.getFloorY() + "," +
									blockLocation.getFloorZ() + "," +
									blockLocation.getLevel().getName();
							locationList.add(locationHash);
							plugin.getImageManager().placedImageLocationMap.put(imageId, locationList);

							player.sendMessage(HcDiscordBot.PREFIX + "완료되었습니다. 이미지가 안 보일경우 잠시만 기다려주세요.");
						}catch (FileNotFoundException e){
							player.sendMessage(HcDiscordBot.PREFIX + "이미 설치되었던 이미지입니다. 재등록 해주세요.");
						}catch (Exception e){
							plugin.getLogger().error(player.getName() + "님의 요청 처리 중 예외 발생", e);
							player.sendMessage(HcDiscordBot.PREFIX + "처리 도중 알 수 없는 오류가 발생하였습니다.");
						}
					}
				});
			}
		}
	}

	@EventHandler
	public void onPlayerFormResponded(PlayerFormRespondedEvent ev){
		if(ev.getWindow() == null) return;
		if(ev.getResponse() == null) return;
		Player player = ev.getPlayer();

		int id = ev.getFormID();
		if(ev.getWindow() instanceof FormWindowSimple){
			FormWindowSimple window = (FormWindowSimple) ev.getWindow();
			FormResponseSimple response = window.getResponse();
			ElementButton clickedButton = response.getClickedButton();

			if(id == ImageManager.MAIN_FORM){
				plugin.getImageManager().giveImgFrameItem(player, clickedButton.getText());
			}
		}else if(ev.getWindow() instanceof FormWindowCustom){
			FormWindowCustom window = (FormWindowCustom) ev.getWindow();
			FormResponseCustom response = window.getResponse();


		}else if(ev.getWindow() instanceof FormWindowModal){
			FormWindowModal window = (FormWindowModal) ev.getWindow();
			FormResponseModal response = window.getResponse();

			int clickedID = response.getClickedButtonId();

		}
	}
}
