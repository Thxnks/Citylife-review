package com.citylife.rag;

import com.citylife.entity.Blog;
import com.citylife.service.IBlogService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class ReviewIndexService {

    private final IBlogService blogService;
    private final VectorStore vectorStore;

    public ReviewIndexService(IBlogService blogService, VectorStore vectorStore) {
        this.blogService = blogService;
        this.vectorStore = vectorStore;
    }

    @PostConstruct
    public void init() {
        try {
            List<Blog> blogs = blogService.list();
            if (blogs == null || blogs.isEmpty()) {
                log.warn("没有Blog数据可索引，跳过ReviewIndex");
                return;
            }
            log.info("开始索引 {} 条Blog点评...", blogs.size());

            List<Document> documents = new ArrayList<>();
            for (Blog blog : blogs) {
                String text = (blog.getTitle() != null ? blog.getTitle() : "")
                        + "\n\n"
                        + (blog.getContent() != null ? blog.getContent() : "");
                Map<String, Object> metadata = new LinkedHashMap<>();
                metadata.put("sourceType", "blog");
                metadata.put("documentId", String.valueOf(blog.getId()));
                documents.add(new Document(text, metadata));
            }

            TokenTextSplitter splitter = new TokenTextSplitter();
            List<Document> chunks = splitter.apply(documents);
            log.info("分块完成: {} chunks", chunks.size());

            vectorStore.add(chunks);
            log.info("ReviewIndex完成: {} chunks 已索引", chunks.size());
        } catch (Exception e) {
            log.error("ReviewIndex初始化失败，RAG功能将不可用", e);
        }
    }
}
