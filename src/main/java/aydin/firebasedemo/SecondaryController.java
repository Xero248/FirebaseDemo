package aydin.firebasedemo;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SecondaryController {

    @FXML private TextField nameTextField;
    @FXML private TextField ageTextField;
    @FXML private TextField phoneTextField;
    @FXML private TextArea outputTextArea;

    @FXML
    private void switchToPrimary() throws IOException {
        DemoApp.setRoot("primary");
    }

    @FXML
    void writeButtonClicked() {
        outputTextArea.setText("Writing…");
        new Thread(() -> {
            try {
                String name  = nameTextField.getText().trim();
                int age      = Integer.parseInt(ageTextField.getText().trim());
                String phone = phoneTextField.getText().trim();

                DocumentReference docRef = DemoApp.fstore.collection("Persons")
                        .document(UUID.randomUUID().toString());

                Map<String, Object> data = new HashMap<>();
                data.put("Name",  name);
                data.put("Age",   age);
                data.put("Phone", phone);

                ApiFuture<WriteResult> write = docRef.set(data);
                write.get();
                javafx.application.Platform.runLater(() ->
                        outputTextArea.setText("Saved: " + name + " | " + age + " | " + phone));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        outputTextArea.setText("Error: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    void readButtonClicked() {
        outputTextArea.setText("Reading…");
        new Thread(() -> {
            try {
                ApiFuture<QuerySnapshot> future = DemoApp.fstore.collection("Persons").get();
                List<QueryDocumentSnapshot> docs = future.get().getDocuments();
                if (docs.isEmpty()) {
                    javafx.application.Platform.runLater(() -> outputTextArea.setText("No persons found."));
                    return;
                }
                StringBuilder sb = new StringBuilder();
                for (QueryDocumentSnapshot d : docs) {
                    Object n = d.get("Name");
                    Object a = d.get("Age");
                    Object p = d.get("Phone"); // may be null on older docs
                    sb.append(String.valueOf(n)).append(" | ")
                            .append(String.valueOf(a)).append(" | ")
                            .append(p == null ? "" : String.valueOf(p)).append("\n");
                }
                String out = sb.toString();
                javafx.application.Platform.runLater(() -> outputTextArea.setText(out));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        outputTextArea.setText("Error: " + e.getMessage()));
            }
        }).start();
    }
}
