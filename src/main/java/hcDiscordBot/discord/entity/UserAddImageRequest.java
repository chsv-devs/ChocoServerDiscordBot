package hcDiscordBot.discord.entity;

import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;

@Getter
@Setter
public class UserAddImageRequest {
    private Message.Attachment attachment;
    private int[] size;
}
