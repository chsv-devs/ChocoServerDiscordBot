package hcDiscordBot.command.commands;

import cn.nukkit.IPlayer;
import cn.nukkit.Server;
import hcDiscordBot.command.BaseCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class SeeMoneyCommand extends BaseCommand {
    public SeeMoneyCommand(String name) {
        super(name);
    }

    @Override
    public void onCommand(TextChannel tc, Message message) {
        String messageContent = message.getContentRaw();
        String targetPlayerName = messageContent.substring(Math.min(messageContent.length(), 3));

        IPlayer player = plugin.getServer().getOfflinePlayer(targetPlayerName);
        if (player == null) {
            tc.sendMessage("해당 플레이어가 존재하지 않습니다.").queue();
            return;
        }
        tc.sendMessage(plugin.economyAPI.myMoney(player) + "원").queue();
    }
}
