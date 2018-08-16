package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.Query;

public class QueryTraverser {

	private final QueryHandler[] queryHandlers;
	
	@SafeVarargs
	public QueryTraverser(final QueryHandler ...queryHandlers) {
		this.queryHandlers = queryHandlers;
	}
	
	public Query traverse(final Query query) {
		for (QueryHandler queryHandler : queryHandlers) {
			if (queryHandler.acceptQuery(query)) {
				return queryHandler.handleQuery(query, this);
			}
		}
		return query;
	}
}
