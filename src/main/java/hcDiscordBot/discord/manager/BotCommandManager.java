package hcDiscordBot.discord.manager;

import cn.nukkit.Server;
import hcDiscordBot.discord.command.*;
import hcDiscordBot.discord.command.commands.RebootCommand;
import hcDiscordBot.discord.command.commands.SeeMoneyCommand;
import hcDiscordBot.discord.command.commands.ServerStatusCommand;
import hcDiscordBot.discord.command.commands.VerifyCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;

import java.util.LinkedHashMap;

public class BotCommandManager {
    public LinkedHashMap<String, GeneralCommand> commandMap = new LinkedHashMap<>();

    public BotCommandManager(){
        initCommands();
    }

    public void initCommands(){
        addCommand(new ServerStatusCommand("서버상태"));
        addCommand(new SeeMoneyCommand("돈"));
        addCommand(new RebootCommand("재부팅"));
        addCommand(new VerifyCommand("인증"));
    }

    public void addCommand(GeneralCommand command){
        this.commandMap.put(command.getCommandName(), command);
    }

    public boolean processMessage(MessageChannel messageChannel, Message message){
        String commandName = message.getContentRaw().substring(1).split(" ")[0];
        GeneralCommand command = this.commandMap.get(commandName);

        Server.getInstance().getLogger().info(commandName);
        if(command != null){
            command.onCommand(messageChannel, message);
            return true;
        }else{
            return false;
        }
    }
}
