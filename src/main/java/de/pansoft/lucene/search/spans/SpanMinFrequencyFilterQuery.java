package de.pansoft.lucene.search.spans;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanWeight;
import org.apache.lucene.search.spans.Spans;

public class SpanMinFrequencyFilterQuery extends SpanQuery implements Cloneable {

	protected final SpanQuery match;
	protected final int minFrequency;

	public SpanMinFrequencyFilterQuery(SpanQuery match, int minFrequency) {
		this.match = Objects.requireNonNull(match);
		this.minFrequency = minFrequency;
	}

	public SpanQuery getMatch() {
		return match;
	}

	@Override
	public String getField() {
		return match.getField();
	}

	@Override
	public SpanWeight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
		SpanWeight matchWeight = match.createWeight(searcher, false, boost);
		return new MinFrequencySpanCountingCheckWeight(matchWeight, searcher,
				needsScores ? getTermContexts(matchWeight) : null, boost);
	}

	public class MinFrequencySpanCountingCheckWeight extends SpanWeight {

		final SpanWeight matchWeight;

		public MinFrequencySpanCountingCheckWeight(SpanWeight matchWeight, IndexSearcher searcher,
				Map<Term, TermContext> terms, float boost) throws IOException {
			super(SpanMinFrequencyFilterQuery.this, searcher, terms, boost);
			this.matchWeight = matchWeight;
		}

		@Override
		public void extractTerms(Set<Term> terms) {
			matchWeight.extractTerms(terms);
		}

		@Override
		public boolean isCacheable(LeafReaderContext ctx) {
			return matchWeight.isCacheable(ctx);
		}

		@Override
		public void extractTermContexts(Map<Term, TermContext> contexts) {
			matchWeight.extractTermContexts(contexts);
		}

		@Override
		public Spans getSpans(final LeafReaderContext context, Postings requiredPostings) throws IOException {
			Spans matchSpans = matchWeight.getSpans(context, requiredPostings);
			return (matchSpans == null) ? null : new MinFrequencySpans(matchSpans, minFrequency);
		}

	}

	@Override
	public Query rewrite(IndexReader reader) throws IOException {
		SpanQuery rewritten = (SpanQuery) match.rewrite(reader);
		if (rewritten != match) {
			SpanMinFrequencyFilterQuery clone = new SpanMinFrequencyFilterQuery(rewritten, minFrequency);
			return clone;
		}
		return super.rewrite(reader);
	}

	@Override
	public boolean equals(Object other) {
		return sameClassAs(other) && match.equals(((SpanMinFrequencyFilterQuery) other).match)
				&& minFrequency == ((SpanMinFrequencyFilterQuery) other).minFrequency;
	}

	@Override
	public int hashCode() {
		return classHash() ^ match.hashCode() ^ Integer.hashCode(minFrequency);
	}

	@Override
	public String toString(String field) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("spanMinFrequencyFilter(");
		buffer.append(match.toString(field));
		buffer.append(", ");
		buffer.append(minFrequency);
		buffer.append(")");
		return buffer.toString();
	}
}
