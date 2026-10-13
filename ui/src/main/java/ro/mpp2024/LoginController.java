package ro.mpp2024;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private IMotociclismServices service;

    public void setService(IMotociclismServices service) {
        this.service = service;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Introduceți username și parola!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Parent root = loader.load();
            MainController mainCtrl = loader.getController();
            mainCtrl.setService(service);

            User user = service.login(username, password, mainCtrl);
            mainCtrl.setUser(user);

            Stage stage = new Stage();
            stage.setTitle("Motociclism");
            stage.setScene(new Scene(root));
            stage.show();

            Stage loginStage = (Stage) usernameField.getScene().getWindow();
            loginStage.close();

        } catch (MotociclismException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Eroare la deschiderea aplicatiei: " + e.getMessage());
        }
    }
}