package org.xbib.elasticsearch.index.query.decompound;

import java.util.Collections;
import java.util.Map;

import org.apache.lucene.queries.payloads.SpanPayloadCheckQuery;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
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
    	}
    	return query;
    }


}
