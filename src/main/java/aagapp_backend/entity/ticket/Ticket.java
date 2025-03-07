package aagapp_backend.entity.ticket;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.CurrentTimestamp;

import java.util.Date;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    private String subject;

    private String description;

    private String status;

    private String remark;

    private String priority;

    private String type;


    @ManyToOne
    @JoinColumn(name = "customer_id")
    private CustomCustomer customer;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private VendorEntity vendor;


    private String role;

    private String email;


    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdDate;

    @CurrentTimestamp
    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedDate;


}
