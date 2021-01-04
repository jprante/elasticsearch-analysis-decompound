package de.pansoft.lucene.index.query.frequency;

import java.io.IOException;
import java.util.Objects;

import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.BytesRef;

public class MinFrequencyPostingsEnum extends PostingsEnum {

	final PostingsEnum td;
	final int minFrequency;

	public MinFrequencyPostingsEnum(PostingsEnum td, int minFrequency) {
		this.td = Objects.requireNonNull(td);
		this.minFrequency = minFrequency;
	}

	@Override
	public int freq() throws IOException {
		return td.freq();
	}

	@Override
	public int nextPosition() throws IOException {
		return td.nextPosition();
	}

	@Override
	public int startOffset() throws IOException {
		return td.startOffset();
	}

	@Override
	public int endOffset() throws IOException {
		return td.endOffset();
	}

	@Override
	public BytesRef getPayload() throws IOException {
		return td.getPayload();
	}

	@Override
	public int docID() {
		return td.docID();
	}

	@Override
	public int nextDoc() throws IOException {
		int docId = NO_MORE_DOCS;
		do {
			docId = td.nextDoc();
		} while (docId != NO_MORE_DOCS && freq() < minFrequency);
		return docId;
	}

	@Override
	public int advance(int target) throws IOException {
		int docId = td.advance(target);
		if (docId != NO_MORE_DOCS && freq() < minFrequency) {
			docId = nextDoc();
		}
		return docId;
	}

	@Override
	public long cost() {
		return td.cost();
	}

	@Override
	public AttributeSource attributes() {
		return td.attributes();
	}

}
