package itsm.itsm_backend.reportWithOllama;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class StatEntry {
    private String name;
    private Long count;

    public StatEntry(Object key, Object value) {

    }
}
