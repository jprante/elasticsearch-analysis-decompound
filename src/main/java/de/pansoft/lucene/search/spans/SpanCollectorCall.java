package de.pansoft.lucene.search.spans;

import java.io.IOException;

import org.apache.lucene.search.spans.SpanCollector;

public interface SpanCollectorCall {

	public void call(SpanCollector collector) throws IOException;
}
