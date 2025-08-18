package aagapp_backend.repository.social;

import aagapp_backend.entity.CustomCustomer;
import aagapp_backend.entity.social.SocialUser;
import aagapp_backend.enums.SocialStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SocialUserRepository extends JpaRepository<SocialUser, Long>, JpaSpecificationExecutor<SocialUser> {

        SocialUser findByCustomer(CustomCustomer customer);

    List<SocialUser> findAllByStatus(SocialStatus status);

    long countByStatus(SocialStatus socialStatus);
}
