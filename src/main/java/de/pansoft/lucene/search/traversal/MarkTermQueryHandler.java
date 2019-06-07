package de.pansoft.lucene.search.traversal;

import de.pansoft.lucene.index.query.term.MarkedTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;

public class MarkTermQueryHandler implements QueryHandler {

	private final MarkedTermQuery.Context context;

	public MarkTermQueryHandler(MarkedTermQuery.Context context) {
		this.context = context;
	}

	@Override
	public Query handleQuery(final QueryShardContext context, final Query query, QueryTraverser queryTraverser) {
		final TermQuery termQuery = (TermQuery) query;
		MappedFieldType fieldType = context.fieldMapper(termQuery.getTerm().field());
		if (fieldType != null && fieldType.tokenized()) {
			return new MarkedTermQuery(termQuery.getTerm(), this.context);
		}
		return termQuery;
	}

	@Override
	public boolean acceptQuery(final QueryShardContext context, Query query) {
		return query != null && query instanceof TermQuery;
	}

}
