package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryShardContext;

public interface QueryHandler {
	
	Query handleQuery(final TraverserContext traverserContext, final QueryShardContext context,
					  Query query, QueryTraverser queryTraverser);
	
	boolean acceptQuery(final TraverserContext traverserContext, final QueryShardContext context, Query query);

}
