package bg.fmi.chatserver.chat.spi;

import bg.fmi.chatserver.chat.Channel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository {

    List<Channel> findAll();

    Channel save(NewChannel newChannel);

    Optional<Channel> findById(String channelId);

    boolean exists(String channelId);
}
