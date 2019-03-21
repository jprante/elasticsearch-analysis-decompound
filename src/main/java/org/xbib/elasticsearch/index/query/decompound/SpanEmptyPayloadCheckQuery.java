package org.xbib.elasticsearch.index.query.decompound;

import java.util.Collections;

import org.apache.lucene.queries.payloads.SpanPayloadCheckQuery;
import org.apache.lucene.search.spans.SpanQuery;

public class SpanEmptyPayloadCheckQuery extends SpanPayloadCheckQuery {

	public SpanEmptyPayloadCheckQuery(SpanQuery match) {
		super(match, Collections.singletonList(null));
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
