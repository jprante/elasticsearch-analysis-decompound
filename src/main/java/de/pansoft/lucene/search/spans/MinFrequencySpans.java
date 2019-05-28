package de.pansoft.lucene.search.spans;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;

import org.apache.lucene.search.TwoPhaseIterator;
import org.apache.lucene.search.spans.SpanCollector;
import org.apache.lucene.search.spans.Spans;

public class MinFrequencySpans extends Spans {

	protected final Spans in;
	protected final int minFrequency;

	private final Queue<Position> positionsInCurrentDoc = new LinkedList<>();
	private Position currentPositionInCurrentDoc = null;

	protected MinFrequencySpans(Spans in, int minFrequency) {
		this.in = Objects.requireNonNull(in);
		this.minFrequency = minFrequency;
	}

	@Override
	public final int nextDoc() throws IOException {
		while (true) {
			int doc = in.nextDoc();
			if (doc == NO_MORE_DOCS) {
				return NO_MORE_DOCS;
			} else if (twoPhaseCurrentDocMatches()) {
				return doc;
			}
		}
	}

	@Override
	public final int advance(int target) throws IOException {
		int doc = in.advance(target);
		while (doc != NO_MORE_DOCS) {
			if (twoPhaseCurrentDocMatches()) {
				break;
			}
			doc = in.nextDoc();
		}

		return doc;
	}

	@Override
	public final int docID() {
		return in.docID();
	}

	@Override
	public final int nextStartPosition() throws IOException {
		if (!positionsInCurrentDoc.isEmpty()) {
			currentPositionInCurrentDoc = positionsInCurrentDoc.poll();
			return currentPositionInCurrentDoc.start;
		} else {
			currentPositionInCurrentDoc = null;
			return in.nextStartPosition();
		}
	}

	@Override
	public final int startPosition() {
		return currentPositionInCurrentDoc != null ? currentPositionInCurrentDoc.start : (positionsInCurrentDoc.isEmpty()) ? in.startPosition() : -1;
	}

	@Override
	public final int endPosition() {
		return currentPositionInCurrentDoc != null ? currentPositionInCurrentDoc.end : (positionsInCurrentDoc.isEmpty()) ? in.endPosition() : -1;
	}

	@Override
	public int width() {
		return currentPositionInCurrentDoc != null ? currentPositionInCurrentDoc.width : (positionsInCurrentDoc.isEmpty()) ? in.width() : -1;
	}

	@Override
	public void collect(SpanCollector collector) throws IOException {
		if (currentPositionInCurrentDoc != null) {
			currentPositionInCurrentDoc.collect(collector);
		} else {
			in.collect(collector);
		}
	}

	@Override
	public final long cost() {
		return in.cost();
	}

	@Override
	public String toString() {
		return "MinFrequencySpans(" + in.toString() + "," + minFrequency + ")";
	}

	@Override
	public final TwoPhaseIterator asTwoPhaseIterator() {
		TwoPhaseIterator inner = in.asTwoPhaseIterator();
		if (inner != null) {
			// wrapped instance has an approximation
			return new TwoPhaseIterator(inner.approximation()) {
				@Override
				public boolean matches() throws IOException {
					return inner.matches() && twoPhaseCurrentDocMatches();
				}

				@Override
				public float matchCost() {
					return inner.matchCost(); // underestimate
				}

				@Override
				public String toString() {
					return "MinFrequencySpans@asTwoPhaseIterator(inner=" + inner + ", in=" + in + ")";
				}
			};
		} else {
			// wrapped instance has no approximation, but
			// we can still defer matching until absolutely needed.
			return new TwoPhaseIterator(in) {

				@Override
				public boolean matches() throws IOException {
					return twoPhaseCurrentDocMatches();
				}

				@Override
				public float matchCost() {
					return in.positionsCost(); // overestimate
				}

				@Override
				public String toString() {
					return "MinFrequencySpans@asTwoPhaseIterator(in=" + in + ")";
				}

			};
		}
	}

	@Override
	public float positionsCost() {
		throw new UnsupportedOperationException(); // asTwoPhaseIterator never returns null
	}

	private final boolean twoPhaseCurrentDocMatches() throws IOException {
		assert positionsInCurrentDoc.isEmpty();
		for (;;) {
			int startPos = in.nextStartPosition();
			if (startPos != NO_MORE_POSITIONS) {
				Position postition = new Position(startPos, in.endPosition(), in.width());
				in.collect(postition);
				positionsInCurrentDoc.add(postition);
				if (positionsInCurrentDoc.size() >= minFrequency) {
					return true;
				}
			} else {
				break;
			}
		}
		positionsInCurrentDoc.clear();
		return false;
	}

	private static class Position extends SpanCollectorRecorder {
		private final int start;
		private final int end;
		private final int width;

		Position(int start, int end, int width) {
			this.start = start;
			this.end = end;
			this.width = width;
		}
	}
}
