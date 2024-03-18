package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.GroupMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author cheng
 * @since 2024/2/15 00:35
 **/
@Repository
public interface GroupMainRepository extends JpaRepository<GroupMain, Long> {

    @Query("SELECT g FROM GroupMain g WHERE g.gid = ?1")
    GroupMain findByGid(String gid);
}
