package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryShardContext;

public class CloneOnChangeConstantScoreQueryHandler implements QueryHandler {

	@Override
	public Query handleQuery(final TraverserContext traverserContext, final QueryShardContext context,
							 final Query query, final QueryTraverser queryTraverser) {
		final ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) query;
		final Query newInnerConstantScoreQuery = queryTraverser.traverse(traverserContext,
				context, constantScoreQuery.getQuery());
		if (newInnerConstantScoreQuery != constantScoreQuery.getQuery()) {
			return new ConstantScoreQuery(newInnerConstantScoreQuery);
		}
		return query;
	}

	@Override
	public boolean acceptQuery(final TraverserContext traverserContext, final QueryShardContext context, Query query) {
		return query != null && query instanceof ConstantScoreQuery;
	}}
