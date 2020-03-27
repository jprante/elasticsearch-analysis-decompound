package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;

import de.pansoft.lucene.search.spans.SpanEmptyPayloadCheckQuery;

public class ExactTermQueryHandler implements QueryHandler {

	@Override
	public Query handleQuery(final TraverserContext traverserContext, final QueryShardContext context,
							 final Query query, QueryTraverser queryTraverser) {
		final TermQuery termQuery = (TermQuery) query;
		MappedFieldType fieldType = context.fieldMapper(termQuery.getTerm().field());
		if (fieldType != null && fieldType.tokenized()) {
			return new SpanEmptyPayloadCheckQuery(new SpanTermQuery((termQuery).getTerm()));
		}
		return termQuery;
	}

	@Override
	public boolean acceptQuery(final TraverserContext traverserContext, final QueryShardContext context,
							   Query query) {
		return query != null && query instanceof TermQuery;
	}

}
