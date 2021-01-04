package de.pansoft.lucene.search.traversal;

import de.pansoft.lucene.index.query.term.MarkedTermQuery;
import de.pansoft.lucene.search.spans.SpanEmptyPayloadCheckQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;

public class ExactMarkedTermQueryHandler implements QueryHandler {

	private final MarkedTermQuery.Context context;

	public ExactMarkedTermQueryHandler(MarkedTermQuery.Context context) {
		this.context = context;
	}

	@Override
	public Query handleQuery(final TraverserContext traverserContext, final QueryShardContext context,
							 final Query query, QueryTraverser queryTraverser) {
		final MarkedTermQuery termQuery = (MarkedTermQuery) query;
		if (termQuery.getContext() == this.context) {
			MappedFieldType fieldType = context.fieldMapper(termQuery.getTerm().field());
			if (fieldType != null && fieldType.tokenized()) {
				return new SpanEmptyPayloadCheckQuery(new SpanTermQuery(termQuery.getTerm()));
			}
		}
		return termQuery;
	}

	@Override
	public boolean acceptQuery(final TraverserContext traverserContext, final QueryShardContext context, Query query) {
		return query != null && query instanceof MarkedTermQuery;
	}

}
