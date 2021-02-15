package hcDiscordBot.discord.command;

import hcDiscordBot.HcDiscordBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;

public class GeneralCommand {
    public HcDiscordBot plugin;

    private final String commandName;

    public GeneralCommand(String name){
        this.commandName = name;
        this.plugin = HcDiscordBot.instant;
    }

    public void onCommand(MessageChannel textChannel, Message message){

    }

    public String getCommandName() {
        return commandName;
    }
}
