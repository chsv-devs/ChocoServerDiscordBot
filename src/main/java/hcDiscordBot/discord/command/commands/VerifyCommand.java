package hcDiscordBot.discord.command.commands;

import hcDiscordBot.HcDiscordBot;
import hcDiscordBot.discord.command.GeneralCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;

public class VerifyCommand extends GeneralCommand {
    private HcDiscordBot plugin;

    public VerifyCommand(String name) {
        super(name);

        this.plugin = HcDiscordBot.instant;
    }

    @Override
    public void onCommand(MessageChannel messageChannel, Message message) {
        if(!(messageChannel instanceof PrivateChannel)){
            messageChannel.sendMessage("봇과의 개인 채팅방에서 입력해주세요.").queue();
            return;
        }

        String token = plugin.getAccountManager().registerNewToken(message.getAuthor().getId());
        messageChannel.sendMessage("인증을 완료하려면 초코서버에 접속해서 채팅에 ```" + token + "```을 입력하세요!").queue();
    }
}
