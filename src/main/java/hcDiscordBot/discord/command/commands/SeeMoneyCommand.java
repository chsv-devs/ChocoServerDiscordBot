package hcDiscordBot.discord.command.commands;

import cn.nukkit.IPlayer;
import hcDiscordBot.discord.command.GeneralCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class SeeMoneyCommand extends GeneralCommand {
    public SeeMoneyCommand(String name) {
        super(name);
    }

    @Override
    public void onCommand(MessageChannel messageChannel, Message message) {
        String messageContent = message.getContentRaw();
        String targetPlayerName = messageContent.substring(Math.min(messageContent.length(), 3));

        IPlayer player = plugin.getServer().getOfflinePlayer(targetPlayerName);
        if (player == null) {
            messageChannel.sendMessage("해당 플레이어가 존재하지 않습니다.").queue();
            return;
        }
        messageChannel.sendMessage(plugin.getApiManager().getEconomyAPI().myMoney(player) + "원").queue();
    }
}
