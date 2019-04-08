package de.pansoft.lucene.search.traversal;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.elasticsearch.index.query.QueryShardContext;
import org.xbib.elasticsearch.index.query.decompound.SpanEmptyPayloadCheckQuery;

public class ExactSpanPhraseQueryHandler implements QueryHandler {
	
	@Override
	public Query handleQuery(final QueryShardContext context, final Query query, QueryTraverser queryTraverser) {
		final PhraseQuery phraseQuery = (PhraseQuery) query;
		SpanNearQuery.Builder builder = new SpanNearQuery.Builder(phraseQuery.getTerms()[0].field(), phraseQuery.getSlop() == 0);
		int i = 0;
		int position = -1;
		for(Term term: phraseQuery.getTerms()) {
			if (i > 0) {
				int gap = (phraseQuery.getPositions()[i] - position) - 1;
				if (gap > 0) {
					builder.addGap(gap);
				}
			}
			position = phraseQuery.getPositions()[i];
			builder.addClause(new SpanEmptyPayloadCheckQuery(new SpanTermQuery(term)));
			i++;
		}
		return builder.setSlop(phraseQuery.getSlop()).build();
	}

	@Override
	public boolean acceptQuery(final QueryShardContext context, Query query) {
		return query != null && query instanceof PhraseQuery;
	}

}
