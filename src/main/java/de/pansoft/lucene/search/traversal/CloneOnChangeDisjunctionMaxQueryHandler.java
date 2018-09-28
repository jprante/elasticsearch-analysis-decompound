package de.pansoft.lucene.search.traversal;

import java.util.ArrayList;

import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.Query;

public class CloneOnChangeDisjunctionMaxQueryHandler implements QueryHandler {

	@Override
	public Query handleQuery(final Query query, final QueryTraverser queryTraverser) {
		final DisjunctionMaxQuery disjunctionMaxQuery = (DisjunctionMaxQuery) query;
		boolean changed = false;
		ArrayList<Query> innerQueries = new ArrayList<Query>();
		for (Query innerQuery: disjunctionMaxQuery.getDisjuncts()) {
			final Query newInnerQuery = queryTraverser.traverse(innerQuery);
			if (newInnerQuery != innerQuery) {
				changed = true;
				innerQueries.add(newInnerQuery);
			} else {
				innerQueries.add(innerQuery);
			}
		}
		if (changed) {
			return new DisjunctionMaxQuery(innerQueries, disjunctionMaxQuery.getTieBreakerMultiplier());
		}
		return query;
	}

	@Override
	public boolean acceptQuery(Query query) {
		return query != null && query instanceof DisjunctionMaxQuery;
	}

}
