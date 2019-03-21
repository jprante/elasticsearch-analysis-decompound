package org.xbib.elasticsearch.index.query.decompound;

import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.search.QueryStringQueryParser;

import de.pansoft.lucene.search.traversal.CloneOnChangeBooleanQueryHandler;
import de.pansoft.lucene.search.traversal.CloneOnChangeBoostQueryHandler;
import de.pansoft.lucene.search.traversal.CloneOnChangeConstantScoreQueryHandler;
import de.pansoft.lucene.search.traversal.CloneOnChangeDisjunctionMaxQueryHandler;
import de.pansoft.lucene.search.traversal.ExactSpanPhraseQueryHandler;
import de.pansoft.lucene.search.traversal.ExactSpanTermQueryHandler;
import de.pansoft.lucene.search.traversal.QueryTraverser;

public class ExactQueryStringQueryParser extends QueryStringQueryParser {
	
	private QueryShardContext context;
    private static final QueryTraverser QUERY_TRAVERSER = new QueryTraverser(
    		new CloneOnChangeBooleanQueryHandler(),
    		new CloneOnChangeBoostQueryHandler(),
    		new CloneOnChangeDisjunctionMaxQueryHandler(),
    		new CloneOnChangeConstantScoreQueryHandler(),
    		new ExactSpanPhraseQueryHandler(),
    		new ExactSpanTermQueryHandler()
    );
	public ExactQueryStringQueryParser(QueryShardContext context, boolean lenient) {
		super(context, lenient);
		this.context = context;
	}

	public ExactQueryStringQueryParser(QueryShardContext context, String defaultField, boolean isLenient) {
		super(context, defaultField, isLenient);
		this.context = context;
	}

	public ExactQueryStringQueryParser(QueryShardContext context, Map<String, Float> resolvedFields,
			boolean isLenient) {
		super(context, resolvedFields, isLenient);
		this.context = context;
	}
	
    @Override
    protected Query getFieldQuery(String field, String queryText, int slop) throws ParseException {
    	Query query = super.getFieldQuery(field, queryText, slop);
    	return QUERY_TRAVERSER.traverse(this.context, query);
    }

}
