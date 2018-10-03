package org.xbib.elasticsearch.plugin.analysis.decompound;

import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import org.elasticsearch.plugins.SearchPlugin;
import org.xbib.elasticsearch.index.analysis.decompound.patricia.DecompoundTokenFilterFactory;
import org.xbib.elasticsearch.index.query.decompound.ExactPhraseQueryBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Decompound plugin.
 */
public class AnalysisDecompoundPlugin extends Plugin implements AnalysisPlugin, SearchPlugin {

    private final Settings settings;

    public AnalysisDecompoundPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public List<Setting<?>> getSettings() {
        return Collections.singletonList(
                new Setting<>("plugins.xbib.decompound.enabled", "true", Function.identity(), Setting.Property.NodeScope)
        );
    }

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> extra = new LinkedHashMap<>();
        if (settings.getAsBoolean("plugins.xbib.decompound.enabled", true)) {
            extra.put("decompound", DecompoundTokenFilterFactory::new);
        }
        return extra;
    }

    @Override
    public List<QuerySpec<?>> getQueries() {
        return Collections.singletonList(new QuerySpec<>(ExactPhraseQueryBuilder.NAME,
                ExactPhraseQueryBuilder::new,
                ExactPhraseQueryBuilder::fromXContent));
    }

}
