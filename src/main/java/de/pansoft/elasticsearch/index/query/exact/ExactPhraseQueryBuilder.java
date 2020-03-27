package de.pansoft.elasticsearch.index.query.exact;

import java.io.IOException;
import java.util.Objects;

import de.pansoft.lucene.index.query.term.MarkedTermQuery;
import de.pansoft.lucene.search.traversal.*;
import org.apache.lucene.search.Query;
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

public class ExactPhraseQueryBuilder extends AbstractQueryBuilder<ExactPhraseQueryBuilder> {
	
	public static final String NAME = "exact_phrase";
    private static final ParseField QUERY_FIELD = new ParseField("query");
    private static final ParseField BOOST_EXACT_TOKENS_FIELD = new ParseField("boostExactTokens");
    private static final ParseField ALL_QUERY_TYPES_FIELD = new ParseField("all_query_types");
    private static final QueryTraverser PHRASE_QUERY_TRAVERSER = new QueryTraverser(
    		new CloneOnChangeBooleanQueryHandler(),
    		new CloneOnChangeBoostQueryHandler(),
    		new CloneOnChangeDisjunctionMaxQueryHandler(),
    		new CloneOnChangeConstantScoreQueryHandler(),
    		new ExactMarkedTermQueryHandler(MarkedTermQuery.Context.PHRASE),
    		new ExactPhraseQueryHandler()
    );
    private static final QueryTraverser FULL_QUERY_TRAVERSER = new QueryTraverser(
    		new CloneOnChangeBooleanQueryHandler(),
    		new CloneOnChangeBoostQueryHandler(),
    		new CloneOnChangeDisjunctionMaxQueryHandler(),
    		new CloneOnChangeConstantScoreQueryHandler(),
    		new ExactPhraseQueryHandler(),
    		new ExactTermQueryHandler(),
    		new ExactMinFrequencyPrefixQueryHandler(),
    		new ExactMultiTermQueryHandler(),
    		new ExactMinFrequencyTermQuery()
    );

    private final QueryBuilder query;
    private final boolean allQueryTypes;
    private final Float boostExactTokens;

    public ExactPhraseQueryBuilder(QueryBuilder query, boolean allQueryTypes, Float boostExactTokens) {
    	this.query = query;
    	this.allQueryTypes = allQueryTypes;
    	this.boostExactTokens = boostExactTokens;
    }

    public ExactPhraseQueryBuilder(StreamInput in) throws IOException {
        super(in);
        this.query = in.readNamedWriteable(QueryBuilder.class);
        this.allQueryTypes = in.readBoolean();
        this.boostExactTokens = in.readOptionalFloat();
    }

	@Override
	public String getWriteableName() {
		return NAME;
	}

	@Override
	protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeNamedWriteable(this.query);
        out.writeBoolean(this.allQueryTypes);
        out.writeOptionalFloat(this.boostExactTokens);
	}

	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.field(QUERY_FIELD.getPreferredName());
        query.toXContent(builder, params);
        builder.field(ALL_QUERY_TYPES_FIELD.getPreferredName(), this.allQueryTypes);
        if (this.boostExactTokens != null) {
            builder.field(BOOST_EXACT_TOKENS_FIELD.getPreferredName(), this.boostExactTokens);
        }
        printBoostAndQueryName(builder);
        builder.endObject();
	}

	@Override
	protected Query doToQuery(QueryShardContext context) throws IOException {
        TraverserContext traverserContext = TraverserContext.getContext(this.boostExactTokens);
		return (this.allQueryTypes || this.boostExactTokens != null ?
                FULL_QUERY_TRAVERSER:PHRASE_QUERY_TRAVERSER).traverse(traverserContext,context,
                this.query.toQuery(context));
	}
	
	@Override
    protected QueryBuilder doRewrite(QueryRewriteContext queryRewriteContext) throws IOException {
        QueryBuilder rewrittenQuery = query.rewrite(queryRewriteContext);
        if (rewrittenQuery != query) {
        	ExactPhraseQueryBuilder exactPhraseQuery = new ExactPhraseQueryBuilder(rewrittenQuery,
                    this.allQueryTypes, this.boostExactTokens);
            return exactPhraseQuery;
        }
        return this;
    }

    public static ExactPhraseQueryBuilder fromXContent(XContentParser parser) throws IOException {
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        Float boostExactTokens = null;
        String queryName = null;
        QueryBuilder query = null;
        boolean allQueryTypes = false;
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
                } else if (ALL_QUERY_TYPES_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    allQueryTypes = parser.booleanValue();
                } else if (BOOST_EXACT_TOKENS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    boostExactTokens = parser.floatValue();
                } else {
                    throw new ParsingException(parser.getTokenLocation(), "[nested] query does not support [" + currentFieldName + "]");
                }
            }
        }
        ExactPhraseQueryBuilder queryBuilder =  new ExactPhraseQueryBuilder(query, allQueryTypes, boostExactTokens)
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
