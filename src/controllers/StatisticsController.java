package controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import storage.Repository;

import java.util.Map;

/**
 * Controller for statistics view — fills charts based on repository data
 */
public class StatisticsController {

    @FXML
    private BarChart<String, Number> topBooksChart;

    @FXML
    private BarChart<String, Number> loansPerReaderChart;

    @FXML
    public void initialize() {
        fillTopBooksChart();
        fillLoansPerReaderChart();
    }

    private void fillTopBooksChart() {
        Repository repo = Repository.getInstance();
        Map<String, Long> top = repo.topBorrowedBooks(10);
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Long> e : top.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        topBooksChart.getData().clear();
        topBooksChart.getData().add(series);
    }

    private void fillLoansPerReaderChart() {
        Repository repo = Repository.getInstance();
        Map<String, Long> perReader = repo.loansCountByReader();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Long> e : perReader.entrySet()) {
            series.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }
        loansPerReaderChart.getData().clear();
        loansPerReaderChart.getData().add(series);
    }
}
