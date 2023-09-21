package pro.gravit.launcher.client.gui.scenes.login;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import pro.gravit.launcher.client.gui.JavaFXApplication;
import pro.gravit.launcher.client.gui.helper.LookupHelper;

public class LoginAuthButtonComponent {
    private final JavaFXApplication application;
    private final Button button;
    private boolean isDisabled;
    private AuthButtonState state = AuthButtonState.UNACTIVE;

    public static enum AuthButtonState {
        ACTIVE("activeButton"), UNACTIVE("unactiveButton"), ERROR("errorButton");
        private final String styleClass;

        public String getStyleClass() {
            return styleClass;
        }

        AuthButtonState(String styleClass) {
            this.styleClass = styleClass;
        }
    }

    public LoginAuthButtonComponent(Button authButton, JavaFXApplication application,
            EventHandler<ActionEvent> eventHandler) {
        this.application = application;
        this.button = authButton;
        this.button.setOnAction(eventHandler);
    }

    public AuthButtonState getState() {
        return state;
    }

    public void setState(AuthButtonState state) {
        if(state == null) {
            throw new NullPointerException("State can't be null");
        }
        if(state == this.state) {
            return;
        }
        if(this.state != null) {
            button.getStyleClass().remove(this.state.getStyleClass());
        }
        button.getStyleClass().add(state.getStyleClass());
    }

    public String getText() {
        return button.getText();
    }

    public void setText(String text) {
        button.setText(text);
    }
}
