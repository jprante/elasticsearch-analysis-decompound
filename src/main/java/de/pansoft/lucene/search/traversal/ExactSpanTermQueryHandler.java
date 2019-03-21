package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;
import org.xbib.elasticsearch.index.query.decompound.SpanEmptyPayloadCheckQuery;

public class ExactSpanTermQueryHandler implements QueryHandler {

	@Override
	public Query handleQuery(final QueryShardContext context, final Query query, QueryTraverser queryTraverser) {
		final TermQuery termQuery = (TermQuery) query;
		MappedFieldType fieldType = context.fieldMapper(termQuery.getTerm().field());
		if (fieldType != null && fieldType.tokenized()) {
			return new SpanEmptyPayloadCheckQuery(new SpanTermQuery((termQuery).getTerm()));
		}
		return termQuery;
	}

	@Override
	public boolean acceptQuery(final QueryShardContext context, Query query) {
		return query != null && query instanceof TermQuery;
	}

}
