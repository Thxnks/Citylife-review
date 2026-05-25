package com.citylife.rag;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RAG 功能开关。本地开发可关闭增强管线节省 API 额度。
 */
@Data
@Component
@ConfigurationProperties(prefix = "citylife.rag")
public class RagProperties {
    /** MQE 多查询扩展，默认开 */
    private boolean mqeEnabled = true;
    /** HyDE 假设文档嵌入，默认开 */
    private boolean hydeEnabled = true;
    /** LLM 重排序，默认开 */
    private boolean rerankEnabled = true;
    /** 热门查询缓存 MQE/HyDE 结果，默认开 */
    private boolean cacheEnabled = true;
    /** 整个增强管线的超时秒数，默认 15 */
    private int timeoutSeconds = 15;
    /** MQE/HyDE 结果缓存过期分钟数，默认 30 */
    private int cacheTtlMinutes = 30;
}
