package org.xbib.elasticsearch.index.query.decompound;

import java.util.Collections;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.queries.payloads.SpanPayloadCheckQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.search.QueryStringQueryParser;

public class ExactQueryStringQueryParser extends QueryStringQueryParser {

	public ExactQueryStringQueryParser(QueryShardContext context, boolean lenient) {
		super(context, lenient);
	}

	public ExactQueryStringQueryParser(QueryShardContext context, String defaultField, boolean isLenient) {
		super(context, defaultField, isLenient);
	}

	public ExactQueryStringQueryParser(QueryShardContext context, Map<String, Float> resolvedFields,
			boolean isLenient) {
		super(context, resolvedFields, isLenient);
	}
	
    @Override
    protected Query getFieldQuery(String field, String queryText, int slop) throws ParseException {
    	Query query = super.getFieldQuery(field, queryText, slop);
    	if (query instanceof TermQuery) {
    		return new SpanPayloadCheckQuery(new SpanTermQuery(((TermQuery)query).getTerm()), Collections.singletonList(null));
    	} else if (query instanceof PhraseQuery) {
    		PhraseQuery phraseQuery = (PhraseQuery) query;
			SpanNearQuery.Builder builder = new SpanNearQuery.Builder(phraseQuery.getTerms()[0].field(), true);
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
				builder.addClause(new SpanPayloadCheckQuery(new SpanTermQuery(term), Collections.singletonList(null)));
				i++;
			}
			return builder.setSlop(phraseQuery.getSlop()).build();
    	}
    	return query;
    }

}
