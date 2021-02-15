package hcDiscordBot.discord.command.commands;

import hcDiscordBot.discord.command.GeneralCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

public class RebootCommand extends GeneralCommand {
    public RebootCommand(String name) {
        super(name);
    }

    @Override
    public void onCommand(MessageChannel messageChannel, Message message) {
        messageChannel.sendMessage("서버 다시 시작하는 중...").queue();
        plugin.getServer().shutdown();
    }
}
