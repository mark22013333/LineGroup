package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.LineUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author cheng
 * @since 2024/2/14 01:11
 **/
@Repository
public interface LineUserRepository extends JpaRepository<LineUser, Long> {

    @Query("SELECT l FROM LineUser l WHERE l.uid = ?1")
    LineUser findByUid(String uid);
}
