package itsm.itsm_backend.ticket.elastic;

import itsm.itsm_backend.ticket.Ticket;
import itsm.itsm_backend.ticket.TicketDocument;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
//@ConditionalOnProperty(name = "elastic.enabled", havingValue = "true")
public interface TicketElasticRepository{/* extends ElasticsearchRepository<TicketDocument, String>
    List<TicketDocument> findByTitleContaining(String keyword);*/
}
