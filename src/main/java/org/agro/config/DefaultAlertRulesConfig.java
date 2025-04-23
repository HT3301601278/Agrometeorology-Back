package org.agro.config;

import org.agro.entity.AlertRule;
import org.agro.entity.Field;
import org.agro.entity.User;
import org.agro.repository.AlertRuleRepository;
import org.agro.repository.FieldRepository;
import org.agro.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 默认预警规则配置
 * 在系统启动时，如果没有预警规则，则创建默认规则
 */
@Configuration
public class DefaultAlertRulesConfig {
    private static final Logger logger = LoggerFactory.getLogger(DefaultAlertRulesConfig.class);

    @Autowired
    private AlertRuleRepository alertRuleRepository;
    
    @Autowired
    private FieldRepository fieldRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Bean
    public CommandLineRunner initDefaultAlertRules() {
        return args -> {
            // 如果已经存在规则，则不创建默认规则
            if (alertRuleRepository.count() > 0) {
                return;
            }
            
            // 找到至少一个用户和一个地块
            List<User> users = userRepository.findAll();
            List<Field> fields = fieldRepository.findAll();
            
            if (users.isEmpty() || fields.isEmpty()) {
                logger.warn("未找到用户或地块数据，无法创建默认预警规则");
                return;
            }
            
            // 使用第一个用户和第一个地块作为默认规则的关联对象
            User defaultUser = users.get(0);
            Field defaultField = fields.get(0);
            
            logger.info("使用用户[{}]和地块[{}]创建默认预警规则", defaultUser.getUsername(), defaultField.getName());
            
            List<AlertRule> defaultRules = createDefaultRules(defaultUser, defaultField);
            alertRuleRepository.saveAll(defaultRules);
            logger.info("成功创建{}条默认预警规则", defaultRules.size());
        };
    }
    
    /**
     * 创建默认预警规则列表
     */
    private List<AlertRule> createDefaultRules(User user, Field field) {
        return Arrays.asList(
            // 高温预警
            createRule(
                "高温预警",
                AlertRule.TYPE_TEMPERATURE,
                AlertRule.SUBTYPE_HIGH_TEMPERATURE,
                "tempMax",
                ">=",
                new BigDecimal("35"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}出现高温天气，最高温度预计达到{value}℃，请注意防范高温对作物的影响。",
                user,
                field
            ),
            
            // 低温/霜冻预警
            createRule(
                "霜冻预警",
                AlertRule.TYPE_TEMPERATURE,
                AlertRule.SUBTYPE_LOW_TEMPERATURE,
                "tempMin",
                "<=",
                new BigDecimal("0"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}出现霜冻天气，最低温度预计降至{value}℃，请做好霜冻防护措施。",
                user,
                field
            ),
            
            // 低湿度预警
            createRule(
                "低湿度预警",
                AlertRule.TYPE_HUMIDITY,
                AlertRule.SUBTYPE_LOW_HUMIDITY,
                "humidity",
                "<=",
                new BigDecimal("30"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}出现低湿度天气，相对湿度预计为{value}%，建议及时增湿或喷水降温。",
                user,
                field
            ),
            
            // 高VPD预警
            createRule(
                "高VPD预警",
                AlertRule.TYPE_HUMIDITY,
                AlertRule.SUBTYPE_HIGH_VPD,
                "vpd",
                ">=",
                new BigDecimal("1.5"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}出现高蒸汽压亏缺(VPD={value}kPa)情况，建议增加灌溉频次。",
                user,
                field
            ),
            
            // 降水概率预警
            createRule(
                "降水概率预警",
                AlertRule.TYPE_PRECIPITATION,
                AlertRule.SUBTYPE_PRECIPITATION_PROBABILITY,
                "pop",
                ">=",
                new BigDecimal("0.5"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}有{value}的概率出现降水，请合理安排农事活动。",
                user,
                field
            ),
            
            // 强降水预警
            createRule(
                "强降水预警",
                AlertRule.TYPE_PRECIPITATION,
                AlertRule.SUBTYPE_HEAVY_RAIN,
                "rain1h",
                ">=",
                new BigDecimal("2"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}出现强降水，预计小时雨量为{value}mm，请注意防范积水和涝灾。",
                user,
                field
            ),
            
            // 病害风险预警
            createRule(
                "病害风险预警",
                AlertRule.TYPE_PRECIPITATION,
                AlertRule.SUBTYPE_DISEASE_RISK,
                "humidity",
                ">=",
                new BigDecimal("90"),
                "pop",
                ">=",
                new BigDecimal("0.5"),
                "地块[{field}]预计在{date}出现高湿度({value}%)且降水概率较高({value2})的情况，病害风险增加，建议提前采取防护措施。",
                user,
                field
            ),
            
            // 大风预警
            createRule(
                "大风预警",
                AlertRule.TYPE_WIND,
                AlertRule.SUBTYPE_STRONG_WIND,
                "windGust",
                ">=",
                new BigDecimal("8"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}出现大风天气，预计阵风风速达到{value}m/s，请加固设施、防止作物倒伏。",
                user,
                field
            ),
            
            // 光照不足预警
            createRule(
                "光照不足预警",
                AlertRule.TYPE_LIGHT,
                AlertRule.SUBTYPE_LOW_LIGHT,
                "cloudsAll",
                ">=",
                new BigDecimal("80"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}云量较大({value}%)，光照不足，温室作物可考虑补光。",
                user,
                field
            ),
            
            // 日照不足预警
            createRule(
                "日照不足预警",
                AlertRule.TYPE_LIGHT,
                AlertRule.SUBTYPE_SHORT_DAYLIGHT,
                "dayLength",
                "<=",
                new BigDecimal("12"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}日照时长较短({value}小时)，可能影响光周期敏感作物。",
                user,
                field
            ),
            
            // 气压快速下降预警
            createRule(
                "气压快速下降预警",
                AlertRule.TYPE_PRESSURE,
                AlertRule.SUBTYPE_PRESSURE_DROP,
                "pressure",
                "<=",
                new BigDecimal("1005"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}气压较低({value}hPa)，可能有恶劣天气，请密切关注天气变化。",
                user,
                field
            ),
            
            // 低能见度预警
            createRule(
                "低能见度预警",
                AlertRule.TYPE_PRESSURE,
                AlertRule.SUBTYPE_LOW_VISIBILITY,
                "visibility",
                "<=",
                new BigDecimal("2000"),
                null,
                null,
                null,
                "地块[{field}]预计在{date}能见度较低({value}m)，请注意作业安全。",
                user,
                field
            )
        );
    }

    /**
     * 创建预警规则辅助方法
     */
    private AlertRule createRule(String name, int type, int subType, 
                               String paramName, String operator, BigDecimal threshold,
                               String paramName2, String operator2, BigDecimal threshold2, 
                               String message, User user, Field field) {
        AlertRule rule = new AlertRule();
        rule.setName(name);
        rule.setType(type);
        rule.setSubType(subType);
        rule.setParamName(paramName);
        rule.setOperator(operator);
        rule.setThreshold(threshold);
        rule.setParamName2(paramName2);
        rule.setOperator2(operator2);
        rule.setThreshold2(threshold2);
        rule.setMessage(message);
        rule.setEnabled(true);
        rule.setUser(user);
        rule.setField(field);
        return rule;
    }
} 