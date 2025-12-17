package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import storage.Repository;

import java.util.Map;

public class StatisticsController {

    @FXML
    private BarChart<String, Number> topBooksChart;

    @FXML
    private BarChart<String, Number> loansPerReaderChart;

    @FXML
    private Label totalBooksLabel;

    @FXML
    private Label totalReadersLabel;

    @FXML
    private Label totalLoansLabel;

    @FXML
    private Label overdueLoansLabel;

    private Repository repository;

    @FXML
    public void initialize() {
        repository = Repository.getInstance();
        refreshStatistics();
    }

    @FXML
    public void refreshStatistics() {
        fillTopBooksChart();
        fillLoansPerReaderChart();
        updateSummaryLabels();
    }

    private void fillTopBooksChart() {
        Map<String, Long> top = repository.topBorrowedBooks(10);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre d'emprunts");

        for (Map.Entry<String, Long> e : top.entrySet()) {
            String bookTitle = repository.getBookTitle(e.getKey());
            if (bookTitle.length() > 20) {
                bookTitle = bookTitle.substring(0, 17) + "...";
            }
            series.getData().add(new XYChart.Data<>(bookTitle, e.getValue()));
        }

        topBooksChart.getData().clear();
        if (!series.getData().isEmpty()) {
            topBooksChart.getData().add(series);
        }
    }

    private void fillLoansPerReaderChart() {
        Map<String, Long> perReader = repository.loansCountByReader();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Nombre d'emprunts");

        for (Map.Entry<String, Long> e : perReader.entrySet()) {
            String readerName = repository.getReaderName(e.getKey());
            series.getData().add(new XYChart.Data<>(readerName, e.getValue()));
        }

        loansPerReaderChart.getData().clear();
        if (!series.getData().isEmpty()) {
            loansPerReaderChart.getData().add(series);
        }
    }

    private void updateSummaryLabels() {
        if (totalBooksLabel != null) {
            totalBooksLabel.setText("Total livres : " + repository.getBooks().size());
        }
        if (totalReadersLabel != null) {
            totalReadersLabel.setText("Total lecteurs : " + repository.getReaders().size());
        }
        if (totalLoansLabel != null) {
            totalLoansLabel.setText("Total emprunts : " + repository.getLoans().size());
        }
        if (overdueLoansLabel != null) {
            int overdueCount = repository.getAllOverdueLoans().size();
            overdueLoansLabel.setText("Emprunts en retard : " + overdueCount);
            if (overdueCount > 0) {
                overdueLoansLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            } else {
                overdueLoansLabel.setStyle("-fx-text-fill: green;");
            }
        }
    }
}
