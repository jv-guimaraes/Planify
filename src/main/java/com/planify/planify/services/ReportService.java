package com.planify.planify.services;

import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.User;
import com.planify.planify.repositories.TransactionRepository;
import com.planify.planify.repositories.UserRepository;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public ReportService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public ByteArrayResource generateReport(List<Transaction> transactions) throws IOException {
        // Create PDF
        try (PDDocument document = new PDDocument()) {

            // Page 1
            PDPage page = new PDPage();
            document.addPage(page);
            float h = page.getBBox().getHeight();
            float w = page.getBBox().getWidth();
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Add text to PDF
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
                contentStream.newLineAtOffset(w * 0.23f,  h * 0.95f);
                contentStream.showText("Relatório financeiro do ano");
                contentStream.endText();

                // Add chart to PDF
                var chartStream = monthlyExpenseChart(transactions);
                PDImageXObject image = PDImageXObject.createFromByteArray(document, chartStream.toByteArray(), "chart");
                contentStream.drawImage(image, 45, h * 0.95f - 320, 500, 300);

                chartStream = monthlyIncomeChart(transactions);
                image = PDImageXObject.createFromByteArray(document, chartStream.toByteArray(), "chart");
                contentStream.drawImage(image, 45, h * 0.95f - 650, 500, 300);
            }

            // Page 2
            page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Add chart to PDF
                var chartStream = expenseByCategoryPieChart(transactions);
                PDImageXObject image = PDImageXObject.createFromByteArray(document, chartStream.toByteArray(), "chart");
                contentStream.drawImage(image, 58, h * 0.95f - 320, 500, 300);

                chartStream = incomeByCategoryPieChart(transactions);
                image = PDImageXObject.createFromByteArray(document, chartStream.toByteArray(), "chart");
                contentStream.drawImage(image, 58, h * 0.95f - 660, 500, 300);
            }

            // Save the PDF
            ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();
            document.save(pdfStream);

            // Now you can save pdfStream to a file or send it as a response
            return new ByteArrayResource(pdfStream.toByteArray());
        }
    }

    private ByteArrayOutputStream monthlyExpenseChart(List<Transaction> transactions) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        var monthlyTotals = new HashMap<Month, Double>();
        transactions.stream()
                .filter(t -> (t.getDate().getYear() == LocalDate.now().getYear()))
                .filter(Transaction::isExpense)
                .forEach(t -> {
                    var month = t.getDate().getMonth();
                    var newValue = monthlyTotals.getOrDefault(month, 0.0) + t.getValue().doubleValue();
                    monthlyTotals.put(month, newValue);
                });

        for (Month m : Month.values()) {
            if (m.getValue() > LocalDate.now().getMonth().getValue()) break;
            dataset.addValue((Number) monthlyTotals.getOrDefault(m, 0.0), "Gastos", m.getValue());
        }

        var chart = ChartFactory.createLineChart(
                "Gasto mensal",
                "Mês",
                "Gastos",
                dataset);

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);
        return chartStream;
    }

    private ByteArrayOutputStream monthlyIncomeChart(List<Transaction> transactions) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        var monthlyTotals = new HashMap<Month, Double>();
        transactions.stream()
                .filter(t -> (t.getDate().getYear() == LocalDate.now().getYear()))
                .filter(t -> !t.isExpense())
                .forEach(t -> {
                    var month = t.getDate().getMonth();
                    var newValue = monthlyTotals.getOrDefault(month, 0.0) + t.getValue().doubleValue();
                    monthlyTotals.put(month, newValue);
                });

        for (Month m : Month.values()) {
            if (m.getValue() > LocalDate.now().getMonth().getValue()) break;
            dataset.addValue((Number) monthlyTotals.getOrDefault(m, 0.0), "Receita", m.getValue());
        }

        var chart = ChartFactory.createLineChart(
                "Receita mensal",
                "Mês",
                "Receita",
                dataset);

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);
        return chartStream;
    }

    private ByteArrayOutputStream expenseByCategoryPieChart(List<Transaction> transactions) throws IOException {
        Map<String, Double> valueByCategory = new HashMap<>();
        transactions.stream()
                .filter(t -> (t.getDate().getYear() == LocalDate.now().getYear()))
                .filter(Transaction::isExpense)
                .forEach(t -> {
                    var category = t.getCategory().getName();
                    var value = valueByCategory.getOrDefault(category, 0.0) + t.getValue().doubleValue();
                    valueByCategory.put(category, value);
                });
        var total = valueByCategory.values().stream().reduce(0.0, Double::sum);

        DefaultPieDataset<String> dataset = new DefaultPieDataset<String>();
        for (var e : valueByCategory.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue() / total);
        }

        var chart = ChartFactory.createPieChart(
                "Gasto por categoria",  // Chart title
                dataset,                  // Dataset
                true,                     // Show legend
                true,
                false
        );

        var plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} = {2}", new DecimalFormat("0"), new DecimalFormat("0%")));

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);
        return chartStream;
    }

    private ByteArrayOutputStream incomeByCategoryPieChart(List<Transaction> transactions) throws IOException {
        Map<String, Double> valueByCategory = new HashMap<>();
        transactions.stream()
                .filter(t -> (t.getDate().getYear() == LocalDate.now().getYear()))
                .filter(t -> !t.isExpense())
                .forEach(t -> {
                    var category = t.getCategory().getName();
                    var value = valueByCategory.getOrDefault(category, 0.0) + t.getValue().doubleValue();
                    valueByCategory.put(category, value);
                });
        var total = valueByCategory.values().stream().reduce(0.0, Double::sum);

        DefaultPieDataset<String> dataset = new DefaultPieDataset<String>();
        for (var e : valueByCategory.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue() / total);
        }

        var chart = ChartFactory.createPieChart(
                "Receita por categoria",  // Chart title
                dataset,                  // Dataset
                true,                     // Show legend
                true,
                false
        );

        var plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} = {2}", new DecimalFormat("0"), new DecimalFormat("0%")));

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);
        return chartStream;
    }

    public ByteArrayResource exportCsv(Principal principal, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserAndDateBetweenOrderByDate(user, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserOrderByDate(user);
        }

        // Gerar o CSV
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream);
        writer.println("date,sender,recipient,value,is_expense,category");
        transactions.stream().map(Transaction::toResponseDto).forEach(t -> {
            var type = t.isExpense() ? "expense" : "income";
            writer.printf("%s,%s,%s,\"%.2f\",%s,%s\n", t.date(), t.sender(), t.recipient(),
                    t.value(), type, t.category().name());
        });
        writer.flush();
        return new ByteArrayResource(outputStream.toByteArray());
    }
}