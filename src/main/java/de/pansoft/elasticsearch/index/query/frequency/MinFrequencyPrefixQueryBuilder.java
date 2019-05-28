package de.pansoft.elasticsearch.index.query.frequency;

import java.io.IOException;
import java.util.Objects;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.MultiTermQueryBuilder;
import org.elasticsearch.index.query.QueryShardContext;

import de.pansoft.lucene.index.query.frequency.MinFrequencyPrefixQuery;

public class MinFrequencyPrefixQueryBuilder  extends AbstractQueryBuilder<MinFrequencyPrefixQueryBuilder> implements MultiTermQueryBuilder {
    public static final String NAME = "min-frequency-prefix";

    private static final ParseField PREFIX_FIELD = new ParseField("value");
    private static final ParseField MIN_FREQUENCY_FIELD = new ParseField("min-frequency");

    private final String fieldName;
    private final String value;
    private final int minFrequency;

    /**
     * A Query that matches documents containing terms with a specified prefix.
     *
     * @param fieldName The name of the field
     * @param value The prefix query
     */
    public MinFrequencyPrefixQueryBuilder(String fieldName, String value, int minFrequency) {
        if (Strings.isEmpty(fieldName)) {
            throw new IllegalArgumentException("field name is null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        this.fieldName = fieldName;
        this.value = value;
        this.minFrequency = minFrequency;
    }

    /**
     * Read from a stream.
     */
    public MinFrequencyPrefixQueryBuilder(StreamInput in) throws IOException {
        super(in);
        fieldName = in.readString();
        value = in.readString();
        minFrequency = in.readInt();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeString(fieldName);
        out.writeString(value);
        out.writeInt(minFrequency);
    }

    public String fieldName() {
        return this.fieldName;
    }

    public String value() {
        return this.value;
    }

    public int minFrequency() {
    	return this.minFrequency;
    }

    @Override
    public void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.startObject(fieldName);
        builder.field(PREFIX_FIELD.getPreferredName(), this.value);
        builder.field(MIN_FREQUENCY_FIELD.getPreferredName(), this.minFrequency);
        printBoostAndQueryName(builder);
        builder.endObject();
        builder.endObject();
    }

    public static MinFrequencyPrefixQueryBuilder fromXContent(XContentParser parser) throws IOException {
        String fieldName = null;
        String value = null;
        int minFrequency = 2;

        String queryName = null;
        float boost = AbstractQueryBuilder.DEFAULT_BOOST;
        String currentFieldName = null;
        XContentParser.Token token;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (token == XContentParser.Token.START_OBJECT) {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, currentFieldName);
                fieldName = currentFieldName;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        currentFieldName = parser.currentName();
                    } else {
                        if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            queryName = parser.text();
                        } else if (PREFIX_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            value = parser.textOrNull();
                        } else if (AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            boost = parser.floatValue();
                        } else if (MIN_FREQUENCY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
                            minFrequency = parser.intValue();
                        } else {
                            throw new ParsingException(parser.getTokenLocation(),
                                    "[min-frequency-prefix] query does not support [" + currentFieldName + "]");
                        }
                    }
                }
            } else {
                throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, parser.currentName());
                fieldName = currentFieldName;
                value = parser.textOrNull();
            }
        }

        return new MinFrequencyPrefixQueryBuilder(fieldName, value, minFrequency)
                .boost(boost)
                .queryName(queryName);
    }

    @Override
    public String getWriteableName() {
        return NAME;
    }

    @Override
    protected Query doToQuery(QueryShardContext context) throws IOException {
        Query query = new MinFrequencyPrefixQuery(new Term(fieldName, BytesRefs.toBytesRef(value)), minFrequency);
		if (boost() != AbstractQueryBuilder.DEFAULT_BOOST) {
			query = new BoostQuery(query, boost());
		}
        return query;
    }

    @Override
    protected final int doHashCode() {
        return Objects.hash(fieldName, value, Integer.valueOf(minFrequency));
    }

    @Override
    protected boolean doEquals(MinFrequencyPrefixQueryBuilder other) {
        return Objects.equals(fieldName, other.fieldName) &&
                Objects.equals(value, other.value) &&
                minFrequency == other.minFrequency;
    }
}
