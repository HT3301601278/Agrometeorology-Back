package org.agro.repository;

import org.agro.entity.AlertRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    
    /**
     * 查找所有已启用的规则
     */
    List<AlertRule> findByEnabledTrue();
    
    /**
     * 分页查询所有规则
     */
    Page<AlertRule> findAll(Pageable pageable);
    
    /**
     * 根据规则类型查询规则
     */
    List<AlertRule> findByTypeAndEnabledTrue(Integer type);
    
    /**
     * 根据规则类型和子类型查询规则
     */
    List<AlertRule> findByTypeAndSubTypeAndEnabledTrue(Integer type, Integer subType);
} 