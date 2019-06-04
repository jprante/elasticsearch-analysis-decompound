package de.pansoft.lucene.search.spans;

import java.io.IOException;
import java.util.Collections;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.payloads.SpanPayloadCheckQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;

public class SpanEmptyPayloadCheckQuery extends SpanPayloadCheckQuery {

	public SpanEmptyPayloadCheckQuery(SpanQuery match) {
		super(match, Collections.singletonList(null));
	}
	
  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    Query matchRewritten = match.rewrite(reader);
    if (match != matchRewritten && matchRewritten instanceof SpanQuery) {
      return new SpanEmptyPayloadCheckQuery((SpanQuery)matchRewritten);
    }
    return super.rewrite(reader);
  }

  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("SpanEmptyPayloadCheckQuery(");
    buffer.append(match.toString(field));
    buffer.append(")");
    return buffer.toString();
  }
}
