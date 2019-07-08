package de.pansoft.elasticsearch.index.query.string;

import de.pansoft.elasticsearch.index.search.QueryStringQueryParser;
import de.pansoft.lucene.index.query.term.MarkedTermQuery;
import de.pansoft.lucene.search.traversal.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryShardContext;

import java.util.Map;

public class GeniosQueryStringQueryParser extends QueryStringQueryParser {

	private static final QueryTraverser CONTAINER_QUERY_TRAVERSER = new QueryTraverser(
			new CloneOnChangeBooleanQueryHandler(),
			new CloneOnChangeBoostQueryHandler(),
			new CloneOnChangeDisjunctionMaxQueryHandler(),
			new CloneOnChangeConstantScoreQueryHandler()
	);

    private static final QueryTraverser MARK_TERM_QUERY_TRAVERSER =
            CONTAINER_QUERY_TRAVERSER.add(
    		new MarkTermQueryHandler(MarkedTermQuery.Context.PHRASE)
    );

	private final QueryShardContext context;

	public GeniosQueryStringQueryParser(QueryShardContext context, boolean lenient) {
		super(context, lenient);
		this.context = context;
	}

	public GeniosQueryStringQueryParser(QueryShardContext context, String defaultField, boolean isLenient) {
		super(context, defaultField, isLenient);
		this.context = context;
	}

	public GeniosQueryStringQueryParser(QueryShardContext context, Map<String, Float> resolvedFields,
										boolean isLenient) {
		super(context, resolvedFields, isLenient);
		this.context = context;
	}
	
    @Override
    protected Query getFieldQuery(String field, String queryText, int slop) throws ParseException {
    	Query query = super.getFieldQuery(field, queryText, slop);
    	return MARK_TERM_QUERY_TRAVERSER.traverse(this.context, query);
    }

	@Override
	protected Query transformTermToFrequencyQuery(Query query, int minFrequency) {
		return CONTAINER_QUERY_TRAVERSER.add(new TransformTermQueryToMinFrequencyTermQueryHandler(minFrequency))
				.traverse(this.context, query);
	}

	@Override
	protected Query transformPrefixToFrequencyPrefixQuery(Query query, int minFrequency) {
		throw new IllegalStateException("PrefixFrequencyQueries are temporary disabled!");
//		return CONTAINER_QUERY_TRAVERSER.add(new TransformPrefixQueryToMinFrequencyPrefixQueryHandler(minFrequency))
//				.traverse(this.context, query);
	}

}
