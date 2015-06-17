package eu.riscoss.client.ui;

import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class GlassPanel extends SimplePanel implements NativePreviewHandler {

    private Label text;

    public GlassPanel() {
        setStyleName("glass-panel");
        add(text = new Label());
    }

    public void setText(String text) {
        this.text.setText(text);
    }

    @Override
    public void onPreviewNativeEvent(NativePreviewEvent event) {
        event.consume();
        event.cancel();
    }
}