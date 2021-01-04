package de.pansoft.lucene.index.query.frequency;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

public final class MinFrequencyMultiPostingsEnum extends PostingsEnum {

	private static final class TermStats {
		private final String term;
		private final int docFreq;
		private final long totalTermFreq;

		TermStats(BytesRef term, int docFreq, long totalTermFreq) {
			this.term = term.utf8ToString();
			this.docFreq = docFreq;
			this.totalTermFreq = totalTermFreq;
		}
		
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(term).append('(').append(docFreq).append(',').append(totalTermFreq).append(')');
			return builder.toString();
		}
	}

	private final int minFrequency;
	private final PostingsEnum[] postingsEnums;
	private final TermStats[] termStats;

	int doc = -1;

	public MinFrequencyMultiPostingsEnum(TermsEnum termsEnum, int minFrequency) throws IOException {
		this.minFrequency = minFrequency;
		Map<TermStats, PostingsEnum> postingsMap = new HashMap<>();
		while (termsEnum.next() != null) {
			if (termsEnum.docFreq() > 0) {
				postingsMap.put(new TermStats(termsEnum.term(), termsEnum.docFreq(), termsEnum.totalTermFreq()),
						termsEnum.postings(null, PostingsEnum.FREQS));
			}
		}
		final List<TermStats> termStatsList = postingsMap.keySet().stream()
				.sorted((s, t) -> Integer.compare(t.docFreq, s.docFreq)).collect(Collectors.toList());
		this.termStats = termStatsList.toArray(new TermStats[termStatsList.size()]);
		this.postingsEnums = termStatsList.stream().map((t) -> postingsMap.get(t)).collect(Collectors.toList()).toArray(new PostingsEnum[termStatsList.size()]);
	}

	@Override
	public int freq() throws IOException {
		int freq = 0;
		for (PostingsEnum postingsEnum : postingsEnums) {
			if (postingsEnum.docID() < doc) {
				postingsEnum.advance(doc);
			}
			if (postingsEnum.docID() == doc) {
				freq += postingsEnum.freq();
			}
		}
		return freq;
	}

	@Override
	public int docID() {
		return doc;
	}

	@Override
	public int advance(int target) throws IOException {
		int doc = this.doc;
		do {
			int nextMinDoc = NO_MORE_DOCS;
			for (PostingsEnum postingsEnum : postingsEnums) {
				if (postingsEnum.docID() < target) {
					postingsEnum.advance(target);
				}
				if (postingsEnum.docID() < nextMinDoc) {
					nextMinDoc = postingsEnum.docID();
				}
				if (nextMinDoc == target) {
					break;
				}
			}
			doc = nextMinDoc;
		} while (!assertMinFrequency(doc));
		this.doc = doc;
		return this.doc;
	}

	@Override
	public int nextDoc() throws IOException {
		int doc = this.doc;
		do {
			int nextMinDoc = NO_MORE_DOCS;
			for (PostingsEnum postingsEnum : postingsEnums) {
				if (postingsEnum.docID() < doc) {
					postingsEnum.advance(doc + 1);
				} else if (postingsEnum.docID() == doc) {
					postingsEnum.nextDoc();
				}
				if (postingsEnum.docID() < nextMinDoc) {
					nextMinDoc = postingsEnum.docID();
				}
				if (nextMinDoc == doc + 1) {
					break;
				}
			}
			doc = nextMinDoc;
		} while (!assertMinFrequency(doc));
		this.doc = doc;
		return this.doc;
	}

	private boolean assertMinFrequency(int nextMinDoc) throws IOException {
		if (nextMinDoc == NO_MORE_DOCS) {
			return true;
		}
		int freq = 0;
		for (PostingsEnum postingsEnum : postingsEnums) {
			if (postingsEnum.docID() < nextMinDoc) {
				postingsEnum.advance(nextMinDoc);
			}
			if (postingsEnum.docID() == nextMinDoc) {
				freq += postingsEnum.freq();
				if (freq >= minFrequency) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public int nextPosition() throws IOException {
		throw new IllegalStateException();
	}

	@Override
	public int startOffset() throws IOException {
		throw new IllegalStateException();
	}

	@Override
	public int endOffset() throws IOException {
		throw new IllegalStateException();
	}

	@Override
	public BytesRef getPayload() throws IOException {
		throw new IllegalStateException();
	}

	@Override
	public long cost() {
		long cost = 0;
		for (PostingsEnum postingsEnum : postingsEnums) {
			if (postingsEnum.docID() < doc) {
				try {
					postingsEnum.advance(doc);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			if (postingsEnum.docID() == doc) {
				cost += postingsEnum.cost();
			}
		}
		return cost;
	}

	@Override
	public String toString() {
		return "MinFrequencyMultiPostingsEnum(" + Arrays.toString(termStats) + ")";
	}
}
