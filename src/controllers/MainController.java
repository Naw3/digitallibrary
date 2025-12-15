package controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Controller for the main view. It swaps subviews into the content area
 */
public class MainController {

    @FXML
    private StackPane contentPane;

    @FXML
    public void initialize() {
        try {
            openBooks();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openBooks() throws IOException {
        loadView("view/BookView.fxml");
    }

    @FXML
    public void openReaders() throws IOException {
        loadView("view/ReaderView.fxml");
    }

    @FXML
    public void openLoans() throws IOException {
        loadView("view/LoanView.fxml");
    }

    @FXML
    public void openStats() throws IOException {
        loadView("view/StatisticsView.fxml");
    }

    private void loadView(String resource) throws IOException {
        Node node = FXMLLoader.load(getClass().getClassLoader().getResource(resource));
        contentPane.getChildren().setAll(node);
    }
}
