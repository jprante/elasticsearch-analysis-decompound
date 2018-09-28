package org.xbib.elasticsearch.index.query.decompound;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import org.apache.lucene.index.Term;
import org.apache.lucene.queries.payloads.SpanPayloadCheckQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryRewriteContext;
import org.elasticsearch.index.query.QueryShardContext;

import de.pansoft.lucene.search.traversal.CloneOnChangeBooleanQueryHandler;
import de.pansoft.lucene.search.traversal.CloneOnChangeBoostQueryHandler;
import de.pansoft.lucene.search.traversal.CloneOnChangeConstantScoreQueryHandler;
import de.pansoft.lucene.search.traversal.CloneOnChangeDisjunctionMaxQueryHandler;
import de.pansoft.lucene.search.traversal.QueryHandler;
import de.pansoft.lucene.search.traversal.QueryTraverser;

public class ExactPhraseQueryBuilder extends AbstractQueryBuilder<ExactPhraseQueryBuilder> {
	
	private static class ReplacePhraseQueryHandler implements QueryHandler {

		@Override
		public Query handleQuery(final Query query, QueryTraverser queryTraverser) {
			final PhraseQuery phraseQuery = (PhraseQuery) query;
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
//				builder.addClause(new SpanPayloadCheckQuery(new SpanTermQuery(term), Collections.singletonList(new BytesRef(new byte[] {1}))));
				builder.addClause(new SpanPayloadCheckQuery(new SpanTermQuery(term), Collections.singletonList(null)));
				i++;
			}
			return builder.setSlop(phraseQuery.getSlop()).build();
		}

		@Override
		public boolean acceptQuery(Query query) {
			return query != null && query instanceof PhraseQuery;
		}
		
	}
	
	public static final String NAME = "exact_phrase";
    private static final ParseField QUERY_FIELD = new ParseField("query");
    private static final QueryTraverser QUERY_TRAVERSER = new QueryTraverser(
    		new CloneOnChangeBooleanQueryHandler(),
    		new CloneOnChangeBoostQueryHandler(),
    		new CloneOnChangeDisjunctionMaxQueryHandler(),
    		new CloneOnChangeConstantScoreQueryHandler(),
    		new ReplacePhraseQueryHandler()
    );

    private final QueryBuilder query;

    public ExactPhraseQueryBuilder(QueryBuilder query) {
    	this.query = query;
    }

    public ExactPhraseQueryBuilder(StreamInput in) throws IOException {
        super(in);
        query = in.readNamedWriteable(QueryBuilder.class);
    }

	@Override
	public String getWriteableName() {
		return NAME;
	}

	@Override
	protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeNamedWriteable(query);
	}

	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.field(QUERY_FIELD.getPreferredName());
        query.toXContent(builder, params);
        printBoostAndQueryName(builder);
        builder.endObject();
	}

	@Override
	protected Query doToQuery(QueryShardContext context) throws IOException {
		return QUERY_TRAVERSER.traverse(this.query.toQuery(context));
	}
	
	@Override
    protected QueryBuilder doRewrite(QueryRewriteContext queryRewriteContext) throws IOException {
        QueryBuilder rewrittenQuery = query.rewrite(queryRewriteContext);
        if (rewrittenQuery != query) {
        	ExactPhraseQueryBuilder exactPhraseQuery = new ExactPhraseQueryBuilder(rewrittenQuery);
            return exactPhraseQuery;
        }
        return this;
    }

    public static ExactPhraseQueryBuilder fromXContent(XContentParser parser) throws IOException {
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String queryName = null;
        QueryBuilder query = null;
        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                if (QUERY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    query = parseInnerQueryBuilder(parser);
                } else {
                    throw new ParsingException(parser.getTokenLocation(), "[nested] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if (AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    boost = parser.floatValue();
                } else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    queryName = parser.text();
                } else {
                    throw new ParsingException(parser.getTokenLocation(), "[nested] query does not support [" + currentFieldName + "]");
                }
            }
        }
        ExactPhraseQueryBuilder queryBuilder =  new ExactPhraseQueryBuilder(query)
            .queryName(queryName)
            .boost(boost);
        return queryBuilder;
    }

	@Override
	protected boolean doEquals(ExactPhraseQueryBuilder that) {
        return Objects.equals(query, that.query);
	}

	@Override
	protected int doHashCode() {
        return Objects.hash(query);
	}
	
    /**
     * Returns the nested query to execute.
     */
    public QueryBuilder query() {
        return query;
    }
}
