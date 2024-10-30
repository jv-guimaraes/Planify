package com.planify.planify.services;

import com.planify.planify.entities.Transaction;
import com.planify.planify.entities.TransactionStatus;
import com.planify.planify.entities.User;
import com.planify.planify.repositories.TransactionRepository;
import com.planify.planify.repositories.UserRepository;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
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
import java.util.*;

@Service
public class ReportService {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public ReportService(UserRepository userRepository, TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    public ByteArrayResource generateReport(Principal principal) throws IOException {
        var user = userRepository.findByEmail(principal.getName()).orElseThrow();
        var transactions = transactionRepository.findByUserAndStatusAndDateBetweenOrderByDate(
                user,
                TransactionStatus.COMPLETE,
                LocalDate.of(LocalDate.now().getYear(), 1, 1),
                LocalDate.now()
        );

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

            // Page 3
            page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                // Add chart to PDF
                var chartStream = monthlyCategoryIncomeChart(transactions);
                PDImageXObject image = PDImageXObject.createFromByteArray(document, chartStream.toByteArray(), "chart");
                contentStream.drawImage(image, 20, h * 0.95f - 320, 550, 300);

                chartStream = monthlyCategoryExpenseChart(transactions);
                image = PDImageXObject.createFromByteArray(document, chartStream.toByteArray(), "chart");
                contentStream.drawImage(image, 20, h * 0.95f - 660, 550, 300);
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
                .filter(Transaction::isExpense)
                .forEach(t -> {
                    var month = t.getDate().getMonth();
                    var newValue = monthlyTotals.getOrDefault(month, 0.0) + t.getValue().doubleValue();
                    monthlyTotals.put(month, newValue);
                });

        for (Month m : Month.values()) {
            if (m.getValue() > LocalDate.now().getMonth().getValue()) break;
            dataset.addValue((Number) monthlyTotals.getOrDefault(m, 0.0), "Despesa", m.getValue());
        }

        var chart = ChartFactory.createLineChart(
                "Despesa mensal",
                "Mês",
                "Despesa",
                dataset);

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);
        return chartStream;
    }

    private ByteArrayOutputStream monthlyIncomeChart(List<Transaction> transactions) throws IOException {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        var monthlyTotals = new HashMap<Month, Double>();
        transactions.stream()
                .filter(t -> !t.isExpense())
                .forEach(t -> {
                    var month = t.getDate().getMonth();
                    var newValue = monthlyTotals.getOrDefault(month, 0.0) + t.getValue().doubleValue();
                    monthlyTotals.put(month, newValue);
                });

        for (Month m : Month.values()) {
            if (m.getValue() > LocalDate.now().getMonth().getValue()) break;
            dataset.addValue((Number) monthlyTotals.getOrDefault(m, 0.0), "Renda", m.getValue());
        }

        var chart = ChartFactory.createLineChart(
                "Renda mensal",
                "Mês",
                "Renda",
                dataset);

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);
        return chartStream;
    }

    private ByteArrayOutputStream expenseByCategoryPieChart(List<Transaction> transactions) throws IOException {
        Map<String, Double> valueByCategory = new HashMap<>();
        transactions.stream()
                .filter(Transaction::isExpense)
                .forEach(t -> {
                    var category = t.getCategory().getName();
                    var value = valueByCategory.getOrDefault(category, 0.0) + t.getValue().doubleValue();
                    valueByCategory.put(category, value);
                });

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (var e : valueByCategory.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Despesa por categoria",  // Chart title
                dataset,                  // Dataset
                true,                     // Show legend
                true,
                false
        );

        var plot = (PiePlot<?>) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} = {1} ({2})", new DecimalFormat("0"), new DecimalFormat("0%")));

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);
        return chartStream;
    }

    private ByteArrayOutputStream incomeByCategoryPieChart(List<Transaction> transactions) throws IOException {
        Map<String, Double> valueByCategory = new HashMap<>();
        transactions.stream()
                .filter(t -> !t.isExpense())
                .forEach(t -> {
                    var category = t.getCategory().getName();
                    var value = valueByCategory.getOrDefault(category, 0.0) + t.getValue().doubleValue();
                    valueByCategory.put(category, value);
                });

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (var e : valueByCategory.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Renda por categoria",  // Chart title
                dataset,                  // Dataset
                true,                     // Show legend
                true,
                false
        );

        var plot = (PiePlot<?>) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{0} = {1} ({2})", new DecimalFormat("0"), new DecimalFormat("0%")));

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
        transactions = transactions.stream().filter(t -> t.getStatus() == TransactionStatus.COMPLETE).toList();

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

    public ByteArrayOutputStream monthlyCategoryIncomeChart(List<Transaction> transactions) throws IOException {
        Map<Month, Map<String, Double>> map = new HashMap<>();
        Set<String> categories = new HashSet<>();
        for (Month m : Month.values()) map.put(m, new HashMap<>());
        transactions.stream().filter(Transaction::isExpense)
                .forEach(t -> {
                    var month = t.getDate().getMonth();
                    var category = t.getCategory().getName();
                    categories.add(category);
                    var value = map.get(month).getOrDefault(category, 0.0);
                    map.get(month).put(category, value + t.getValue().doubleValue());

                });
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Month month : Month.values()) {
            if (month.getValue() > LocalDate.now().getMonth().getValue()) break;
            for (var category : categories) {
                var value = map.get(month).getOrDefault(category, 0.0);
                dataset.addValue(value, category, month.name().substring(0, 3));
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Despesa mensal por categoria",      // Chart title
                "Categoria",               // X-axis label
                "Despesa",                  // Y-axis label
                dataset                   // Dataset
        );

        // Save chart as PNG
        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);
        return chartStream;
    }

    public ByteArrayOutputStream monthlyCategoryExpenseChart(List<Transaction> transactions) throws IOException {
        Map<Month, Map<String, Double>> map = new HashMap<>();
        Set<String> categories = new HashSet<>();
        for (Month m : Month.values()) map.put(m, new HashMap<>());
        transactions.stream().filter(t -> !t.isExpense())
                .forEach(t -> {
                    var month = t.getDate().getMonth();
                    var category = t.getCategory().getName();
                    categories.add(category);
                    var value = map.get(month).getOrDefault(category, 0.0);
                    map.get(month).put(category, value + t.getValue().doubleValue());

                });
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Month month : Month.values()) {
            if (month.getValue() > LocalDate.now().getMonth().getValue()) break;
            for (var category : categories) {
                var value = map.get(month).getOrDefault(category, 0.0);
                dataset.addValue(value, category, month.name().substring(0, 3));
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Renda mensal por categoria",
                "Categoria",
                "Renda",
                dataset
        );

        ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
        ChartUtils.writeChartAsPNG(chartStream, chart, 500, 300);
        return chartStream;
    }
}