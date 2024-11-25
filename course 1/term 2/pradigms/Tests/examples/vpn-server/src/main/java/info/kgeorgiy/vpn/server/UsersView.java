package info.kgeorgiy.vpn.server;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldBase;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

@Route("")
public class UsersView extends VerticalLayout {
    private static final Logger log = LoggerFactory.getLogger(UsersView.class);

    private final UserRepository users;
    private final Grid<User> userGrid;

    public UsersView(final UserRepository users, final PasswordEncoder passwordEncoder) {
        this.users = users;

        userGrid = createUserGrid();
        refreshUserGrid();

        add(new H1("Users"), createForm(passwordEncoder), userGrid);
    }

    private Grid<User> createUserGrid() {
        final Grid<User> grid = new Grid<>(User.class);
        grid.removeAllColumns();
        grid.addColumn("username").setHeader("Username");
        grid.addColumn("name").setHeader("Name");
        grid.addColumn(new ComponentRenderer<>(Button::new, (button, user) -> {
                    button.setText("Remove");
                    button.addClickListener(event -> {
                        final ConfirmDialog confirmDialog = new ConfirmDialog();
                        confirmDialog.setHeader("Remove user");
                        confirmDialog.setText("Remove user %s (id=%s)?".formatted(user.getName(), user.getId()));
                        confirmDialog.setCancelable(true);
                        confirmDialog.addConfirmListener(confirmation -> {
                            log.info("Removing user {}", user);
                            users.delete(user);
                            refreshUserGrid();
                        });
                        confirmDialog.open();
                    });
                }))
                .setHeader("Actions");
        return grid;
    }

    private void refreshUserGrid() {
        userGrid.setItems(users.findAll());
    }

    private Component createForm(final PasswordEncoder passwordEncoder) {
        final TextField username = new TextField("Username");
        final TextField name = new TextField("Name");
        final PasswordField password = new PasswordField("Password");

        final Binder<User> binder = new Binder<>(User.class);
        bindField(binder, username, "username", 3);
        bindField(binder, name, "name", 0);
        bindField(binder, password, "password", 8);

        final Button add = new Button("Add");
        add.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        add.addClickShortcut(Key.ENTER);
        add.addClickListener(event -> {
            final User user = new User();
            try {
                binder.writeBean(user);
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                users.save(user);
                binder.readBean(new User());
                refreshUserGrid();
            } catch (ValidationException e) {
                log.debug("Validation error {}", e.getMessage());
            }
        });

        final HorizontalLayout form = new HorizontalLayout(username, name, password, add);
        form.setAlignItems(Alignment.BASELINE);
        return form;
    }

    private static void bindField(Binder<User> binder, TextFieldBase<?, String> field, String property, int minLength) {
        binder.forField(field)
                .withValidator(
                        value -> value.length() >= minLength,
                        "Should contain at least %s characters".formatted(minLength)
                )
                .bind(property);
    }
}
