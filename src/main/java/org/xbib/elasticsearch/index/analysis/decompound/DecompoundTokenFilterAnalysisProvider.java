package org.xbib.elasticsearch.index.analysis.decompound;

import java.io.IOException;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;

public class DecompoundTokenFilterAnalysisProvider implements AnalysisProvider<TokenFilterFactory>{

	private final long maxDecompoundEntries;
	
	public DecompoundTokenFilterAnalysisProvider(long maxDecompoundEntries) {
		this.maxDecompoundEntries = maxDecompoundEntries;
	}
	
	@Override
	public TokenFilterFactory get(IndexSettings indexSettings, Environment environment, String name, Settings settings)
			throws IOException {
		return new DecompoundTokenFilterFactory(indexSettings, name, settings, maxDecompoundEntries);
	}

}
