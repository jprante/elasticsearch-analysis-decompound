package de.pansoft.elasticsearch.index.query.string;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.lucene.search.Queries;
import org.elasticsearch.common.regex.Regex;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.mapper.AllFieldMapper;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.index.query.QueryShardException;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.support.QueryParsers;
import org.elasticsearch.index.search.QueryParserHelper;

public class GeniosQueryStringQueryBuilder extends QueryStringQueryBuilder {
	
    public static final String NAME = "exact_query_string";     // TODO rename in "genios_query_string" later on.... (check also complex_query.json)
    
    public static final MultiMatchQueryBuilder.Type DEFAULT_TYPE = MultiMatchQueryBuilder.Type.BEST_FIELDS;
    
    private static final ParseField QUERY_FIELD = new ParseField("query");
    private static final ParseField FIELDS_FIELD = new ParseField("fields");
    private static final ParseField DEFAULT_FIELD_FIELD = new ParseField("default_field");
    private static final ParseField DEFAULT_OPERATOR_FIELD = new ParseField("default_operator");
    private static final ParseField ANALYZER_FIELD = new ParseField("analyzer");
    private static final ParseField QUOTE_ANALYZER_FIELD = new ParseField("quote_analyzer");
    private static final ParseField ALLOW_LEADING_WILDCARD_FIELD = new ParseField("allow_leading_wildcard");
    private static final ParseField AUTO_GENERATE_PHRASE_QUERIES_FIELD = new ParseField("auto_generate_phrase_queries")
            .withAllDeprecated("This setting is ignored, use [type=phrase] instead to make phrase queries out of all text that " +
                "is within query operators, or use explicitly quoted strings if you need finer-grained control");
    private static final ParseField MAX_DETERMINIZED_STATES_FIELD = new ParseField("max_determinized_states");
    private static final ParseField LOWERCASE_EXPANDED_TERMS_FIELD = new ParseField("lowercase_expanded_terms")
            .withAllDeprecated("Decision is now made by the analyzer");
    private static final ParseField ENABLE_POSITION_INCREMENTS_FIELD = new ParseField("enable_position_increments");
    private static final ParseField ESCAPE_FIELD = new ParseField("escape");
    private static final ParseField USE_DIS_MAX_FIELD = new ParseField("use_dis_max")
            .withAllDeprecated("Set [tie_breaker] to 1 instead");
    private static final ParseField FUZZY_PREFIX_LENGTH_FIELD = new ParseField("fuzzy_prefix_length");
    private static final ParseField FUZZY_MAX_EXPANSIONS_FIELD = new ParseField("fuzzy_max_expansions");
    private static final ParseField FUZZY_REWRITE_FIELD = new ParseField("fuzzy_rewrite");
    private static final ParseField PHRASE_SLOP_FIELD = new ParseField("phrase_slop");
    private static final ParseField TIE_BREAKER_FIELD = new ParseField("tie_breaker");
    private static final ParseField ANALYZE_WILDCARD_FIELD = new ParseField("analyze_wildcard");
    private static final ParseField REWRITE_FIELD = new ParseField("rewrite");
    private static final ParseField MINIMUM_SHOULD_MATCH_FIELD = new ParseField("minimum_should_match");
    private static final ParseField QUOTE_FIELD_SUFFIX_FIELD = new ParseField("quote_field_suffix");
    private static final ParseField LENIENT_FIELD = new ParseField("lenient");
    private static final ParseField LOCALE_FIELD = new ParseField("locale")
            .withAllDeprecated("Decision is now made by the analyzer");
    private static final ParseField TIME_ZONE_FIELD = new ParseField("time_zone");
    private static final ParseField SPLIT_ON_WHITESPACE = new ParseField("split_on_whitespace")
            .withAllDeprecated("This setting is ignored, the parser always splits on operator");
    private static final ParseField ALL_FIELDS_FIELD = new ParseField("all_fields")
            .withAllDeprecated("Set [default_field] to `*` instead");
    private static final ParseField TYPE_FIELD = new ParseField("type");
    private static final ParseField GENERATE_SYNONYMS_PHRASE_QUERY = new ParseField("auto_generate_synonyms_phrase_query");
    private static final ParseField FUZZY_TRANSPOSITIONS_FIELD = new ParseField("fuzzy_transpositions");

	public GeniosQueryStringQueryBuilder(String queryString) throws IOException {
		super(queryString);
	}

