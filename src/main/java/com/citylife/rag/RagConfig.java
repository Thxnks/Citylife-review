package com.citylife.rag;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * RAG 基础设施配置。
 * <p>
 * RedisVectorStore：向量持久化到 Redis Stack（RedisSearch 模块），
 * 服务重启索引不丢失。JedisPooled 专用于向量存储，
 * 与 Spring Data Redis 的 Lettuce 连接池互不干扰。
 * <p>
 * ragExecutor：固定 6 线程，用于 MQE+HyDE 并行调用和多路检索并发。
 */
@Configuration
public class RagConfig {

    @Value("${spring.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Bean(destroyMethod = "close")
    JedisPooled jedisPooled() {
        if (redisPassword != null && !redisPassword.isBlank()) {
            return new JedisPooled(redisHost, redisPort, null, redisPassword);
        }
        return new JedisPooled(redisHost, redisPort);
    }

    @Bean
    VectorStore vectorStore(JedisPooled jedisPooled, EmbeddingModel embeddingModel) {
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName("citylife-review-index")
                .prefix("rag:")
                .initializeSchema(true)
                .build();
    }

    /**
     * RAG 管线专用线程池。
     * 6 个核心线程覆盖 MQE(1) + HyDE(1) + 最多 4 路检索的并行需求。
     */
    @Bean(destroyMethod = "shutdown")
    ExecutorService ragExecutor() {
        return Executors.newFixedThreadPool(6, r -> {
            Thread t = new Thread(r, "rag-");
            t.setDaemon(true);
            return t;
        });
    }
}
