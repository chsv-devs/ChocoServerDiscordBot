package hcDiscordBot.discord.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageCheckResult {
    private boolean isSuccess = false;
    private double normal, soft, adult;
}
