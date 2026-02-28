package com.fifthdimension.digital_twin.config;

import org.opensearch.client.RestHighLevelClient;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.opensearch.data.client.orhlc.RestClients;
import org.opensearch.data.client.orhlc.ClientConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenSearchClientConfig extends AbstractOpenSearchConfiguration {

    @Value("${spring.data.opensearch.url}")
    private String url;

    @Value("${spring.data.opensearch.username}")
    private String username;

    @Value("${spring.data.opensearch.password}")
    private String password;

    @Override
    @Bean
    public RestHighLevelClient opensearchClient() {

        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo(url)
                .withBasicAuth(username, password)
                .build();

        return RestClients.create(clientConfiguration).rest();
    }
}
