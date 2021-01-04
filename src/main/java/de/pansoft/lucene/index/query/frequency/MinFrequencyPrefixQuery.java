package de.pansoft.lucene.index.query.frequency;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.AttributeSource;

public class MinFrequencyPrefixQuery extends PrefixQuery {

	final int minFrequency;
	
	public MinFrequencyPrefixQuery(Term prefix, int minFrequency) {
		super(prefix);
		this.minFrequency = minFrequency;
		this.rewriteMethod = new RewriteMethod() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public Query rewrite(IndexReader reader, MultiTermQuery query) {
				return new MinFrequencyMultiTermQueryConstantScoreWrapper((MinFrequencyPrefixQuery) query,
						minFrequency);
			}
		};
	}
	
	public int getMinFrequency() {
		return minFrequency;
	}

	@Override
	public TermsEnum getTermsEnum(Terms terms, AttributeSource atts) throws IOException {
		return super.getTermsEnum(terms, atts);
	}
	
	@Override
	public String toString(String field) {
		StringBuilder builder = new StringBuilder();
		builder.append(super.toString(field));
		builder.append("/a").append(minFrequency);
		return builder.toString();
	}
}
