package bg.fmi.chatserver.views;

import bg.fmi.chatserver.views.lobby.LobbyView;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    private final AuthenticationContext authenticationContext;

    private H2 viewTitle;

    public MainLayout(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;

        setPrimarySection(Section.DRAWER);

        addNavbarContent();
        addDrawerContent();
    }

    private void addNavbarContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        toggle.setTooltipText("Toggle menu");

        this.viewTitle = new H2();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.NONE,
                LumoUtility.Flex.GROW);

        Button logoutButton = new Button("Logout " + this.authenticationContext.getPrincipalName().orElse(""),
                VaadinIcon.SIGN_OUT.create(),
                e -> this.authenticationContext.logout());

        Header header = new Header(toggle, viewTitle, logoutButton);
        header.addClassNames(LumoUtility.AlignItems.CENTER,
                LumoUtility.Display.FLEX,
                LumoUtility.Padding.End.MEDIUM,
                LumoUtility.Width.FULL);

        addToNavbar(false, header);
    }

    private void addDrawerContent() {
        Span appName = new Span("Chat-Mat");
        appName.addClassNames(LumoUtility.AlignItems.CENTER,
                LumoUtility.Display.FLEX,
                LumoUtility.FontSize.LARGE,
                LumoUtility.FontWeight.SEMIBOLD,
                LumoUtility.Height.XLARGE,
                LumoUtility.Padding.Horizontal.MEDIUM);
        addToDrawer(appName, new Scroller(createSideNav()));
    }

    private SideNav createSideNav() {
        SideNav sideNav = new SideNav();
        sideNav.addItem(new SideNavItem("Lobby", LobbyView.class, VaadinIcon.BUILDING.create()));

        return sideNav;
    }

    private String getCurrentPageTitle() {
        if (getContent() == null) {
            return "";
        }
        else if (getContent() instanceof HasDynamicTitle titleHolder) {
            return titleHolder.getPageTitle();
        }
        else {
            PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
            return title == null ? "" : title.value();
        }
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        viewTitle.setText(getCurrentPageTitle());
    }
}
