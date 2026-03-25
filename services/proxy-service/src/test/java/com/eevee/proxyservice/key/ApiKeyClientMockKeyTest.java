package com.eevee.proxyservice.key;

import com.eevee.proxyservice.config.ProxyProperties;
import com.eevee.usage.events.AiProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyClientMockKeyTest {

    @Test
    void openaiUsesMockKeyOpenaiWhenSet() {
        ProxyProperties props = baseProps();
        props.getKeyService().setMockKeyOpenai("sk-specific");
        props.getKeyService().setMockKeyGoogle("AIza-google");
        props.getKeyService().setMockKey("legacy");

        ApiKeyClient client = new ApiKeyClient(props);
        assertThat(client.resolveApiKey("user-1", AiProvider.OPENAI).block()).isEqualTo("sk-specific");
    }

    @Test
    void googleUsesMockKeyGoogleWhenSet() {
        ProxyProperties props = baseProps();
        props.getKeyService().setMockKeyOpenai("sk-specific");
        props.getKeyService().setMockKeyGoogle("AIza-google");
        props.getKeyService().setMockKey("legacy");

        ApiKeyClient client = new ApiKeyClient(props);
        assertThat(client.resolveApiKey("user-1", AiProvider.GOOGLE).block()).isEqualTo("AIza-google");
    }

    @Test
    void fallsBackToLegacyMockKeyWhenProviderSpecificBlank() {
        ProxyProperties props = baseProps();
        props.getKeyService().setMockKeyOpenai("");
        props.getKeyService().setMockKeyGoogle("");
        props.getKeyService().setMockKey("legacy-only");

        ApiKeyClient client = new ApiKeyClient(props);
        assertThat(client.resolveApiKey("user-1", AiProvider.OPENAI).block()).isEqualTo("legacy-only");
        assertThat(client.resolveApiKey("user-1", AiProvider.GOOGLE).block()).isEqualTo("legacy-only");
    }

    private static ProxyProperties baseProps() {
        ProxyProperties props = new ProxyProperties();
        props.getKeyService().setBaseUrl("http://localhost:0");
        props.getKeyService().setCacheTtl("PT1S");
        return props;
    }
}
