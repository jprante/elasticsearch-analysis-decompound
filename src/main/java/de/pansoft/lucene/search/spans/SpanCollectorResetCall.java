package de.pansoft.lucene.search.spans;

import java.io.IOException;

import org.apache.lucene.search.spans.SpanCollector;

public class SpanCollectorResetCall implements SpanCollectorCall {

	@Override
	public void call(SpanCollector collector) throws IOException {
		if (collector != null) {
			collector.reset();
		}
	}

}
