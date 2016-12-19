package org.xbib.elasticsearch.plugin.analysis.decompound;

import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import org.xbib.elasticsearch.index.analysis.decompound.DecompoundTokenFilterFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class AnalysisDecompoundPlugin extends Plugin implements AnalysisPlugin {

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> extra = new LinkedHashMap<>();
        extra.put("decompound", DecompoundTokenFilterFactory::new);
        return extra;
    }
}
