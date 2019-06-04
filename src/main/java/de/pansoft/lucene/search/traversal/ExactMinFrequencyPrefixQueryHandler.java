package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;

import de.pansoft.lucene.index.query.frequency.MinFrequencyPrefixQuery;
import de.pansoft.lucene.search.spans.SpanEmptyPayloadCheckQuery;
import de.pansoft.lucene.search.spans.SpanMinFrequencyFilterQuery;

public class ExactMinFrequencyPrefixQueryHandler implements QueryHandler {

	@Override
	public Query handleQuery(final QueryShardContext context, final Query query, QueryTraverser queryTraverser) {
		final MinFrequencyPrefixQuery multiTermQuery = (MinFrequencyPrefixQuery) query;
		MappedFieldType fieldType = context.fieldMapper(multiTermQuery.getField());
		if (fieldType != null && fieldType.tokenized()) {
			return new SpanMinFrequencyFilterQuery(
					new SpanEmptyPayloadCheckQuery(new SpanMultiTermQueryWrapper<MultiTermQuery>(multiTermQuery)),
					multiTermQuery.getMinFrequency());
		}
		return multiTermQuery;
	}

	@Override
	public boolean acceptQuery(final QueryShardContext context, Query query) {
		return query != null && query instanceof MinFrequencyPrefixQuery;
	}

}
