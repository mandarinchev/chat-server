package bg.fmi.chatserver.chat;

import jakarta.annotation.Nullable;

public record Channel(
        String id,
        String name,
        @Nullable Message lastMessage
) {

    public Channel(String id, String name) {
        this(id, name, null);
    }
}
