package hcDiscordBot.command;

import hcDiscordBot.HcDiscordBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

public class BaseCommand {
    public HcDiscordBot plugin;

    private final String commandName;

    public BaseCommand(String name){
        this.commandName = name;
        this.plugin = HcDiscordBot.INSTANCE;
    }

    public void onCommand(TextChannel textChannel, Message message){

    }

    public String getCommandName() {
        return commandName;
    }
}
