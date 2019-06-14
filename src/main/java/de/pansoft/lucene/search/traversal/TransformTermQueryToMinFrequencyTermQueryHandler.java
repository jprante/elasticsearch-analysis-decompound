package de.pansoft.lucene.search.traversal;

import de.pansoft.lucene.index.query.frequency.MinFrequencyTermQuery;
import de.pansoft.lucene.index.query.term.MarkedTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;

public class TransformTermQueryToMinFrequencyTermQueryHandler implements QueryHandler {

	private final int minFrequency;

	public TransformTermQueryToMinFrequencyTermQueryHandler(int minFrequency) {
		this.minFrequency = minFrequency;
	}

	@Override
	public Query handleQuery(final QueryShardContext context, final Query query, QueryTraverser queryTraverser) {
		final TermQuery termQuery = (TermQuery) query;
		return new MinFrequencyTermQuery(termQuery.getTerm(), minFrequency);
	}

	@Override
	public boolean acceptQuery(final QueryShardContext context, Query query) {
		return query != null && query instanceof TermQuery;
	}

}
