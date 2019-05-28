package de.pansoft.elasticsearch.index.query.frequency;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.Objects;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.ParsingException;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.lucene.BytesRefs;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryShardContext;

import de.pansoft.lucene.index.query.frequency.MinFrequencyTermQuery;

public class MinFrequencyTermQueryBuilder extends AbstractQueryBuilder<MinFrequencyTermQueryBuilder> {

	public static final String NAME = "min-frequency-term";

	private static final ParseField VALUE_FIELD = new ParseField("value");
	private static final ParseField TERM_FIELD = new ParseField("term");
	private static final ParseField MIN_FREQUENCY_FIELD = new ParseField("min-frequency");

	protected final String fieldName;
	protected final Object value;
	private final int minFrequency;

	public MinFrequencyTermQueryBuilder(String fieldName, Object value, int minFrequency) {
		this.fieldName = fieldName;
		this.value = value;
		this.minFrequency = minFrequency;
	}

	public MinFrequencyTermQueryBuilder(StreamInput in) throws IOException {
		super(in);
		fieldName = in.readString();
		value = in.readGenericValue();
		minFrequency = in.readInt();
	}

	@Override
	protected void doWriteTo(StreamOutput out) throws IOException {
		out.writeString(fieldName);
		out.writeGenericValue(value);
		out.writeInt(minFrequency);
	}

	public static MinFrequencyTermQueryBuilder fromXContent(XContentParser parser) throws IOException {
		String queryName = null;
		String fieldName = null;
		Object value = null;
		int minFrequency = 2;
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
						if (TERM_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
							value = maybeConvertToBytesRef(parser.objectBytes());
						} else if (VALUE_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
							value = maybeConvertToBytesRef(parser.objectBytes());
						} else if (AbstractQueryBuilder.NAME_FIELD.match(currentFieldName,
								parser.getDeprecationHandler())) {
							queryName = parser.text();
						} else if (AbstractQueryBuilder.BOOST_FIELD.match(currentFieldName,
								parser.getDeprecationHandler())) {
							boost = parser.floatValue();
						} else if (MIN_FREQUENCY_FIELD.match(currentFieldName, parser.getDeprecationHandler())) {
							minFrequency = parser.intValue();
						} else {
							throw new ParsingException(parser.getTokenLocation(),
									"[min-frequency-term] query does not support [" + currentFieldName + "]");
						}
					}
				}
			} else if (token.isValue()) {
				throwParsingExceptionOnMultipleFields(NAME, parser.getTokenLocation(), fieldName, parser.currentName());
				fieldName = currentFieldName;
				value = maybeConvertToBytesRef(parser.objectBytes());
			} else if (token == XContentParser.Token.START_ARRAY) {
				throw new ParsingException(parser.getTokenLocation(),
						"[min-frequency-term] query does not support array of values");
			}
		}

		MinFrequencyTermQueryBuilder minFrequencyTermQuery = new MinFrequencyTermQueryBuilder(fieldName, value,
				minFrequency);
		minFrequencyTermQuery.boost(boost);
		if (queryName != null) {
			minFrequencyTermQuery.queryName(queryName);
		}
		return minFrequencyTermQuery;
	}

	static Object maybeConvertToBytesRef(Object obj) {
		if (obj instanceof String) {
			return BytesRefs.toBytesRef(obj);
		} else if (obj instanceof CharBuffer) {
			return new BytesRef((CharBuffer) obj);
		}
		return obj;
	}

	static Object maybeConvertToString(Object obj) {
		if (obj instanceof BytesRef) {
			return ((BytesRef) obj).utf8ToString();
		} else if (obj instanceof CharBuffer) {
			return new BytesRef((CharBuffer) obj).utf8ToString();
		}
		return obj;
	}

	@Override
	protected Query doToQuery(QueryShardContext context) throws IOException {
		Query query = new MinFrequencyTermQuery(new Term(this.fieldName, BytesRefs.toBytesRef(this.value)),
				minFrequency);
		if (boost() != AbstractQueryBuilder.DEFAULT_BOOST) {
			query = new BoostQuery(query, boost());
		}
		return query;
	}

	@Override
	public String getWriteableName() {
		return NAME;
	}

	@Override
	protected void doXContent(XContentBuilder builder, Params params) throws IOException {
		builder.startObject(getName());
		builder.startObject(fieldName);
		builder.field(VALUE_FIELD.getPreferredName(), maybeConvertToString(this.value));
		builder.field(MIN_FREQUENCY_FIELD.getPreferredName(), minFrequency);
		printBoostAndQueryName(builder);
		builder.endObject();
		builder.endObject();
	}

	@Override
	protected boolean doEquals(MinFrequencyTermQueryBuilder other) {
		return Objects.equals(fieldName, other.fieldName) && Objects.equals(value, other.value)
				&& minFrequency == other.minFrequency;
	}

	@Override
	protected int doHashCode() {
		return Objects.hash(fieldName, value, Integer.valueOf(minFrequency));
	}

}
