package org.agro.repository;

import org.agro.entity.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 地块数据访问接口
 */
@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
    
    /**
     * 根据用户ID查询地块列表
     * @param userId 用户ID
     * @return 地块列表
     */
    List<Field> findByUserId(Long userId);
    
    /**
     * 根据用户ID和地块组ID查询地块列表
     * @param userId 用户ID
     * @param groupId 地块组ID
     * @return 地块列表
     */
    List<Field> findByUserIdAndGroupId(Long userId, Long groupId);
    
    /**
     * 根据用户ID和地块名称查询地块
     * @param userId 用户ID
     * @param name 地块名称
     * @return 地块
     */
    Field findByUserIdAndName(Long userId, String name);
    
    /**
     * 根据地块组ID查询地块列表
     * @param groupId 地块组ID
     * @return 地块列表
     */
    List<Field> findByGroupId(Long groupId);
} 