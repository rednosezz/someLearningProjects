import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org. springframework.ai.document.Document;

import java.util.List;

/**
 * @author lixuefan
 * @since 2026/5/29
 */
@Bean
public org.springframework.ai.vectorstore.VectorStore vectorStore(EmbeddingModel embeddingModel) {
   SimpleVectorStore simpleVectorStore =
           SimpleVectorStore.builder(embeddingModel).build();
   List<Document> documents = List.of(
           new Document("天气预报agent的作者是lixuefan"));
   //向量化存储
   simpleVectorStore.add(documents);
   return simpleVectorStore;
}

public void main() {
}
