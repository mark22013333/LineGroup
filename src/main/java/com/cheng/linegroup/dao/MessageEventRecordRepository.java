package com.cheng.linegroup.dao;

import com.cheng.linegroup.entity.MessageEventRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageEventRecordRepository extends JpaRepository<MessageEventRecord, Integer> {
}
