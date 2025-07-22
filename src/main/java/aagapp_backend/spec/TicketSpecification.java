package aagapp_backend.spec;

import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.enums.AssignedTeam;
import aagapp_backend.enums.TicketEnum;
import aagapp_backend.enums.TicketPriority;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> getTicketFilters(
            String subject, String description, TicketEnum status, String remark,
            String role, TicketPriority priority, AssignedTeam assignedTeam, Date createdDate, Date updatedDate
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (subject != null && !subject.isBlank())
                predicates.add(cb.like(cb.lower(root.get("subject")), "%" + subject.toLowerCase() + "%"));
            if (description != null && !description.isBlank())
                predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));
            if (remark != null && !remark.isBlank())
                predicates.add(cb.like(cb.lower(root.get("remark")), "%" + remark.toLowerCase() + "%"));
            if (role != null && !role.isBlank())
                predicates.add(cb.equal(cb.lower(root.get("role")), role.toLowerCase()));
            if (priority != null)
                predicates.add(cb.equal(root.get("priority"), priority));
            if (assignedTeam != null && !assignedTeam.name().isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("assignedTeam")), "%" + assignedTeam.name().toLowerCase() + "%"));
            }
            if (createdDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdDate"), createdDate));
            if (updatedDate != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("updatedDate"), updatedDate));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
