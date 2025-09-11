package com.cheng.linegroup.repository;

import com.cheng.linegroup.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 庫存管理系統使用者 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根據使用者名稱查詢使用者
     */
    Optional<User> findByUsername(String username);

    /**
     * 根據電子郵件查詢使用者
     */
    Optional<User> findByEmail(String email);

    /**
     * 根據手機號碼查詢使用者
     */
    Optional<User> findByPhone(String phone);

    /**
     * 查詢啟用的使用者
     */
    @Query("SELECT u FROM User u WHERE u.status = 1 AND (u.deleted IS NULL OR u.deleted = 0)")
    List<User> findEnabledUsers();

    /**
     * 根據部門ID查詢使用者
     */
    @Query("SELECT u FROM User u WHERE u.deptId = :deptId AND u.status = 1 AND (u.deleted IS NULL OR u.deleted = 0)")
    List<User> findByDeptId(@Param("deptId") Long deptId);

    /**
     * 根據條件分頁查詢使用者
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:username IS NULL OR u.username LIKE %:username%) AND " +
           "(:nickname IS NULL OR u.nickname LIKE %:nickname%) AND " +
           "(:email IS NULL OR u.email LIKE %:email%) AND " +
           "(:deptId IS NULL OR u.deptId = :deptId) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(u.deleted IS NULL OR u.deleted = 0)")
    Page<User> findUsersByConditions(
            @Param("username") String username,
            @Param("nickname") String nickname,
            @Param("email") String email,
            @Param("deptId") Long deptId,
            @Param("status") Integer status,
            Pageable pageable
    );

    /**
     * 檢查使用者名稱是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username AND (u.deleted IS NULL OR u.deleted = 0)")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 檢查電子郵件是否存在
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.email = :email AND (u.deleted IS NULL OR u.deleted = 0)")
    boolean existsByEmail(@Param("email") String email);

    /**
     * 根據關鍵字搜尋使用者（使用者名稱、暱稱、電子郵件）
     */
    @Query("SELECT u FROM User u WHERE " +
           "(u.username LIKE %:keyword% OR u.nickname LIKE %:keyword% OR u.email LIKE %:keyword%) AND " +
           "u.status = 1 AND (u.deleted IS NULL OR u.deleted = 0)")
    List<User> searchUsers(@Param("keyword") String keyword);

    /**
     * 統計啟用使用者數量
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 1 AND (u.deleted IS NULL OR u.deleted = 0)")
    long countEnabledUsers();

    /**
     * 統計各部門使用者數量
     */
    @Query("SELECT u.deptId, COUNT(u) FROM User u WHERE u.status = 1 AND (u.deleted IS NULL OR u.deleted = 0) GROUP BY u.deptId")
    List<Object[]> countUsersByDepartment();
}
