package info.kgeorgiy.vpn.server;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("/hello")
public class HelloView extends VerticalLayout {
    public HelloView() {
        final TextField name = new TextField("Name");
        final Button hello = new Button("Say hello");
        hello.addClickListener(event -> {
            add(new Paragraph("Hello, %s!".formatted(name.getValue())));
            name.clear();
        });

        final HorizontalLayout form = new HorizontalLayout(name, hello);
        form.setAlignItems(Alignment.BASELINE);

        add(new H1("Hello"), form);
    }
}
