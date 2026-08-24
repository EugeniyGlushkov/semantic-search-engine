package ru.alvisid.semanticsearchengine.api.config;

import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.alvisid.semanticsearch.grpc.SemanticSearchServiceGrpc;

/**
 * @author EGlushkov
 * Date: 24.08.2026
 * Time: 0:36
 */

@Configuration
public class GrpcConfig {

    @Bean
    public SemanticSearchServiceGrpc.SemanticSearchServiceBlockingStub semanticSearchStub(GrpcChannelFactory channelFactory) {
        return SemanticSearchServiceGrpc.newBlockingStub(channelFactory.createChannel("semantic-search-service"));
    }
}