package bg.fmi.chatserver.chat.spi;

import java.time.Instant;

public record NewMessage(String channelId, Instant timestamp, String author, String message) {
}
