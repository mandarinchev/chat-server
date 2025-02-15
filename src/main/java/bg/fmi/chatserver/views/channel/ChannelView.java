package bg.fmi.chatserver.views.channel;

import bg.fmi.chatserver.chat.Channel;
import bg.fmi.chatserver.chat.ChatService;
import bg.fmi.chatserver.chat.Message;
import bg.fmi.chatserver.util.LimitedSortedAppendOnlyList;
import bg.fmi.chatserver.views.MainLayout;
import bg.fmi.chatserver.views.lobby.LobbyView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;
import reactor.core.Disposable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Route(value = "channel", layout = MainLayout.class)
@PermitAll
public class ChannelView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {

    private final ChatService chatService;
    private final MessageList messageList;
    private final AuthenticationContext authenticationContext;

    private String channelId;
    private String channelName;

    private static final int HISTORY_SIZE = 20;
    private final LimitedSortedAppendOnlyList<Message> receivedMessages;

    public ChannelView(ChatService chatService, AuthenticationContext authenticationContext) {
        this.chatService = chatService;
        this.authenticationContext = authenticationContext;
        this.receivedMessages = new LimitedSortedAppendOnlyList<>(
                HISTORY_SIZE,
                Comparator.comparing(Message::sequenceNumber)
        );

        setSizeFull();
        this.messageList = new MessageList();
        this.messageList.setSizeFull();
        for (MessageListItem message : messageList.getItems()) {
//            if(message.getUserName().equals(authenticationContext.getPrincipalName().orElse(""))) {
                message.addClassNames(LumoUtility.AlignItems.END,
                        LumoUtility.AlignContent.END,
                        LumoUtility.AlignSelf.END,
                        LumoUtility.JustifyContent.END);
            }
//        }
        add(this.messageList);

        MessageInput messageInput = new MessageInput();
        messageInput.addSubmitListener(e -> {
            sendMessage(e.getValue());
        });
        messageInput.setWidthFull();
        add(messageInput);
    }

    private void sendMessage(String message) {
        if (!message.isBlank()) {
            this.chatService.postMessage(this.channelId, message);
        }
    }

    private void receiveMessages(List<Message> messages) {
        this.receivedMessages.addAll(messages);
        getUI().ifPresent(ui -> ui.access(() -> {
            this.messageList.setItems(this.receivedMessages.stream()
                    .map(this::createMessageListItem)
                    .toList());
        }));
    }

    private Disposable subscribe() {
        Disposable subscription = this.chatService
                .liveMessages(channelId)
                .subscribe(this::receiveMessages);
        String lastSeenMessageId = this.receivedMessages.getLast().map(Message::messageId).orElse(null);
        receiveMessages(this.chatService.messageHistory(this.channelId, HISTORY_SIZE, lastSeenMessageId));
        return subscription;
    }

    private MessageListItem createMessageListItem(Message message) {
        MessageListItem item = new MessageListItem();
        item.setText(message.message());
        item.setTime(message.timestamp());
        item.setUserName(message.author());
        return item;
    }

    @Override
    public void setParameter(BeforeEvent event, String channelId) {
        Optional<Channel> channel = this.chatService.channel(channelId);
        if (this.chatService.channel(channelId).isEmpty()) {
            event.forwardTo(LobbyView.class);
        }
        else {
            this.channelId = channelId;
            this.channelName = channel.get().name();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        Disposable subscription = subscribe();
        addDetachListener(e -> subscription.dispose());
    }

    @Override
    public String getPageTitle() {
        return this.channelName;
    }
}
