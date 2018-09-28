package org.xbib.elasticsearch.plugin.analysis.decompound;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.xbib.elasticsearch.index.analysis.decompound.DecompoundTokenFilterAnalysisProvider;
import org.xbib.elasticsearch.index.query.decompound.ExactPhraseQueryBuilder;

/**
 *
 */
public class AnalysisDecompoundPlugin extends Plugin implements AnalysisPlugin, SearchPlugin {

	private static final Logger LOG = LogManager.getLogger(AnalysisDecompoundPlugin.class);
	
	public static final Setting<Long> SETTING_MAX_CACHE_SIZE = 
			Setting.longSetting("decompound_max_cache_size", 8388608, 131072, Setting.Property.NodeScope);

	private final long maxCacheSize;
	
    @Inject
    public AnalysisDecompoundPlugin(Settings settings) {
    		this.maxCacheSize = SETTING_MAX_CACHE_SIZE.get(settings);
    		LOG.info("Maximum Cache Size AnalysisDecompoundPlugin: " + this.maxCacheSize);
    		
    }

    @Override
    public Map<String, AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
    		return Collections.singletonMap("decompound", new DecompoundTokenFilterAnalysisProvider(this.maxCacheSize));
    }

    @Override
	public List<Setting<?>> getSettings() {
		return Stream.of(SETTING_MAX_CACHE_SIZE).collect(Collectors.toList());
	}
    
    @Override
    public List<QuerySpec<?>> getQueries() {
    	return Arrays.asList(
        		new QuerySpec<>(
        				ExactPhraseQueryBuilder.NAME,
        				ExactPhraseQueryBuilder::new,
        				ExactPhraseQueryBuilder::fromXContent));
    }

}
