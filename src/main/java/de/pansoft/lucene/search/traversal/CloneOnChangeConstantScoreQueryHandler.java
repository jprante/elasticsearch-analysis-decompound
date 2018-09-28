package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;

public class CloneOnChangeConstantScoreQueryHandler implements QueryHandler {

	@Override
	public Query handleQuery(final Query query, final QueryTraverser queryTraverser) {
		final ConstantScoreQuery constantScoreQuery = (ConstantScoreQuery) query;
		final Query newInnerConstantScoreQuery = queryTraverser.traverse(constantScoreQuery.getQuery());
		if (newInnerConstantScoreQuery != constantScoreQuery.getQuery()) {
			return new ConstantScoreQuery(newInnerConstantScoreQuery);
		}
		return query;
	}

	@Override
	public boolean acceptQuery(Query query) {
		return query != null && query instanceof ConstantScoreQuery;
	}}
