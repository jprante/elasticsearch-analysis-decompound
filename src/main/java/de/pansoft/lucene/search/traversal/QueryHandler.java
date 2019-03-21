package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryShardContext;

public interface QueryHandler {
	
	Query handleQuery(final QueryShardContext context, Query query, QueryTraverser queryTraverser);
	
	boolean acceptQuery(final QueryShardContext context, Query query);

}
