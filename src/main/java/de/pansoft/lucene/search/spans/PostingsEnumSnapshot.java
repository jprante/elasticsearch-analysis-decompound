package de.pansoft.lucene.search.spans;

import java.io.IOException;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.util.BytesRef;

public class PostingsEnumSnapshot extends PostingsEnum {

	final int docID;
	int freq;
	int startOffset = -1;
	int endOffset = -1;
	BytesRef payload = null;
	long cost;

	public PostingsEnumSnapshot(PostingsEnum postings) {
		this.docID = postings.docID();
		try {
			this.freq = postings.freq();
		} catch (Exception e) {
			// ignore
		}
		try {
			this.startOffset = postings.startOffset();
		} catch (Exception e) {
			// ignore
		}
		try {
			this.endOffset = postings.endOffset();
		} catch (Exception e) {
			// ignore
		}
		try {
			this.payload = postings.getPayload();
		} catch (Exception e) {
			// ignore
		}
		try {
			this.cost = postings.cost();
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	public int freq() throws IOException {
		return freq;
	}

	@Override
	public int nextPosition() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int startOffset() throws IOException {
		return startOffset;
	}

	@Override
	public int endOffset() throws IOException {
		return endOffset;
	}

	@Override
	public BytesRef getPayload() throws IOException {
		return payload;
	}

	@Override
	public int docID() {
		return docID;
	}

	@Override
	public int nextDoc() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int advance(int target) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long cost() {
		return cost;
	}

}
