/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.hod.configuration;

import com.hp.autonomy.frontend.find.core.beanconfiguration.InMemoryCondition;
import com.hp.autonomy.frontend.find.core.configuration.AutoCreatingEhCacheCacheManager;
import com.hp.autonomy.frontend.find.hod.web.HodFindCacheNames;
import com.hp.autonomy.hod.caching.HodCacheNameResolver;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(InMemoryCondition.class)
public class HodAutoCreatingEhCacheCacheManager extends AutoCreatingEhCacheCacheManager {
    private final HodCacheNameResolver cacheNameResolver;

    @Autowired
    public HodAutoCreatingEhCacheCacheManager(final CacheManager cacheManager, final CacheConfiguration defaults, final HodCacheNameResolver cacheNameResolver) {
        super(cacheManager, HodFindCacheNames.CACHE_EXPIRES, defaults);
        this.cacheNameResolver = cacheNameResolver;
    }

    @Override
    protected String getCacheName(final String name) {
        return cacheNameResolver.getOriginalName(name);
    }
}
