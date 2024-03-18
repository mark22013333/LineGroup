package com.cheng.linegroup.dao;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * @author cheng
 * @since 2024/3/11 23:16
 **/
@NoRepositoryBean
public interface BaseRepository<T, P> extends JpaRepository<T, P>, JpaSpecificationExecutor<T> {
    default T findByIdOrThrow(P id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Entity not found with id: " + id));
    }

    default boolean exists(P id) {
        return findById(id).isPresent();
    }

    default void deleteByIdAndCheck(P id) {
        deleteById(id);
        if (exists(id)) {
            throw new EntityExistsException("Entity with id: " + id + " could not be deleted");
        }
    }

}
