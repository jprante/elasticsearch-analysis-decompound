package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryShardContext;

public class QueryTraverser {

	private final QueryHandler[] queryHandlers;
	
	@SafeVarargs
	public QueryTraverser(final QueryHandler ...queryHandlers) {
		this.queryHandlers = queryHandlers;
	}
	
	public Query traverse(final QueryShardContext context, final Query query) {
		for (QueryHandler queryHandler : queryHandlers) {
			if (queryHandler.acceptQuery(context, query)) {
				return queryHandler.handleQuery(context, query, this);
			}
		}
		return query;
	}
}
