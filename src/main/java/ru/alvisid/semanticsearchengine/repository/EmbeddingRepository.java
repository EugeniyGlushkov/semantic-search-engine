package ru.alvisid.semanticsearchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.alvisid.semanticsearchengine.model.EmbeddingEntity;

import java.util.List;

/**
 * @author EGlushkov
 * Date: 08.07.2026
 * Time: 1:03
 */

@Repository
public interface EmbeddingRepository extends JpaRepository<EmbeddingEntity, Long> {

    @Query(value = "SELECT * FROM embeddings ORDER BY cosine_distance(embedding, cast(:vector as vector)) LIMIT :limit", nativeQuery = true)
    List<EmbeddingEntity> findNearestByEmbedding(@Param("vector") String vector, @Param("limit") int limit);
}
