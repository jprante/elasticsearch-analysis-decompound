package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryShardContext;

public class QueryTraverser {

	private final QueryHandler[] queryHandlers;
	
	@SafeVarargs
	public QueryTraverser(final QueryHandler ...queryHandlers) {
		this.queryHandlers = queryHandlers;
	}

	public QueryTraverser add(final QueryHandler queryHandler) {
		QueryHandler[] queryHandlers = new QueryHandler[this.queryHandlers.length + 1];
		System.arraycopy(this.queryHandlers, 0, queryHandlers, 0, this.queryHandlers.length);
		queryHandlers[this.queryHandlers.length] = queryHandler;
		return new QueryTraverser(queryHandlers);
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
