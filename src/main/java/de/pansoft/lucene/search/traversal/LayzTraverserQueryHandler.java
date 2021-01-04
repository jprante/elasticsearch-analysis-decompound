package de.pansoft.lucene.search.traversal;

import de.pansoft.lucene.index.query.term.MarkedTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.index.query.QueryShardContext;

public class LayzTraverserQueryHandler implements QueryHandler {

	private final Class<? extends Query>[] queryClasses;

	public LayzTraverserQueryHandler(Class<? extends Query> ... queryClasses) {
	    this.queryClasses = queryClasses;
	}

	@Override
	public Query handleQuery(final TraverserContext traverserContext, final QueryShardContext context,
							 final Query query, QueryTraverser queryTraverser) {
		if (traverserContext.getTraversalPhase() == TraversalPhase.BUILD_PHASE) {
			return new LazyTraverserQuery(
					queryTraverser,
					traverserContext.inTraversalPhase(TraversalPhase.REWRITE_PHASE),
					context,
					query);
		}
		return query;
	}

	@Override
	public boolean acceptQuery(final TraverserContext traverserContext, final QueryShardContext context,
							   Query query) {
		if (traverserContext.getTraversalPhase() == TraversalPhase.BUILD_PHASE) {
		    for (Class<? extends Query> clazz: this.queryClasses) {
		    	if (clazz.isAssignableFrom(query.getClass())) {
		    		return true;
				}
			}
		}
		return false;
	}

}
