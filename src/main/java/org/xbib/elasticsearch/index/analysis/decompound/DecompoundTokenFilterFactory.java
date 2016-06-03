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

import java.io.IOException;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import org.xbib.elasticsearch.plugin.analysis.decompound.AnalysisDecompoundPlugin;

public class DecompoundTokenFilterFactory extends AbstractTokenFilterFactory {

    private final Decompounder decompounder;
    private final Boolean respectKeywords;
    private final Boolean subwordsonly;

    @Inject
    public DecompoundTokenFilterFactory(IndexSettings indexSettings, String name, Settings settings) {
        super(indexSettings, name, settings);

        this.decompounder = createDecompounder(settings);
        this.respectKeywords = settings.getAsBoolean("respect_keywords", false);
        this.subwordsonly = settings.getAsBoolean("subwords_only", false);

    }

    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new DecompoundTokenFilter(tokenStream, decompounder, respectKeywords, subwordsonly);
    }

    private Decompounder createDecompounder(Settings settings) {
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
        } catch (ClassNotFoundException | IOException e) {
            throw new IllegalArgumentException("decompounder resources in settings not found: " + settings, e);
        }
    }
}
