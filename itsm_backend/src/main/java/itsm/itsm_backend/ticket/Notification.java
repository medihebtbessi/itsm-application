package itsm.itsm_backend.ticket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
@AllArgsConstructor
@NoArgsConstructor
@Data

@JsonIgnoreProperties(ignoreUnknown = true)
public class Notification {
    private String id;
    private String status;
    private String priority;
    private String title;
    private String description;
    private String category;
    private String type;
    private String op;
    private Long timestamp;
    // getters & setters
}
