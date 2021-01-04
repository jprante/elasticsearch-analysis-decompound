package de.pansoft.lucene.search.spans;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanCollector;

public class SpanCollectorRecorder implements SpanCollector {

	private static final SpanCollectorCall RESET_CALL = new SpanCollectorResetCall();
	
	private Queue<SpanCollectorCall> calls = null;

	@Override
	public void collectLeaf(PostingsEnum postings, int position, Term term) throws IOException {
		if (calls == null) {
			calls = new LinkedList<SpanCollectorCall>();
		}
		calls.add(new SpanCollectorCollectLeafCall(postings, position, term));
	}

	@Override
	public void reset() {
		if (calls == null) {
			calls = new LinkedList<SpanCollectorCall>();
		}
		calls.add(RESET_CALL);
	}
	
	public void collect(SpanCollector collector) throws IOException {
		while (calls != null && !calls.isEmpty()) {
			SpanCollectorCall call = calls.poll();
			call.call(collector);
		}
	}
}
