package aydin.firebasedemo;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PrimaryController {

    @FXML private TextField emailTextField;
    @FXML private PasswordField passwordTextField;
    @FXML private TextArea outputTextArea;

    // Firebase Console → Project settings → General → Web API key
    private static final String API_KEY = "AIzaSyCbu3qZVnMcyu-L-PEh2IdtVfb6h2ArHmM";

    @FXML
    void registerButtonClicked() {
        outputTextArea.setText("Registering…");
        new Thread(() -> {
            try {
                String email = emailTextField.getText().trim();
                String pass  = passwordTextField.getText().trim();

                UserRecord.CreateRequest req = new UserRecord.CreateRequest()
                        .setEmail(email)
                        .setEmailVerified(false)
                        .setPassword(pass)
                        .setDisabled(false);

                UserRecord user = DemoApp.fauth.createUser(req);
                String msg = "Created user UID: " + user.getUid();
                javafx.application.Platform.runLater(() -> outputTextArea.setText(msg));
            } catch (FirebaseAuthException e) {
                javafx.application.Platform.runLater(() -> outputTextArea.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    // Sign in (credential check). On success → go to data screen.
    @FXML
    void switchToSecondary() throws IOException {
        outputTextArea.setText("Signing in…");
        new Thread(() -> {
            try {
                String email = emailTextField.getText().trim();
                String pass  = passwordTextField.getText().trim();

                String url  = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=" + API_KEY;
                String body = """
                  { "email":"%s", "password":"%s", "returnSecureToken": true }
                  """.formatted(email, pass);

                HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build();

                HttpResponse<String> resp = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() != 200) throw new RuntimeException(resp.body());

                javafx.application.Platform.runLater(() -> {
                    try { DemoApp.setRoot("secondary"); }
                    catch (Exception ex) { outputTextArea.setText("Nav error: " + ex.getMessage()); }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> outputTextArea.setText("Sign-in failed: " + e.getMessage()));
            }
        }).start();
    }
}
