package hcDiscordBot.manager;

import cn.nukkit.Server;
import hcDiscordBot.command.BaseCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.LinkedHashMap;

public class CommandManager {
    public LinkedHashMap<String, BaseCommand> commandMap = new LinkedHashMap<>();

    public void addCommand(BaseCommand command){
        this.commandMap.put(command.getCommandName(), command);
    }

    public boolean processMessage(TextChannel textChannel, Message message){
        String commandName = message.getContentRaw().substring(1).split(" ")[0];
        BaseCommand command = this.commandMap.get(commandName);

        Server.getInstance().getLogger().info(commandName);
        if(command != null){
            command.onCommand(textChannel, message);
            return true;
        }else{
            return false;
        }
    }
}
