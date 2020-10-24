package hcDiscordBot.command.commands;

import hcDiscordBot.command.BaseCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class RebootCommand extends BaseCommand {
    public RebootCommand(String name) {
        super(name);
    }

    @Override
    public void onCommand(TextChannel textChannel, Message message) {
        textChannel.sendMessage("서버 다시 시작하는 중...").queue();
        plugin.getServer().shutdown();
    }
}
