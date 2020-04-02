package hancho.plugin.nukkit.discordapi;

import javax.security.auth.login.LoginException;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.Config;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

public class discordapi extends PluginBase implements Listener{
	public JDA jda;
	public JDABuilder jb;
	
	public void onEnable() {
		saveDefaultConfig();
		Config config = getConfig();
		String token = config.getString("token");
		if(token.equals("")) {
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		}
		jb = new JDABuilder(AccountType.BOT);
		jb.setAutoReconnect(true);
		jb.setStatus(OnlineStatus.ONLINE);
		jb.setToken(token);
		jb.setActivity(Activity.watching("시즌3 시범운영"));
		jb.addEventListeners(new discordListener(this));
		try {
			jda = jb.build();
		}catch(LoginException e) {
			e.printStackTrace();
		}
		
		
		
		this.getServer().getPluginManager().registerEvents(this, this);
	}
	public void onDisable() {
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent ev) {
		//jda.getGuildById(508167852042485760L).getTextChannelById(590963879190986752L).sendMessage("테스트입니다." + ev.getPlayer().getName() + "님이 접속").queue();
		jda.getPresence().setActivity(Activity.playing((this.getServer().getOnlinePlayers().size()) + "명 온라인"));
		
	}
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent ev) {
		jda.getPresence().setActivity(Activity.playing((this.getServer().getOnlinePlayers().size() - 1) + "명 온라인"));
	}
	
	public void scheduling() {
		this.getServer().getScheduler().scheduleRepeatingTask(this, new AsyncTask() {
			
			@Override
			public void onRun() {
				// TODO Auto-generated method stub
				
			}
		}, 20 * 10);
	}
}
