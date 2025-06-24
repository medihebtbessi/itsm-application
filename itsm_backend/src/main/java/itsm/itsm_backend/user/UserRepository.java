package itsm.itsm_backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {
    Optional<User> findByEmail(String email);
    @Query(name = UserConstants.FIND_USER_BY_PUBLIC_ID)
    Optional<User> findByPublicId(Integer publicId);

    @Query(name = UserConstants.FIND_ALL_USERS_EXCEPT_SELF)
    List<User> findAllUsersExceptSelf(@Param("publicId") Integer senderId);

    @Modifying
    void deleteUserByEmail(String email);

    @Modifying
    void  deleteAllByCreatedDateBeforeAndEnable(LocalDateTime now,boolean lock);
}
