package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;
import org.xbib.elasticsearch.index.query.decompound.SpanEmptyPayloadCheckQuery;

public class ExactSpanMultiTermWrapperQueryHandler implements QueryHandler {

	@Override
	public Query handleQuery(final QueryShardContext context, final Query query, QueryTraverser queryTraverser) {
		final MultiTermQuery multiTermQuery = (MultiTermQuery) query;
		MappedFieldType fieldType = context.fieldMapper(multiTermQuery.getField());
		if (fieldType != null && fieldType.tokenized()) {
			return new SpanEmptyPayloadCheckQuery(new SpanMultiTermQueryWrapper<MultiTermQuery>(multiTermQuery));
		}
		return multiTermQuery;
	}

	@Override
	public boolean acceptQuery(final QueryShardContext context, Query query) {
		return query != null && query instanceof MultiTermQuery;
	}

}
