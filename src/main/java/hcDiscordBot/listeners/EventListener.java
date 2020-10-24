package hcDiscordBot.listeners;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.form.element.ElementButton;
import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;
import cn.nukkit.form.response.FormResponseSimple;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.form.window.FormWindowSimple;
import cn.nukkit.scheduler.AsyncTask;
import hcDiscordBot.manager.AccountManager;
import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.manager.JDAManager;
import hcDiscordBot.manager.SubAccountManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import static hcDiscordBot.HcDiscordBot.PREFIX;

@SuppressWarnings("unchecked")
public class EventListener implements Listener{
	public static final int IS_YOU_MODAL_FORM = 8216;

	private final AccountManager accountManager;
	private final SubAccountManager subAccountManager;
	private final JDAManager jdaManager;
	private final HcDiscordBot plugin;

	public EventListener() {
		this.plugin = HcDiscordBot.INSTANCE;
		this.accountManager = plugin.getAccountManager();
		this.subAccountManager = plugin.getSubAccountManager();
		this.jdaManager = plugin.getJdaManager();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent ev) {
		Player player = ev.getPlayer();
		HashSet<String> todayPlayers = (HashSet<String>) plugin.todayDB.getHashSet("dc_today_pl");
		int todayMax = plugin.todayDB.getInt("dc_today_max");

		if (!todayPlayers.contains(player.getName())) {
			todayPlayers.add(player.getName());
			plugin.todayDB.put("dc_today_pl", todayPlayers);
		}

		int playersSize = plugin.getServer().getOnlinePlayers().size();
		if (todayMax < playersSize) {
			plugin.todayDB.put("dc_today_max", playersSize);
		}

		if ((int) plugin.serverData.getOrDefault("maxPlayers", 20) < playersSize) {
			plugin.serverData.put("maxPlayers", playersSize);

			TextChannel tc = plugin.getJdaManager().getTodayReportChannel();
			EmbedBuilder embed = new EmbedBuilder();
			StringBuilder sb = new StringBuilder();

			embed.setTitle("최고동접 " + playersSize + "명 감사합니다.");
			plugin.getServer().getOnlinePlayers().forEach((uuid, pl) -> {
				sb.append(pl.getName() + "님, ");
			});
			embed.addField("접속해 주신 분들", sb.toString(), false);

			tc.sendMessage(embed.build()).queue();
		}
		plugin.getServer().getScheduler().scheduleAsyncTask(plugin, new AsyncTask() {

			@Override
			public void onRun() {
				subAccountManager.checkSubAccount(ev.getPlayer());
			}
		});
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
			if(id == AccountManager.MAIN_FORM){
				/*ArrayList<Element> elements = new ArrayList<>();
				elements.add(new ElementInput("§b● §f디스코드 태그를 제외한 이름을 입력하여주세요.\n§b● §fHANCHO#7030 -> HANCHO"));
				FormWindowCustom form = new FormWindowCustom("§l§0디스코드 계정 연동", elements);
				player.showFormWindow(form, AccountManager.ENTER_NAME_LINK_FORM);*/
				this.showMyToken(player);
			}else if(id == AccountManager.SELECT_LINK_FORM){
				Server.getInstance().getScheduler().scheduleAsyncTask(this.plugin, new AsyncTask() {
					@Override
					public void onRun() {
						String target = response.getClickedButton().getText();
						String name = target.split("§b")[0];
						String tag = target.split("§r")[1];

						Member member = plugin.getJdaManager().getMainGuild().getMemberByTag(name, tag);
						plugin.getLogger().info(name + ":" + tag);

						if(member == null){
							FormWindowSimple formWindowSimple = new FormWindowSimple("§0오류", "§f§l해당 유저를 찾을 수 없습니다.\n다시 시도해보세요.");
							player.showFormWindow(formWindowSimple);
							return;
						}

						accountManager.linkAccount(player.getName(), member.getId());
						member.getUser().openPrivateChannel().queue((channel) -> {
							channel.sendMessage("초코서버와 계정을 연동해주셔서 감사합니다!\n인게임 닉네임 : " + player.getName() + ", 연동 일시 : " + HcDiscordBot.sdf.format(System.currentTimeMillis())).queue();
						});

						FormWindowSimple formWindowSimple = new FormWindowSimple("§0성공", "§b●§l§f 성공적으로 연동하였습니다.\n§b● §f이제 오프라인상태에서도 귓속말을 사용할 수 있어요.");
						player.showFormWindow(formWindowSimple);
					}
				});
			}
		}else if(ev.getWindow() instanceof FormWindowCustom){
			FormWindowCustom window = (FormWindowCustom) ev.getWindow();
			FormResponseCustom response = window.getResponse();

			if(id == AccountManager.ENTER_NAME_LINK_FORM){
				String targetName = response.getInputResponse(0);
				if(targetName.isEmpty()) return;

				Server.getInstance().getScheduler().scheduleAsyncTask(this.plugin, new AsyncTask() {
					@Override
					public void onRun() {
						ArrayList<Member> list = jdaManager.searchMemberByName(targetName);
						if(!player.isOnline()) return;

						String content = "§b● §f§l연동할 자신의 계정을 선택하세요.";
						ArrayList<ElementButton> buttons = new ArrayList<>();

						for(Member member : list){
							buttons.add(new ElementButton(  member.getUser().getName() + "§b:§r" + member.getUser().getDiscriminator()));
						}

						FormWindowSimple form = new FormWindowSimple("§0디스코드 계정 연동", content, buttons);
						player.showFormWindow(form, AccountManager.SELECT_LINK_FORM);
					}
				});
			}
		}else if(ev.getWindow() instanceof FormWindowModal){
			FormWindowModal window = (FormWindowModal) ev.getWindow();
			FormResponseModal response = window.getResponse();

			int clickedID = response.getClickedButtonId();
			if(id == IS_YOU_MODAL_FORM){
				if(clickedID == 0){
					this.accountManager.linkAccount(player.getName(), this.accountManager.getTokenByName(player.getName()));

					FormWindowSimple form = new FormWindowSimple("§0연동 완료", PREFIX + "성공적으로 연동되었습니다!" );
					player.showFormWindow(form );
				}else if(clickedID == 1){
					// TODO
				}
			}
		}
	}

	public void showMyToken(Player player){
		String token = this.accountManager.getTokenByName(player.getName());
		if(token == null){
			this.accountManager.resetToken(player.getName());
			token = this.accountManager.getTokenByName(player.getName());
		}

		HashMap<String, Object> data = this.accountManager.getTokenData(token);
		if(data.containsKey("isWaiting")){
			if((boolean) data.get("isWaiting")){
				FormWindowModal form = new FormWindowModal("§0디스코드 연동", PREFIX + "님이 맞으신가요?", "네", "아니요");
				player.showFormWindow(form, IS_YOU_MODAL_FORM);
				return;
			}
		}

		FormWindowSimple form = new FormWindowSimple("§0디스코드 연동", PREFIX + "아래 토큰을 초코서버 봇 개인 채팅으로 보내주세요! \n" + PREFIX + token);
		player.showFormWindow(form);
	}
}
