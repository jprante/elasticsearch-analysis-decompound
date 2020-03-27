package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryShardContext;

public class CloneOnChangeBooleanQueryHandler implements QueryHandler {

	@Override
	public Query handleQuery(final TraverserContext traverserContext, final QueryShardContext context,
							 final Query query, final QueryTraverser queryTraverser) {
		final BooleanQuery booleanQuery = (BooleanQuery) query;
		boolean changed = false;
		BooleanQuery.Builder builder = new BooleanQuery.Builder();
		for (BooleanClause clause: booleanQuery.clauses()) {
			final Query newClauseQuery = queryTraverser.traverse(traverserContext, context, clause.getQuery());
			if (newClauseQuery != clause.getQuery()) {
				changed = true;
				builder.add(new BooleanClause(newClauseQuery, clause.getOccur()));
				
			} else {
				builder.add(clause);
			}
		}
		if (changed) {
			builder.setMinimumNumberShouldMatch(booleanQuery.getMinimumNumberShouldMatch());
			return builder.build();
		}
		return query;
	}

	@Override
	public boolean acceptQuery(final TraverserContext traverserContext, final QueryShardContext context, Query query) {
		return query != null && query instanceof BooleanQuery;
	}

}
