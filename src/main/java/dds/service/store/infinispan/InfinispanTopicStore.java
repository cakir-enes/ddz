package dds.service.store.infinispan;

import dds.service.Serde;
import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.api.CacheContainerAdmin;

public class InfinispanTopicStore {

    public static <T> TransientTopicStore<T> createFor(String name, Serde<T> serde) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
                .host("127.0.0.1")
                .port(ConfigurationProperties.DEFAULT_HOTROD_PORT)
                .security().authentication()
                .username("default")
                .password("default")
                .realm("default")
                .saslMechanism("DIGEST-MD5");

        RemoteCacheManager cacheManager = new RemoteCacheManager(builder.build());

        // Create test cache, if such does not exist
        cacheManager.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE).getOrCreateCache(name, DefaultTemplate.DIST_SYNC);

        // Obtain the remote cache
        RemoteCache<String, byte[]> cache = cacheManager.getCache(name);
        return new TransientTopicStore<T>(cache, serde);
    }
}
