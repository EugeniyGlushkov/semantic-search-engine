package ru.alvisid.semanticsearchengine.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.alvisid.semanticsearchengine.api.dto.EmbeddingResponse;
import ru.alvisid.semanticsearchengine.dto.SearchRequest;
import ru.alvisid.semanticsearchengine.dto.SearchResponse;

@FeignClient(name = "worker-service", url = "${urls.worker:http://localhost:8081}")
public interface WorkerClient {
    @PostMapping("/api/embed/get-by-text")
    EmbeddingResponse getByText(@RequestBody SearchRequest request);

    @PostMapping("/api/embed/search")
    public SearchResponse search(@RequestBody SearchRequest request);
}