package de.pansoft.lucene.search.traversal;

import de.pansoft.lucene.index.query.frequency.MinFrequencyPrefixQuery;
import de.pansoft.lucene.index.query.frequency.MinFrequencyTermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.index.query.QueryShardContext;

public class TransformPrefixQueryToMinFrequencyPrefixQueryHandler implements QueryHandler {

	private final int minFrequency;

	public TransformPrefixQueryToMinFrequencyPrefixQueryHandler(int minFrequency) {
		this.minFrequency = minFrequency;
	}

	@Override
	public Query handleQuery(final TraverserContext traverserContext, final QueryShardContext context,
							 final Query query, QueryTraverser queryTraverser) {
		final PrefixQuery prefixQuery = (PrefixQuery) query;
		return new MinFrequencyPrefixQuery(prefixQuery.getPrefix(), minFrequency);
	}

	@Override
	public boolean acceptQuery(final TraverserContext traverserContext, final QueryShardContext context,
							   Query query) {
		return query != null && query instanceof PrefixQuery;
	}

}
