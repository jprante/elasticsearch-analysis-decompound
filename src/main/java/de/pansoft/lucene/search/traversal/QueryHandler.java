package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.Query;

public interface QueryHandler {
	
	Query handleQuery(Query query, QueryTraverser queryTraverser);
	
	boolean acceptQuery(Query query);

}
