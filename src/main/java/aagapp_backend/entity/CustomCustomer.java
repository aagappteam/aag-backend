package aagapp_backend.entity;

import aagapp_backend.entity.Document;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CUSTOM_CUSTOMER")
@Inheritance(strategy = InheritanceType.JOINED)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CustomCustomer  {

    @Nullable
    @Column(name = "country_code")
    private String countryCode;

    @Nullable
    @Column(name = "mobile_number", unique = true)
    private String mobileNumber;

    @Nullable
    @Column(name = "otp", unique = true)
    private String otp;

    @Nullable
    @Column(name = "pan_number")
    private String panNumber;


    @Nullable
    @Column(name = "father_name")
    private String fathersName;

    @Nullable
    @Column(name = "nationality")
    private String nationality;

    @Column(name = "mother_name")
    private String mothersName;

    @Nullable
    @Column(name = "date_of_birth")
    private String dob;

    @Nullable
    @Column(name = "gender")
    private String gender;

    @Nullable
    @Column(name = "adhar_number", unique = true)
    @Size(min = 12, max = 12)
    private String adharNumber;


    @Column(name = "hide_phone_number")
    private Boolean hidePhoneNumber=false;

    @Nullable
    @Column(name = "secondary_mobile_number")
    private String secondaryMobileNumber;

    @Nullable
    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Nullable
    @Column(name = "secondary_email")
    private String secondaryEmail;

    @Nullable
    @Column(name = "residential_address")
    private String residentialAddress;

    @Nullable
    @Column(name = "state")
    private String state;

    @Nullable
    @Column(name = "district")
    private String district;

    @Nullable
    @Column(name = "city")
    private String city;

    @Nullable
    @Column(name = "pincode")
    private String pincode;

/*    @Nullable
    @ManyToMany
    @JoinTable(
            name = "customer_saved_forms",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id"))
    private List<CustomProduct>savedForms;*/


    @Nullable
    @JsonManagedReference("documents-customer")
    @OneToMany(mappedBy = "custom_customer", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    private List<Document> documents;


    @Nullable
    @Column(length = 512)
    private String token;

    @JsonBackReference("bankDetails-customer")
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BankDetails> bankDetails = new ArrayList<>();

    @Column(name = "order_count")
    private Integer numberOfOrders;
}