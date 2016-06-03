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
package org.xbib.elasticsearch.plugin.analysis.decompound;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.Plugin;
import org.xbib.elasticsearch.index.analysis.decompound.DecompoundAnalysisProvider;

public class AnalysisDecompoundPlugin extends Plugin {

    private final Settings settings;

    @Inject
    public AnalysisDecompoundPlugin(Settings settings) {
        this.settings = settings;
    }

    @Override
    public String name() {
        return "analysis-decompound";
    }

    @Override
    public String description() {
        return "A decompounding token filter for german and other languages";
    }

    public void onModule(AnalysisModule module) {
        if (settings.getAsBoolean("plugins.decompound.enabled", true)) {
            module.registerTokenFilter("decompound", new DecompoundAnalysisProvider());
        }
    }
}
