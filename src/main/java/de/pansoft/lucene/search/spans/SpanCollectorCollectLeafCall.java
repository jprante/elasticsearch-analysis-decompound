package de.pansoft.lucene.search.spans;

import java.io.IOException;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanCollector;

public class SpanCollectorCollectLeafCall implements SpanCollectorCall {

	private final PostingsEnum postingsEnum;
	private final int position;
	private final Term term;

	public SpanCollectorCollectLeafCall(PostingsEnum postings, int position, Term term) {
		this.postingsEnum = new PostingsEnumSnapshot(postings);
		this.position = position;
		this.term = term;
	}

	@Override
	public void call(SpanCollector collector) throws IOException {
		if (collector != null) {
			collector.collectLeaf(postingsEnum, position, term);
		}
	}

}
