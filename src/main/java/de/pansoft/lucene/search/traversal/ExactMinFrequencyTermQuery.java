package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;

import de.pansoft.lucene.index.query.frequency.MinFrequencyTermQuery;
import de.pansoft.lucene.search.spans.SpanEmptyPayloadCheckQuery;
import de.pansoft.lucene.search.spans.SpanMinFrequencyFilterQuery;

public class ExactMinFrequencyTermQuery implements QueryHandler {

	@Override
	public Query handleQuery(final TraverserContext traverserContext, QueryShardContext context,
							 Query query, QueryTraverser queryTraverser) {
		final MinFrequencyTermQuery minFrequencyTermQuery = (MinFrequencyTermQuery) query;
		MappedFieldType fieldType = context.fieldMapper(minFrequencyTermQuery.getTerm().field());
		if (fieldType != null && fieldType.tokenized()) {
			return new SpanMinFrequencyFilterQuery(new SpanEmptyPayloadCheckQuery(new SpanTermQuery((minFrequencyTermQuery).getTerm())), minFrequencyTermQuery.getMinFrequency());
		}
		return minFrequencyTermQuery;
	}

	@Override
	public boolean acceptQuery(final TraverserContext traverserContext, QueryShardContext context,
							   Query query) {
		return query != null && query instanceof MinFrequencyTermQuery;
	}

}
