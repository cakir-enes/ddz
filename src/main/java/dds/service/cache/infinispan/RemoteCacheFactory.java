package dds.service.cache.infinispan;

import org.infinispan.client.hotrod.DefaultTemplate;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.impl.ConfigurationProperties;
import org.infinispan.commons.api.CacheContainerAdmin;

public class RemoteCacheFactory {

    protected static <K, V> RemoteCache<K, V> createCache(String namespace) {
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
        cacheManager.administration().withFlags(CacheContainerAdmin.AdminFlag.VOLATILE).getOrCreateCache(namespace, DefaultTemplate.DIST_SYNC);

        // Obtain the remote cache
        RemoteCache<K, V> cache = cacheManager.getCache(namespace);
        return cache;
    }
}
