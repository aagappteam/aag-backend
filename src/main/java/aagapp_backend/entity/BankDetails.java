package aagapp_backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "bank_details")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id")
    private CustomCustomer customer;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "ifsc_code", nullable = false)
    private String ifscCode;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "branch_name", nullable = false)
    private String branchName;

    @Column(name = "account_type", nullable = false)
    private String accountType;


}

