package de.pansoft.lucene.search.traversal;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.elasticsearch.index.query.QueryShardContext;

import java.io.IOException;

public class LazyTraverserQuery extends Query {

    private final QueryTraverser queryTraverser;
    private final TraverserContext traverserContext;
    private final QueryShardContext queryShardContext;
    private final Query query;

    public LazyTraverserQuery(QueryTraverser queryTraverser, TraverserContext traverserContext,
                              QueryShardContext queryShardContext, Query query) {
        this.queryTraverser = queryTraverser;
        this.traverserContext = traverserContext;
        this.queryShardContext = queryShardContext;
        this.query = query;
    }

    public Query rewrite(IndexReader reader) throws IOException {
        Query rewrittenQuery = this.query.rewrite(reader);
        if (!rewrittenQuery.equals(this.query)) {
           rewrittenQuery = this.queryTraverser.traverse(this.traverserContext, this.queryShardContext, rewrittenQuery);
        }
        return rewrittenQuery;
    }

    @Override
    public String toString(String field) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("LazyTraverserQuery(");
        buffer.append(this.query);
        buffer.append(")");
        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return this.query.equals(obj);
    }

    @Override
    public int hashCode() {
        return this.query.hashCode();
    }
}
