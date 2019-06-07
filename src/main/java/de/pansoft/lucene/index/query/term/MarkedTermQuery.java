package de.pansoft.lucene.index.query.term;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.io.IOException;

public class MarkedTermQuery extends TermQuery {

    public enum Context {
        PHRASE
    }

    private final Context context;

    public MarkedTermQuery(Term t, Context context) {
        super(t);
        this.context = context;
    }

    @Override
    public Query rewrite(IndexReader reader) throws IOException {
        return new TermQuery(this.getTerm());
    }

    public Context getContext() {
        return context;
    }

}
