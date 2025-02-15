package bg.fmi.chatserver.views.lobby;

import bg.fmi.chatserver.chat.Channel;
import bg.fmi.chatserver.chat.ChatService;
import bg.fmi.chatserver.security.Roles;
import bg.fmi.chatserver.views.MainLayout;
import bg.fmi.chatserver.views.channel.ChannelView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Lobby")
@PermitAll
public class LobbyView extends VerticalLayout {

    private final ChatService chatService;
    private final AuthenticationContext authenticationContext;

    private final VirtualList<Channel> channels;
    private final TextField channelNameField;
    private final Button addChannelButton;

    public LobbyView(ChatService chatService, AuthenticationContext authenticationContext) {
        this.chatService = chatService;
        this.authenticationContext = authenticationContext;

        setSizeFull();

        this.channels = new VirtualList<>();
        add(this.channels);
        expand(this.channels);
        this.channels.setRenderer(new ComponentRenderer<>(this::createChannelComponent));

        this.channelNameField = new TextField();
        this.channelNameField.setPlaceholder("Enter new channel name.");

        this.addChannelButton = new Button("Add Channel");
        this.addChannelButton.setDisableOnClick(true);
        this.addChannelButton.addClickListener(e -> addChannel());

        if (this.authenticationContext.hasRole(Roles.ADMIN)) {
            HorizontalLayout toolbar = new HorizontalLayout(this.channelNameField, this.addChannelButton);
            toolbar.setWidthFull();
            toolbar.expand(this.channelNameField);
            add(toolbar);
        }
    }

    private void refreshChannels() {
        this.channels.setItems(this.chatService.channels());
    }

    private void addChannel() {
        try {
            String channelName = this.channelNameField.getValue();
            if (!channelName.isBlank()) {
                this.chatService.createChannel(channelName);
                this.channelNameField.clear();
                refreshChannels();
            }
        } finally {
            this.addChannelButton.setEnabled(true);
        }
    }

    private Component createChannelComponent(Channel channel) {
        return new RouterLink(channel.name(), ChannelView.class, channel.id());
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        refreshChannels();
    }
}
