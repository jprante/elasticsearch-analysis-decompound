package de.pansoft.lucene.index.query.frequency;

import java.io.IOException;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BulkScorer;
import org.apache.lucene.search.ConstantScoreScorer;
import org.apache.lucene.search.ConstantScoreWeight;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.DocIdSetBuilder;

final class MinFrequencyMultiTermQueryConstantScoreWrapper<Q extends MinFrequencyPrefixQuery> extends Query {

	protected final Q query;
	private final int minFrequency;

	protected MinFrequencyMultiTermQueryConstantScoreWrapper(Q query, int minFrequency) {
		this.query = query;
		this.minFrequency = minFrequency;
	}

	@Override
	public String toString(String field) {
		return query.toString(field);
	}

	@Override
	public final boolean equals(final Object other) {
		return sameClassAs(other) && query.equals(((MinFrequencyMultiTermQueryConstantScoreWrapper<?>) other).query)
				&& minFrequency == ((MinFrequencyMultiTermQueryConstantScoreWrapper<?>) other).minFrequency;
	}

	@Override
	public final int hashCode() {
		return 31 * classHash() + query.hashCode();
	}

	public Q getQuery() {
		return query;
	}

	public final String getField() {
		return query.getField();
	}
	
	public final int getMinFrequency() {
		return minFrequency;
	}

	@Override
	public Weight createWeight(IndexSearcher searcher, boolean needsScores, float boost) throws IOException {
		return new ConstantScoreWeight(this, boost) {

			private DocIdSet rewrite(LeafReaderContext context) throws IOException {
				final Terms terms = context.reader().terms(query.getField());
				if (terms == null) {
					return null;
				}

				final TermsEnum termsEnum = query.getTermsEnum(terms, new AttributeSource());
				assert termsEnum != null;

				PostingsEnum docs = new MinFrequencyMultiPostingsEnum(termsEnum, minFrequency);
				DocIdSetBuilder builder = new DocIdSetBuilder(context.reader().maxDoc(), terms);
				builder.add(docs);
				return builder.build();
			}

			private Scorer scorer(DocIdSet set) throws IOException {
				if (set == null) {
					return null;
				}
				final DocIdSetIterator disi = set.iterator();
				if (disi == null) {
					return null;
				}
				return new ConstantScoreScorer(this, score(), disi);
			}

			@Override
			public BulkScorer bulkScorer(LeafReaderContext context) throws IOException {
				final Scorer scorer = scorer(rewrite(context));
				if (scorer == null) {
					return null;
				}
				return new DefaultBulkScorer(scorer);
			}

			@Override
			public Scorer scorer(LeafReaderContext context) throws IOException {
				return scorer(rewrite(context));
			}

			@Override
			public boolean isCacheable(LeafReaderContext ctx) {
				return true;
			}

		};
	}
}
