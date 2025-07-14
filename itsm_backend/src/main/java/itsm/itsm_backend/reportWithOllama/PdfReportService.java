package itsm.itsm_backend.reportWithOllama;

import itsm.itsm_backend.ticket.Ticket;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Service;
import org.jfree.chart.ChartUtils;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfReportService {

    public void generatePdf(List<Ticket> tickets, String ollamaText, Map<String, Long> categoryStats) throws Exception {
        Map<String, Object> params = new HashMap<>();

        // Paramètres généraux
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        params.put("reportPeriod", lastMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH)));
        params.put("totalTickets", tickets.size());
        params.put("ollamaSummary", ollamaText);
        params.put("generationDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Statistiques par service (en supposant que vous avez un champ service)
        Map<String, Long> serviceStats = getServiceStats(tickets);
        params.put("serviceStats", serviceStats);

        // Top 5 des catégories
        List<CategoryStat> topCategories = getTopCategories(categoryStats);
        params.put("topCategories", new JRBeanCollectionDataSource(topCategories));

        // Top 5 des services
        List<ServiceStat> topServices = getTopServices(serviceStats);
        params.put("topServices", new JRBeanCollectionDataSource(topServices));

        // Incidents les plus fréquents (regroupés par titre similaire)
        List<IncidentFrequency> frequentIncidents = getFrequentIncidents(tickets);
        params.put("frequentIncidents", new JRBeanCollectionDataSource(frequentIncidents));

        // Génération des graphiques
        byte[] categoryChart = createCategoryBarChart(categoryStats);
        byte[] serviceChart = createServicePieChart(serviceStats);

        params.put("categoryChart", new ByteArrayInputStream(categoryChart));
        params.put("serviceChart", new ByteArrayInputStream(serviceChart));

        // Analyse des causes récurrentes
        String recurringCauses = analyzeRecurringCauses(tickets);
        params.put("recurringCauses", recurringCauses);

        // Recommandations automatiques
        String recommendations = generateRecommendations(categoryStats, serviceStats);
        params.put("recommendations", recommendations);
        params.put("SUBREPORT_DIR", "src/main/resources/reports/");


        // Compiler et remplir le rapport
        JasperReport jasperReport = JasperCompileManager.compileReport("src/main/resources/reports/trend_report.jrxml");
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());

        // Exporter en PDF
        String outputDir = "reports";
        File dir = new File(outputDir);
        if (!dir.exists()) dir.mkdirs();

        String outputPath = outputDir + File.separator + "trend-report-" + LocalDate.now() + ".pdf";
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputPath);

        System.out.println("Rapport PDF généré : " + outputPath);
    }

    private Map<String, Long> getServiceStats(List<Ticket> tickets) {
        // Adapter selon votre modèle de données
        return tickets.stream()
                .filter(t -> t.getSender() != null && t.getSender().getGroup() != null)
                .collect(Collectors.groupingBy(
                        t -> t.getSender().getGroup().name(),
                        Collectors.counting()
                ));
    }

    private List<CategoryStat> getTopCategories(Map<String, Long> categoryStats) {
        return categoryStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new CategoryStat(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<ServiceStat> getTopServices(Map<String, Long> serviceStats) {
        return serviceStats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> new ServiceStat(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private List<IncidentFrequency> getFrequentIncidents(List<Ticket> tickets) {
        Map<String, Long> incidentCounts = tickets.stream()
                .collect(Collectors.groupingBy(
                        t -> normalizeTitle(t.getTitle()),
                        Collectors.counting()
                ));

        return incidentCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1) // Seulement les incidents répétitifs
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new IncidentFrequency(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private String normalizeTitle(String title) {
        // Normaliser les titres pour détecter les incidents similaires
        return title.toLowerCase()
                .replaceAll("\\d+", "X") // Remplacer les chiffres par X
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String analyzeRecurringCauses(List<Ticket> tickets) {
        Map<String, Long> descriptionKeywords = new HashMap<>();

        tickets.forEach(ticket -> {
            if (ticket.getDescription() != null) {
                String[] words = ticket.getDescription().toLowerCase().split("\\s+");
                for (String word : words) {
                    if (word.length() > 4) { // Ignorer les mots trop courts
                        descriptionKeywords.merge(word, 1L, Long::sum);
                    }
                }
            }
        });

        return descriptionKeywords.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> entry.getKey() + " (" + entry.getValue() + " occurrences)")
                .collect(Collectors.joining(", "));
    }

    private String generateRecommendations(Map<String, Long> categoryStats, Map<String, Long> serviceStats) {
        StringBuilder recommendations = new StringBuilder();

        // Recommandation basée sur la catégorie la plus fréquente
        String topCategory = categoryStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        if (!topCategory.isEmpty()) {
            recommendations.append("• Prioriser la résolution des incidents de type '")
                    .append(topCategory)
                    .append("' qui représentent le plus grand volume.\n");
        }

        // Recommandation basée sur le service le plus impacté
        String topService = serviceStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("");

        if (!topService.isEmpty()) {
            recommendations.append("• Renforcer le support pour le service '")
                    .append(topService)
                    .append("' qui génère le plus d'incidents.\n");
        }

        // Recommandations générales
        recommendations.append("• Mettre en place une documentation préventive pour les incidents récurrents.\n");
        recommendations.append("• Organiser des formations ciblées sur les problématiques les plus fréquentes.");

        return recommendations.toString();
    }

    private byte[] createCategoryBarChart(Map<String, Long> categoryStats) throws Exception {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        categoryStats.forEach((cat, count) -> dataset.addValue(count, "Tickets", cat));

        JFreeChart chart = ChartFactory.createBarChart(
                "Répartition par catégorie",
                "Catégorie",
                "Nombre de tickets",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        // Personnaliser le graphique
        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 14));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(baos, chart, 600, 400);
            return baos.toByteArray();
        }
    }

    private byte[] createServicePieChart(Map<String, Long> serviceStats) throws Exception {
        DefaultPieDataset dataset = new DefaultPieDataset();
        serviceStats.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart(
                "Services les plus impactés",
                dataset,
                true, true, false
        );

        // Personnaliser le graphique circulaire
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setLabelFont(new Font("Arial", Font.PLAIN, 10));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ChartUtils.writeChartAsPNG(baos, chart, 600, 400);
            return baos.toByteArray();
        }
    }

    // Classes internes pour les données du rapport
    public static class CategoryStat {
        private String category;
        private Long count;

        public CategoryStat(String category, Long count) {
            this.category = category;
            this.count = count;
        }

        public String getCategory() { return category; }
        public Long getCount() { return count; }
    }

    public static class ServiceStat {
        private String service;
        private Long count;

        public ServiceStat(String service, Long count) {
            this.service = service;
            this.count = count;
        }

        public String getService() { return service; }
        public Long getCount() { return count; }
    }

    public static class IncidentFrequency {
        private String incident;
        private Long frequency;

        public IncidentFrequency(String incident, Long frequency) {
            this.incident = incident;
            this.frequency = frequency;
        }

        public String getIncident() { return incident; }
        public Long getFrequency() { return frequency; }
    }
}