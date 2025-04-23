package org.agro.repository;

import org.agro.entity.FieldGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 地块组数据访问接口
 */
@Repository
public interface FieldGroupRepository extends JpaRepository<FieldGroup, Long> {
    
    /**
     * 根据用户ID查询地块组列表
     * @param userId 用户ID
     * @return 地块组列表
     */
    List<FieldGroup> findByUserId(Long userId);
    
    /**
     * 根据用户ID和名称查询地块组
     * @param userId 用户ID
     * @param name 地块组名称
     * @return 地块组
     */
    FieldGroup findByUserIdAndName(Long userId, String name);
    
    /**
     * 删除用户的所有地块组
     * @param userId 用户ID
     */
    @Modifying
    @Query("DELETE FROM FieldGroup fg WHERE fg.userId = ?1")
    void deleteByUserId(Long userId);
} 