package itsm.itsm_backend.reportWithOllama;

import java.util.List;
import java.util.Map;

public class ReportDTO {
    private int totalTickets;
    private Map<String, Integer> categoryHistogram; // pour histogramme
    private Map<String, Integer> servicePieData;
    private String topCauses;
    private List<FrequentIncidentDTO> frequentIncidents;
    private String ollamaComment;
    private String recommendations;
}
