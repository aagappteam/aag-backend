package aagapp_backend.services.ticket;

import aagapp_backend.components.Constant;
import aagapp_backend.components.JwtUtil;

import aagapp_backend.dto.TicketDTO;
import aagapp_backend.dto.TicketResponseDTO;
import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.VendorEntity;
import aagapp_backend.entity.ticket.PredefinedQA;
import aagapp_backend.entity.ticket.Ticket;
import aagapp_backend.enums.AssignedTeam;
import aagapp_backend.enums.TicketEnum;
import aagapp_backend.enums.TicketPriority;
import aagapp_backend.enums.TicketUserType;
import aagapp_backend.repository.customcustomer.CustomCustomerRepository;
import aagapp_backend.repository.ticket.PredefinedQARepository;
import aagapp_backend.repository.ticket.TicketRepository;
import aagapp_backend.repository.vendor.VendorRepository;
import aagapp_backend.services.CustomCustomerService;
import aagapp_backend.services.ResponseService;
import aagapp_backend.services.vendor.VenderService;
import aagapp_backend.spec.TicketSpecification;
import io.micrometer.common.util.StringUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import jakarta.persistence.Query;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketService {
    @Autowired
    private VenderService vendorService;

    @Autowired
    private CustomCustomerService customCustomerService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ResponseService responseService;

    @Autowired
    private EntityManager em;

    @Autowired
    private PredefinedQARepository qaRepository;

    @Autowired
    private CustomCustomerRepository customCustomerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    public ResponseEntity<?> createTicket(Map<String, Object> ticketDetails, String token) {
        try {
            // Extract User ID and Role from JWT Token
            String jwtToken = token.replace("Bearer ", "");
            Long userId = jwtUtil.extractId(jwtToken);
            int role = jwtUtil.extractRoleId(jwtToken);
            String userorvendorrole = "";
            String EmailId = "";

            // Check if vendor or customer exists based on role
            if (role == Constant.VENDOR_ROLE) {
                VendorEntity vendor = vendorService.getServiceProviderById(userId);
                if (vendor == null) {
                    return responseService.generateErrorResponse("Vendor not found", HttpStatus.NOT_FOUND);
                }
                userorvendorrole = "Vendor";

            } else if (role == Constant.CUSTOMER_ROLE) {
                CustomCustomer customer = customCustomerService.getCustomerById(userId);
                if (customer == null) {
                    return responseService.generateErrorResponse("Customer not found", HttpStatus.NOT_FOUND);
                }
                userorvendorrole = "Customer";
            } else {
                return responseService.generateErrorResponse("Unauthorized role", HttpStatus.UNAUTHORIZED);
            }

            // Extract the ticket details from the request
            String subject = (String) ticketDetails.get("subject");
            String description = (String) ticketDetails.get("description");

            // Validate required fields
            if (StringUtils.isEmpty(subject)) {
                return responseService.generateErrorResponse("Subject is required", HttpStatus.BAD_REQUEST);
            }

            if (StringUtils.isEmpty(description)) {
                return responseService.generateErrorResponse("Description is required", HttpStatus.BAD_REQUEST);
            }

            // Create a new ticket
            Ticket ticket = new Ticket();
            ticket.setSubject(subject);
            ticket.setDescription(description);
            ticket.setStatus(TicketEnum.PENDING);
            ticket.setCustomerOrVendorId(userId);
            ticket.setRole(userorvendorrole);
            ticket.setCreatedDate(new Date());
            ticket.setUpdatedDate(new Date());

            // Save the ticket to the database
            ticket = ticketRepository.save(ticket);

            // Map entity to DTO
            TicketDTO ticketDTO = new TicketDTO();
            ticketDTO.setId(ticket.getId());
            ticketDTO.setSubject(ticket.getSubject());
            ticketDTO.setDescription(ticket.getDescription());
            ticketDTO.setStatus(ticket.getStatus().name());
            ticketDTO.setRemark(ticket.getRemark());
            ticketDTO.setCustomerOrVendorId(ticket.getCustomerOrVendorId());
            ticketDTO.setRole(ticket.getRole());

            // Format the date to "yyyy-MM-dd HH:mm:ss"
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            ticketDTO.setCreatedDate(sdf.format(ticket.getCreatedDate()));
            ticketDTO.setUpdatedDate(sdf.format(ticket.getUpdatedDate()));

            // Return response with DTO
            return responseService.generateSuccessResponse("Ticket raised successfully", ticketDTO, HttpStatus.CREATED);

        } catch (Exception e) {
            return responseService.generateErrorResponse("Error raising ticket: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getTicketsByRoleAndId(String role, Long id, TicketEnum status) {
        try {
            List<Ticket> tickets = ticketRepository.findByRoleAndCustomerOrVendorIdAndStatus(role, id, status);

            return responseService.generateSuccessResponse("Tickets retrieved successfully", tickets, HttpStatus.OK);

        } catch (Exception e) {
            return responseService.generateErrorResponse("Error retrieving tickets: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getTicketsByRoleAndIdNew(String role, Long id, TicketEnum status, String subject, String description,
                                                      Integer page, Integer size) {
        try {
            int pageNumber = (page != null && page >= 0) ? page : 0;
            int pageSize = (size != null && size > 0) ? size : 10;

            StringBuilder sb = new StringBuilder("SELECT t FROM Ticket t WHERE LOWER(t.role) = :role AND t.customerOrVendorId = :id");
            StringBuilder countSb = new StringBuilder("SELECT COUNT(t) FROM Ticket t WHERE LOWER(t.role) = :role AND t.customerOrVendorId = :id");

            if (status != null) {
                sb.append(" AND t.status = :status");
                countSb.append(" AND t.status = :status");
            }
            if (subject != null && !subject.isEmpty()) {
                sb.append(" AND LOWER(t.subject) LIKE :subject");
                countSb.append(" AND LOWER(t.subject) LIKE :subject");
            }
            if (description != null && !description.isEmpty()) {
                sb.append(" AND LOWER(t.description) LIKE :description");
                countSb.append(" AND LOWER(t.description) LIKE :description");
            }

            sb.append(" ORDER BY t.id DESC"); // Sort by ID descending

            // Data query
            TypedQuery<Ticket> query = em.createQuery(sb.toString(), Ticket.class);
            query.setParameter("role", role.toLowerCase());
            query.setParameter("id", id);

            // Count query
            TypedQuery<Long> countQuery = em.createQuery(countSb.toString(), Long.class);
            countQuery.setParameter("role", role.toLowerCase());
            countQuery.setParameter("id", id);

            // Apply parameters
            if (status != null) {
                query.setParameter("status", status);
                countQuery.setParameter("status", status);
            }
            if (subject != null && !subject.isEmpty()) {
                String sub = "%" + subject.toLowerCase() + "%";
                query.setParameter("subject", sub);
                countQuery.setParameter("subject", sub);
            }
            if (description != null && !description.isEmpty()) {
                String desc = "%" + description.toLowerCase() + "%";
                query.setParameter("description", desc);
                countQuery.setParameter("description", desc);
            }

            // Pagination
            query.setFirstResult(pageNumber * pageSize);
            query.setMaxResults(pageSize);

            List<Ticket> tickets = query.getResultList();
            Long totalCount = countQuery.getSingleResult();

            List<TicketDTO> ticketDTOs = tickets.stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

            return responseService.generateSuccessResponseWithCount("Tickets fetched successfully", ticketDTOs, totalCount, HttpStatus.OK);


        } catch (Exception e) {
            return responseService.generateErrorResponse("Error fetching tickets: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private TicketDTO mapToDTO(Ticket ticket) {
        TicketDTO dto = new TicketDTO();
        dto.setId(ticket.getId());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus().name());
        dto.setRemark(ticket.getRemark());
        dto.setCustomerOrVendorId(ticket.getCustomerOrVendorId());
        dto.setRole(ticket.getRole());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dto.setCreatedDate(sdf.format(ticket.getCreatedDate()));
        dto.setUpdatedDate(sdf.format(ticket.getUpdatedDate()));

        return dto;
    }



    /**
     * Fetch tickets by UserId with optional status and role filters
     */
    public Page<Ticket> getTicketsByUserIdWithFilters(
            Long customerOrVendorId,
            TicketEnum status,
            String role,
            Pageable pageable
    ) {
        try {
            // ✅ Base SQL Query
            StringBuilder sql = new StringBuilder("SELECT * FROM tickets WHERE customerorvendorid = :id");
            StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM tickets WHERE customerorvendorid = :id");

            // ✅ Apply status filter if present
            if (status != null) {
                sql.append(" AND status = :status");
                countSql.append(" AND status = :status");
            }

            // ✅ Apply role filter if present
            if (role != null && !role.isEmpty()) {
                sql.append(" AND role = :role");
                countSql.append(" AND role = :role");
            }

            // ✅ Order by creation date
            sql.append(" ORDER BY created_date DESC");

            // ✅ Main Query
            Query query = em.createNativeQuery(sql.toString(), Ticket.class);
            Query countQuery = em.createNativeQuery(countSql.toString());

            // ✅ Set parameters
            setParameters(query, customerOrVendorId, status, role);
            setParameters(countQuery, customerOrVendorId, status, role);

            // ✅ Pagination
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            // ✅ Execute
            List<Ticket> tickets = query.getResultList();
            Long total = ((Number) countQuery.getSingleResult()).longValue();

            return new PageImpl<>(tickets, pageable, total);

        } catch (Exception e) {
            throw new RuntimeException("Error fetching tickets: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ Set Query Parameters Dynamically
     */
    private void setParameters(Query query, Long id, TicketEnum status, String role) {
        query.setParameter("id", id);

        if (status != null) {
            query.setParameter("status", status.name());  // Convert Enum to String
        }

        if (role != null && !role.isEmpty()) {
            query.setParameter("role", role);
        }
    }

    public Page<PredefinedQA> getPredefinedQAByRole(String role, int page, int size, String keyword) {
        try {
            TicketUserType userType;

            if ("customer".equalsIgnoreCase(role)) {
                userType = TicketUserType.CUSTOMER;
            } else if ("vendor".equalsIgnoreCase(role)) {
                userType = TicketUserType.VENDOR;
            } else {
                throw new IllegalArgumentException("Invalid role. Must be 'customer' or 'vendor'");
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            if (keyword != null && !keyword.trim().isEmpty()) {
                return qaRepository.findByUserTypeAndKeyword(userType, keyword, pageable);
            } else {
                return qaRepository.findByUserType(userType, pageable);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching predefined questions: " + e.getMessage(), e);
        }
    }



    public ResponseEntity<?> getFilteredTickets(
            int page, int size,
            String name, String email, String mobile, String search,
            String subject, String description, TicketEnum status,
            String remark, String role, TicketPriority priority,
            AssignedTeam assignedTeam,
            Date createdDate, Date updatedDate
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

            // Apply only Ticket-specific filters here
            Specification<Ticket> spec = TicketSpecification.getTicketFilters(subject, description, status, remark, role, priority, assignedTeam, createdDate, updatedDate);

            Page<Ticket> ticketPage = ticketRepository.findAll(spec, pageable);

            List<TicketResponseDTO> responseList = ticketPage.getContent().stream()
                    .map(ticket -> {
                        String nameVal = null;
                        String emailVal = null;
                        String mobileVal = null;

                        if ("CUSTOMER".equalsIgnoreCase(ticket.getRole())) {
                            Optional<CustomCustomer> customerOpt = customCustomerRepository.findById(ticket.getCustomerOrVendorId());
                            if (customerOpt.isPresent()) {
                                CustomCustomer customer = customerOpt.get();
                                if (!matchesUserFilters(customer.getName(), customer.getEmail(), customer.getMobileNumber(), name, email, mobile, search)) {
                                    return null;
                                }
                                nameVal = customer.getName();
                                emailVal = customer.getEmail();
                                mobileVal = customer.getMobileNumber();
                            }
                        } else if ("VENDOR".equalsIgnoreCase(ticket.getRole())) {
                            Optional<VendorEntity> vendorOpt = vendorRepository.findById(ticket.getCustomerOrVendorId());
                            if (vendorOpt.isPresent()) {
                                VendorEntity vendor = vendorOpt.get();
                                if (!matchesUserFilters(vendor.getName(), vendor.getPrimary_email(), vendor.getMobileNumber(), name, email, mobile, search)) {
                                    return null;
                                }
                                nameVal = vendor.getName();
                                emailVal = vendor.getPrimary_email();
                                mobileVal = vendor.getMobileNumber();
                            }
                        }

                        return TicketResponseDTO.builder()
                                .ticketId(ticket.getId())
                                .subject(ticket.getSubject())
                                .remark(ticket.getRemark())
                                .customerOrVendorId(ticket.getCustomerOrVendorId())
                                .description(ticket.getDescription())
                                .status(ticket.getStatus() != null ? ticket.getStatus().toString() : null)
                                .priority(ticket.getPriority() != null ? ticket.getPriority().toString() : null)
                                .role(ticket.getRole())
                                .assignedTeam(String.valueOf(ticket.getAssignedTeam()))
                                .createdDate(ticket.getCreatedDate())
                                .updatedDate(ticket.getUpdatedDate())
                                .name(nameVal)
                                .email(emailVal)
                                .mobile(mobileVal)
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return responseService.generateSuccessResponseWithCount("responseList", responseList,responseList.stream().count() ,HttpStatus.OK);

        } catch (Exception e) {
            return responseService.generateErrorResponse("Error filtering tickets: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private boolean matchesUserFilters(String uName, String uEmail, String uMobile,
                                       String name, String email, String mobile, String search) {
        if (name != null && (uName == null || !uName.toLowerCase().contains(name.toLowerCase()))) return false;
        if (email != null && (uEmail == null || !uEmail.toLowerCase().contains(email.toLowerCase()))) return false;
        if (mobile != null && (uMobile == null || !uMobile.contains(mobile))) return false;

        if (search != null) {
            String s = search.toLowerCase();
            return (uName != null && uName.toLowerCase().contains(s)) ||
                    (uEmail != null && uEmail.toLowerCase().contains(s)) ||
                    (uMobile != null && uMobile.contains(s));
        }

        return true;
    }




    public ResponseEntity<?> assignTicket(Long ticketId, String team, String priorityStr) {
        Optional<Ticket> optional = ticketRepository.findById(ticketId);
        if (optional.isEmpty()) {
            return responseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
        }

        Ticket ticket = optional.get();
        ticket.setAssignedTeam(AssignedTeam.valueOf(team));

        try {
            TicketPriority priority = TicketPriority.valueOf(priorityStr.toUpperCase());
            ticket.setPriority(priority);
        } catch (IllegalArgumentException e) {
            return responseService.generateErrorResponse("Invalid priority level", HttpStatus.BAD_REQUEST);
        }

        ticket.setStatus(TicketEnum.PROCESSING);
        ticket.setUpdatedDate(new Date());

        ticketRepository.save(ticket);
        return responseService.generateSuccessResponse("Ticket assigned successfully", ticket, HttpStatus.OK);
    }


    public ResponseEntity<?> markTicketAsResolved(Long ticketId) {
        Optional<Ticket> optional = ticketRepository.findById(ticketId);
        if (optional.isEmpty()) {
            return responseService.generateErrorResponse("Ticket not found", HttpStatus.NOT_FOUND);
        }

        Ticket ticket = optional.get();
        ticket.setStatus(TicketEnum.RESOLVED);
        ticket.setUpdatedDate(new Date());

        ticketRepository.save(ticket);
        return responseService.generateSuccessResponse("Ticket marked as resolved", ticket, HttpStatus.OK);
    }


}
