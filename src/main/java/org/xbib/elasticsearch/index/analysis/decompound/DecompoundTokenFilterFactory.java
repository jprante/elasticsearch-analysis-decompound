package org.xbib.elasticsearch.index.analysis.decompound;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;

/**
 *
 */
public class DecompoundTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Decompounder decompounder;

    private final Boolean respectKeywords;

    private final Boolean subwordsonly;
    
    private final long maxCacheSize;

    public DecompoundTokenFilterFactory(IndexSettings indexSettings, @Assisted String name, @Assisted Settings settings, long maxCacheSize) {
        super(indexSettings, name, settings);
        this.decompounder = createDecompounder(settings);
        this.respectKeywords = settings.getAsBoolean("respect_keywords", false);
        this.subwordsonly = settings.getAsBoolean("subwords_only", false);
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new DecompoundTokenFilter(tokenStream, decompounder, respectKeywords, subwordsonly, maxCacheSize);
    }

    private Decompounder createDecompounder(Settings settings) {
        try {
            String forward = settings.get("forward", "/decompound/kompVVic.tree");
            String backward = settings.get("backward", "/decompound/kompVHic.tree");
            String reduce = settings.get("reduce", "/decompound/grfExt.tree");
            double threshold = settings.getAsDouble("threshold", 0.51);
            return new Decompounder(getClass().getResourceAsStream(forward),
                    getClass().getResourceAsStream(backward),
                    getClass().getResourceAsStream(reduce),
                    threshold);
        } catch (Exception e) {
            throw new ElasticsearchException("decompounder resources in settings not found: " + settings, e);
        }
    }
}
