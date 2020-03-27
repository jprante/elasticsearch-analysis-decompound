package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryShardContext;

public class CloneOnChangeBoostQueryHandler implements QueryHandler {

	@Override
	public Query handleQuery(final TraverserContext traverserContext, final QueryShardContext context,
							 final Query query, final QueryTraverser queryTraverser) {
		final BoostQuery boostQuery = (BoostQuery) query;
		final Query newInnerBoostQuery = queryTraverser.traverse(traverserContext, context, boostQuery.getQuery());
		if (newInnerBoostQuery != boostQuery.getQuery()) {
			return new BoostQuery(newInnerBoostQuery, boostQuery.getBoost());
		}
		return query;
	}

	@Override
	public boolean acceptQuery(final TraverserContext traverserContext, final QueryShardContext context, Query query) {
		return query != null && query instanceof BoostQuery;
	}
}
