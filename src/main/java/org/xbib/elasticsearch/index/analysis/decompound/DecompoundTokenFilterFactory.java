/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.xbib.elasticsearch.index.analysis.decompound;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.elasticsearch.index.settings.IndexSettingsService;
import org.xbib.elasticsearch.plugin.analysis.decompound.AnalysisDecompoundPlugin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DecompoundTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Decompounder decompounder;
    private final Boolean respectKeywords;
    private final Boolean subwordsonly;
    private final Map<String,Boolean> filter;

    @Inject
    public DecompoundTokenFilterFactory(Index index,
            IndexSettingsService indexSettingsService, Environment env,
            @Assisted String name, @Assisted Settings settings) {
        super(index, indexSettingsService.getSettings(), name, settings);

        this.decompounder = createDecompounder(env, settings);
        this.respectKeywords = settings.getAsBoolean("respect_keywords", false);
        this.subwordsonly = settings.getAsBoolean("subwords_only", false);
        this.filter = new HashMap<>();
        final String[] filtersInput = settings.getAsArray("filter", new String[0], true);
        for(final String filterInput : filtersInput) {
               this.filter.put(filterInput, true);
        }
    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new DecompoundTokenFilter(tokenStream, decompounder, respectKeywords, subwordsonly, filter);
    }

    private Decompounder createDecompounder(Environment env, Settings settings) {
        try {
            String forward = settings.get("forward", "/kompVVic.tree");
            String backward = settings.get("backward", "/kompVHic.tree");
            String reduce = settings.get("reduce", "/grfExt.tree");
            double threshold = settings.getAsDouble("threshold", 0.51);
            return new Decompounder(
                    AnalysisDecompoundPlugin.class.getResourceAsStream(forward),
                    AnalysisDecompoundPlugin.class.getResourceAsStream(backward),
                    AnalysisDecompoundPlugin.class.getResourceAsStream(reduce),
                    threshold);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("decompounder resources in settings not found: " + settings, e);
        } catch (IOException e) {
            throw new IllegalArgumentException("decompounder resources in settings not found: " + settings, e);
        }
    }
}