	public GeniosQueryStringQueryBuilder(StreamInput in) throws IOException {
		super(in);
	}

	@Override
    public String getWriteableName() {
        return GeniosQueryStringQueryBuilder.NAME;
    }
	
   @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.field(QUERY_FIELD.getPreferredName(), this.queryString());
        if (this.defaultField() != null) {
            builder.field(DEFAULT_FIELD_FIELD.getPreferredName(), this.defaultField());
        }
        builder.startArray(FIELDS_FIELD.getPreferredName());
        for (Map.Entry<String, Float> fieldEntry : this.fields().entrySet()) {
            builder.value(fieldEntry.getKey() + "^" + fieldEntry.getValue());
        }
        builder.endArray();
        builder.field(TYPE_FIELD.getPreferredName(), DEFAULT_TYPE.toString().toLowerCase(Locale.ENGLISH));

        if (tieBreaker() != null) {
            builder.field(TIE_BREAKER_FIELD.getPreferredName(), this.tieBreaker());
        }
        builder.field(DEFAULT_OPERATOR_FIELD.getPreferredName(),
                this.defaultOperator().name().toLowerCase(Locale.ROOT));
        if (this.analyzer() != null) {
            builder.field(ANALYZER_FIELD.getPreferredName(), this.analyzer());
        }
        if (this.quoteAnalyzer() != null) {
            builder.field(QUOTE_ANALYZER_FIELD.getPreferredName(), this.quoteAnalyzer());
        }
        builder.field(MAX_DETERMINIZED_STATES_FIELD.getPreferredName(), this.maxDeterminizedStates());
        if (this.allowLeadingWildcard() != null) {
            builder.field(ALLOW_LEADING_WILDCARD_FIELD.getPreferredName(), this.allowLeadingWildcard());
        }
        builder.field(ENABLE_POSITION_INCREMENTS_FIELD.getPreferredName(), this.enablePositionIncrements());
        this.fuzziness().toXContent(builder, params);
        builder.field(FUZZY_PREFIX_LENGTH_FIELD.getPreferredName(), this.fuzzyPrefixLength());
        builder.field(FUZZY_MAX_EXPANSIONS_FIELD.getPreferredName(), this.fuzzyMaxExpansions());
        if (this.fuzzyRewrite() != null) {
            builder.field(FUZZY_REWRITE_FIELD.getPreferredName(), this.fuzzyRewrite());
        }
        builder.field(PHRASE_SLOP_FIELD.getPreferredName(), this.phraseSlop());
        if (this.analyzeWildcard() != null) {
            builder.field(ANALYZE_WILDCARD_FIELD.getPreferredName(), this.analyzeWildcard());
        }
        if (this.rewrite() != null) {
            builder.field(REWRITE_FIELD.getPreferredName(), this.rewrite());
        }
        if (this.minimumShouldMatch() != null) {
            builder.field(MINIMUM_SHOULD_MATCH_FIELD.getPreferredName(), this.minimumShouldMatch());
        }
        if (this.quoteFieldSuffix() != null) {
            builder.field(QUOTE_FIELD_SUFFIX_FIELD.getPreferredName(), this.quoteFieldSuffix());
        }
        if (this.lenient() != null) {
            builder.field(LENIENT_FIELD.getPreferredName(), this.lenient());
        }
        if (this.timeZone() != null) {
            builder.field(TIME_ZONE_FIELD.getPreferredName(), this.timeZone().getID());
        }
        builder.field(ESCAPE_FIELD.getPreferredName(), this.escape());
        builder.field(GENERATE_SYNONYMS_PHRASE_QUERY.getPreferredName(), autoGenerateSynonymsPhraseQuery());
        builder.field(FUZZY_TRANSPOSITIONS_FIELD.getPreferredName(), fuzzyTranspositions());
        printBoostAndQueryName(builder);
        builder.endObject();
    }
        		
    public static GeniosQueryStringQueryBuilder fromXContent(XContentParser parser) throws IOException {
    	
        String currentFieldName = null;
        XContentParser.Token token;
        String queryString = null;
        String defaultField = null;
        String analyzer = null;
        String quoteAnalyzer = null;
        String queryName = null;
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        int maxDeterminizedStates = QueryStringQueryBuilder.DEFAULT_MAX_DETERMINED_STATES;
        boolean enablePositionIncrements = QueryStringQueryBuilder.DEFAULT_ENABLE_POSITION_INCREMENTS;
        boolean escape = QueryStringQueryBuilder.DEFAULT_ESCAPE;
        int fuzzyPrefixLength = QueryStringQueryBuilder.DEFAULT_FUZZY_PREFIX_LENGTH;
        int fuzzyMaxExpansions = QueryStringQueryBuilder.DEFAULT_FUZZY_MAX_EXPANSIONS;
        int phraseSlop = QueryStringQueryBuilder.DEFAULT_PHRASE_SLOP;
        MultiMatchQueryBuilder.Type type = DEFAULT_TYPE;
        Float tieBreaker = null;
        Boolean analyzeWildcard = null;
        Boolean allowLeadingWildcard = null;
        String minimumShouldMatch = null;
        String quoteFieldSuffix = null;
        Boolean lenient = null;
        Operator defaultOperator = QueryStringQueryBuilder.DEFAULT_OPERATOR;
        String timeZone = null;
        Fuzziness fuzziness = QueryStringQueryBuilder.DEFAULT_FUZZINESS;
        String fuzzyRewrite = null;
        String rewrite = null;
        Map<String, Float> fieldsAndWeights = null;
        boolean autoGenerateSynonymsPhraseQuery = true;
        boolean fuzzyTranspositions = DEFAULT_FUZZY_TRANSPOSITIONS;

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_ARRAY) {
                if (FIELDS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    List<String> fields = new ArrayList<>();
                    while (parser.nextToken() != XContentParser.Token.END_ARRAY) {
                        fields.add(parser.text());
                    }
                    fieldsAndWeights = QueryParserHelper.parseFieldsAndWeights(fields);
                } else {
                    throw new ParsingException(parser.getTokenLocation(), "[" + GeniosQueryStringQueryBuilder.NAME +
                            "] query does not support [" + currentFieldName + "]");
                }
            } else if (token.isValue()) {
                if (QUERY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    queryString = parser.text();
                } else if (DEFAULT_FIELD_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    defaultField = parser.text();
                } else if (DEFAULT_OPERATOR_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    defaultOperator = Operator.fromString(parser.text());
                } else if (ANALYZER_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    analyzer = parser.text();
                } else if (QUOTE_ANALYZER_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    quoteAnalyzer = parser.text();
                } else if (ALLOW_LEADING_WILDCARD_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    allowLeadingWildcard = parser.booleanValue();
                } else if (MAX_DETERMINIZED_STATES_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    maxDeterminizedStates = parser.intValue();
                } else if (ENABLE_POSITION_INCREMENTS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    enablePositionIncrements = parser.booleanValue();
                } else if (ESCAPE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    escape = parser.booleanValue();
                } else if (FUZZY_PREFIX_LENGTH_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    fuzzyPrefixLength = parser.intValue();
                } else if (FUZZY_MAX_EXPANSIONS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    fuzzyMaxExpansions = parser.intValue();
                } else if (FUZZY_REWRITE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    fuzzyRewrite = parser.textOrNull();
                } else if (PHRASE_SLOP_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    phraseSlop = parser.intValue();
                } else if (Fuzziness.FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    fuzziness = Fuzziness.parse(parser);
                } else if (AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    boost = parser.floatValue();
                } else if (TYPE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    type = MultiMatchQueryBuilder.Type.parse(parser.text(), parser.getDeprecationHandler());
                } else if (TIE_BREAKER_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    tieBreaker = parser.floatValue();
                } else if (ANALYZE_WILDCARD_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    analyzeWildcard = parser.booleanValue();
                } else if (REWRITE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    rewrite = parser.textOrNull();
                } else if (MINIMUM_SHOULD_MATCH_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    minimumShouldMatch = parser.textOrNull();
                } else if (QUOTE_FIELD_SUFFIX_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    quoteFieldSuffix = parser.textOrNull();
                } else if (LENIENT_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    lenient = parser.booleanValue();
                } else if (ALL_FIELDS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    defaultField = "*";
                } else if (MAX_DETERMINIZED_STATES_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    maxDeterminizedStates = parser.intValue();
                } else if (TIME_ZONE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    try {
                        timeZone = parser.text();
                    } catch (IllegalArgumentException e) {
                        throw new ParsingException(parser.getTokenLocation(), "[" + GeniosQueryStringQueryBuilder.NAME +
                                "] time_zone [" + parser.text() + "] is unknown");
                    }
                } else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    queryName = parser.text();
                } else if (GENERATE_SYNONYMS_PHRASE_QUERY.match(currentFieldName, parser.getDeprecationHandler())) {
                    autoGenerateSynonymsPhraseQuery = parser.booleanValue();
                } else if (FUZZY_TRANSPOSITIONS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    fuzzyTranspositions = parser.booleanValue();
                } else if (AUTO_GENERATE_PHRASE_QUERIES_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    // ignore, deprecated setting
                } else if (LOWERCASE_EXPANDED_TERMS_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    // ignore, deprecated setting
                } else if (LOCALE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    // ignore, deprecated setting
                } else if (USE_DIS_MAX_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                    // ignore, deprecated setting
                } else if (SPLIT_ON_WHITESPACE.match(currentFieldName, parser.getDeprecationHandler())) {
                    // ignore, deprecated setting
                } else {
                    throw new ParsingException(parser.getTokenLocation(), "[" + GeniosQueryStringQueryBuilder.NAME +
                            "] query does not support [" + currentFieldName + "]");
                }
            } else {
                throw new ParsingException(parser.getTokenLocation(), "[" + GeniosQueryStringQueryBuilder.NAME +
                        "] unknown token [" + token + "] after [" + currentFieldName + "]");
            }
        }
        if (queryString == null) {
            throw new ParsingException(parser.getTokenLocation(), "[" + GeniosQueryStringQueryBuilder.NAME + "] must be provided with a [query]");
        }

        GeniosQueryStringQueryBuilder queryStringQuery = new GeniosQueryStringQueryBuilder(queryString);
        if (fieldsAndWeights != null) {
            queryStringQuery.fields(fieldsAndWeights);
        }
        queryStringQuery.defaultField(defaultField);
        queryStringQuery.defaultOperator(defaultOperator);
        queryStringQuery.analyzer(analyzer);
        queryStringQuery.quoteAnalyzer(quoteAnalyzer);
        queryStringQuery.allowLeadingWildcard(allowLeadingWildcard);
        queryStringQuery.maxDeterminizedStates(maxDeterminizedStates);
        queryStringQuery.enablePositionIncrements(enablePositionIncrements);
        queryStringQuery.escape(escape);
        queryStringQuery.fuzzyPrefixLength(fuzzyPrefixLength);
        queryStringQuery.fuzzyMaxExpansions(fuzzyMaxExpansions);
        queryStringQuery.fuzzyRewrite(fuzzyRewrite);
        queryStringQuery.phraseSlop(phraseSlop);
        queryStringQuery.fuzziness(fuzziness);
        queryStringQuery.type(type);
        if (tieBreaker != null) {
            queryStringQuery.tieBreaker(tieBreaker);
        }
        queryStringQuery.analyzeWildcard(analyzeWildcard);
        queryStringQuery.rewrite(rewrite);
        queryStringQuery.minimumShouldMatch(minimumShouldMatch);
        queryStringQuery.quoteFieldSuffix(quoteFieldSuffix);
        queryStringQuery.lenient(lenient);
        queryStringQuery.timeZone(timeZone);
        queryStringQuery.boost(boost);
        queryStringQuery.queryName(queryName);
        queryStringQuery.autoGenerateSynonymsPhraseQuery(autoGenerateSynonymsPhraseQuery);
        queryStringQuery.fuzzyTranspositions(fuzzyTranspositions);
        return queryStringQuery;
    }
    
    @Override
    protected Query doToQuery(QueryShardContext context) throws IOException {
        String rewrittenQueryString = escape() ? org.apache.lucene.queryparser.classic.QueryParser.escape(this.queryString()) : queryString();
        if (fields().size() > 0 && this.defaultField() != null) {
            throw addValidationError("cannot use [fields] parameter in conjunction with [default_field]", null);
        }

        GeniosQueryStringQueryParser queryParser;
        boolean isLenient = lenient() == null ? context.queryStringLenient() : lenient();
        if (defaultField() != null) {
            if (Regex.isMatchAllPattern(defaultField())) {
                queryParser = new GeniosQueryStringQueryParser(context, lenient() == null ? true : lenient());
            } else {
                queryParser = new GeniosQueryStringQueryParser(context, defaultField(), isLenient);
            }
        } else if (fields().size() > 0) {
            final Map<String, Float> resolvedFields = QueryParserHelper.resolveMappingFields(context, fields());
            queryParser = new GeniosQueryStringQueryParser(context, resolvedFields, isLenient);
        } else {
            List<String> defaultFields = context.defaultFields();
            if (context.getMapperService().allEnabled() == false &&
                    defaultFields.size() == 1 && AllFieldMapper.NAME.equals(defaultFields.get(0))) {
                // For indices created before 6.0 with _all disabled
                defaultFields = Collections.singletonList("*");
            }
            boolean isAllField = defaultFields.size() == 1 && Regex.isMatchAllPattern(defaultFields.get(0));
            if (isAllField) {
                queryParser = new GeniosQueryStringQueryParser(context, lenient() == null ? true : lenient());
            } else {
                final Map<String, Float> resolvedFields = QueryParserHelper.resolveMappingFields(context,
                    QueryParserHelper.parseFieldsAndWeights(defaultFields));
                queryParser = new GeniosQueryStringQueryParser(context, resolvedFields, isLenient);
            }
        }

        if (analyzer() != null) {
            NamedAnalyzer namedAnalyzer = context.getIndexAnalyzers().get(analyzer());
            if (namedAnalyzer == null) {
                throw new QueryShardException(context, "[query_string] analyzer [" + analyzer() + "] not found");
            }
            queryParser.setForceAnalyzer(namedAnalyzer);
        }

        if (quoteAnalyzer() != null) {
            NamedAnalyzer forceQuoteAnalyzer = context.getIndexAnalyzers().get(quoteAnalyzer());
            if (forceQuoteAnalyzer == null) {
                throw new QueryShardException(context, "[query_string] quote_analyzer [" + quoteAnalyzer() + "] not found");
            }
            queryParser.setForceQuoteAnalyzer(forceQuoteAnalyzer);
        }

        queryParser.setDefaultOperator(defaultOperator().toQueryParserOperator());
        queryParser.setType(DEFAULT_TYPE);
        if (tieBreaker() != null) {
            queryParser.setGroupTieBreaker(tieBreaker());
        } else {
            queryParser.setGroupTieBreaker(DEFAULT_TYPE.tieBreaker());
        }
        queryParser.setPhraseSlop(phraseSlop());
        queryParser.setQuoteFieldSuffix(quoteFieldSuffix());
        queryParser.setAllowLeadingWildcard(allowLeadingWildcard() == null ?
            context.queryStringAllowLeadingWildcard() : allowLeadingWildcard());
        queryParser.setAnalyzeWildcard(analyzeWildcard() == null ? context.queryStringAnalyzeWildcard() : analyzeWildcard());
        queryParser.setEnablePositionIncrements(enablePositionIncrements());
        queryParser.setFuzziness(fuzziness());
        queryParser.setFuzzyPrefixLength(fuzzyPrefixLength());
        queryParser.setFuzzyMaxExpansions(fuzzyMaxExpansions());
        queryParser.setFuzzyRewriteMethod(QueryParsers.parseRewriteMethod(this.fuzzyRewrite(), LoggingDeprecationHandler.INSTANCE));
        queryParser.setMultiTermRewriteMethod(QueryParsers.parseRewriteMethod(this.rewrite(), LoggingDeprecationHandler.INSTANCE));
        queryParser.setTimeZone(timeZone());
        queryParser.setMaxDeterminizedStates(maxDeterminizedStates());
        queryParser.setAutoGenerateMultiTermSynonymsPhraseQuery(autoGenerateSynonymsPhraseQuery());
        queryParser.setFuzzyTranspositions(fuzzyTranspositions());

        Query query;
        try {
            query = queryParser.parse(rewrittenQueryString);
        } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            throw new QueryShardException(context, "Failed to parse query [" + this.queryString() + "]", e);
        }

        if (query == null) {
            return null;
        }

        //save the BoostQuery wrapped structure if present
        List<Float> boosts = new ArrayList<>();
        while(query instanceof BoostQuery) {
            BoostQuery boostQuery = (BoostQuery) query;
            boosts.add(boostQuery.getBoost());
            query = boostQuery.getQuery();
        }

        query = Queries.fixNegativeQueryIfNeeded(query);
        query = Queries.maybeApplyMinimumShouldMatch(query, this.minimumShouldMatch());

        //restore the previous BoostQuery wrapping
        for (int i = boosts.size() - 1; i >= 0; i--) {
            query = new BoostQuery(query, boosts.get(i));
        }

        return query;
    }

}
