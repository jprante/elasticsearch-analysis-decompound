package de.pansoft.lucene.index.query.frequency;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;

import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.TermState;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.Weight;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.search.similarities.Similarity.SimScorer;

public class MinFrequencyTermQuery extends Query {

	private final Term term;
	private final int minFrequency;
	private final TermContext perReaderTermState;

	final class MinFrequencyTermWeight extends Weight {
		private final Similarity similarity;
		private final Similarity.SimWeight stats;
		private final TermContext termStates;

		public MinFrequencyTermWeight(IndexSearcher searcher, boolean needsScores, float boost, TermContext termStates)
				throws IOException {
			super(MinFrequencyTermQuery.this);
			this.termStates = Objects.requireNonNull(termStates);
			this.similarity = searcher.getSimilarity(needsScores);

			final CollectionStatistics collectionStats;
			final TermStatistics termStats;
			collectionStats = searcher.collectionStatistics(term.field());
			termStats = searcher.termStatistics(term, termStates);

			this.stats = similarity.computeWeight(boost, collectionStats, termStats);
		}

		@Override
		public void extractTerms(Set<Term> terms) {
			terms.add(getTerm());
		}

		@Override
		public String toString() {
			return "weight(" + MinFrequencyTermWeight.this + ")";
		}

		@Override
		public Scorer scorer(LeafReaderContext context) throws IOException {
			assert termStates == null || termStates.wasBuiltFor(ReaderUtil.getTopLevelContext(
					context)) : "The top-reader used to create Weight is not the same as the current reader's top-reader ("
							+ ReaderUtil.getTopLevelContext(context);
			final TermsEnum termsEnum = getTermsEnum(context);
			if (termsEnum == null) {
				return null;
			}
			PostingsEnum docs = new MinFrequencyPostingsEnum(termsEnum.postings(null, PostingsEnum.FREQS), MinFrequencyTermQuery.this.minFrequency);
			return new MinFrequencyTermScorer(this, docs, similarity.simScorer(stats, context));
		}

		@Override
		public boolean isCacheable(LeafReaderContext ctx) {
			return true;
		}

		/**
		 * Returns a {@link TermsEnum} positioned at this weights Term or null if the
		 * term does not exist in the given context
		 */
		private TermsEnum getTermsEnum(LeafReaderContext context) throws IOException {
			assert termStates.wasBuiltFor(ReaderUtil.getTopLevelContext(
					context)) : "The top-reader used to create Weight is not the same as the current reader's top-reader ("
							+ ReaderUtil.getTopLevelContext(context);
			final TermState state = termStates.get(context.ord);
			if (state == null) { // term is not present in that reader
				assert termNotInReader(context.reader(), term) : "no termstate found but term exists in reader term="
						+ term;
				return null;
			}
			final TermsEnum termsEnum = context.reader().terms(term.field()).iterator();
			termsEnum.seekExact(term.bytes(), state);
			return termsEnum;
		}

		private boolean termNotInReader(LeafReader reader, Term term) throws IOException {
			// only called from assert
			return reader.docFreq(term) == 0;
		}

		@Override
		public Explanation explain(LeafReaderContext context, int doc) throws IOException {
			MinFrequencyTermScorer scorer = (MinFrequencyTermScorer) scorer(context);
			if (scorer != null) {
				int newDoc = scorer.iterator().advance(doc);
				if (newDoc == doc) {
					float freq = scorer.freq();
					SimScorer docScorer = similarity.simScorer(stats, context);
					Explanation freqExplanation = Explanation.match(freq, "termFreq=" + freq);
					Explanation scoreExplanation = docScorer.explain(doc, freqExplanation);
					return Explanation.match(scoreExplanation.getValue(), "weight(" + getQuery() + " in " + doc + ") ["
							+ similarity.getClass().getSimpleName() + "], result of:", scoreExplanation);
				}
			}
			return Explanation.noMatch("no matching term");
		}
	}

	public MinFrequencyTermQuery(Term term, int minFrequency) {
		this.term = term = Objects.requireNonNull(term);
		this.minFrequency = minFrequency;
		this.perReaderTermState = null;
	}

	public MinFrequencyTermQuery(Term term, int minFrequency, TermContext perReaderTermState) {
		this.term = term = Objects.requireNonNull(term);
		this.minFrequency = minFrequency;
		this.perReaderTermState = Objects.requireNonNull(perReaderTermState);
	}

	@Override
	public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
		final IndexReaderContext context = searcher.getTopReaderContext();
		final TermContext termState;
		if (perReaderTermState == null || perReaderTermState.wasBuiltFor(context) == false) {
			termState = TermContext.build(context, term);
		} else {
			termState = this.perReaderTermState;
		}
		return new MinFrequencyTermWeight(searcher, needsScores, boost, termState);
	}

	@Override
	public String toString(String field) {
		StringBuilder buffer = new StringBuilder();
		if (!term.field().equals(field)) {
			buffer.append(term.field());
			buffer.append(":");
		}
		buffer.append(term.text());
		buffer.append("/a").append(minFrequency);
		return buffer.toString();
	}

	@Override
	public boolean equals(Object other) {
		return sameClassAs(other) && term.equals(((MinFrequencyTermQuery) other).term)
				&& minFrequency == ((MinFrequencyTermQuery) other).minFrequency;
	}

	@Override
	public int hashCode() {
		return classHash() ^ term.hashCode() + minFrequency;
	}

	public Term getTerm() {
		return term;
	}

	public int getMinFrequency() {
		return minFrequency;
	}

}
