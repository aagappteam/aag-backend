package aagapp_backend.services;

import aagapp_backend.components.Constant;
import aagapp_backend.entity.Role;
import aagapp_backend.services.exception.ExceptionHandlingImplement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RoleService {
    @PersistenceContext
    private EntityManager entityManager;
    private ResponseService responseService;
    private ExceptionHandlingImplement exceptionHandling;

    @Autowired
    public void setResponseService(ResponseService responseService) {
        this.responseService = responseService;
    }

    @Autowired
    public void setExceptionHandling(ExceptionHandlingImplement exceptionHandling) {
        this.exceptionHandling = exceptionHandling;
    }

    @Transactional
    public String findRoleName(int role_id) {
        String response= entityManager.createQuery(Constant.FETCH_ROLE, String.class)
                .setParameter("role_id", role_id)
                .getResultStream()
                .findFirst()
                .orElse(null);
        if(response==null)
            return "EMPTY";
        else return response;
    }

    @Transactional
    public ResponseEntity<?> addRole(Role role) {
        try {
            // Check if role name is provided
            if (role.getRoleName() == null)
                return responseService.generateErrorResponse("Role name cannot be Empty", HttpStatus.BAD_REQUEST);

            // Set createdAt and updatedAt to the current timestamp
            LocalDateTime currentTimestamp = LocalDateTime.now();
            role.setCreatedAt(currentTimestamp);
            role.setUpdatedAt(currentTimestamp);

            role.setCreatedBy("SUPER_ADMIN");

            entityManager.persist(role);

            return responseService.generateSuccessResponse("Role added successfully", role, HttpStatus.OK);

        } catch (Exception e) {
            // Handle any exception and return an error response
            exceptionHandling.handleException(e);
            return responseService.generateErrorResponse("Error saving role: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Transactional
    public List<Role> findAllRoleList() {
        TypedQuery<Role> query = entityManager.createQuery(Constant.GET_ALL_ROLES, Role.class);
        return query.getResultList();
    }

        public Role getRoleByRoleId(int roleId) {
        return entityManager.createQuery(Constant.GET_ROLE_BY_ROLE_ID, Role.class)
                .setParameter("roleId", roleId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }


}
