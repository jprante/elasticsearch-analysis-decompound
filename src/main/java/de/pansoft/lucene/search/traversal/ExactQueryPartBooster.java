package de.pansoft.lucene.search.traversal;

import org.apache.lucene.search.*;

public class ExactQueryPartBooster {

    public static Query query(Query boostedQuery, Query defaultQuery, Float boost) {
        if (boost == null) {
            return defaultQuery;
        }
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.setMinimumNumberShouldMatch(1);
        builder.add(new BooleanClause(new BoostQuery(boostedQuery, boost), BooleanClause.Occur.SHOULD));
        builder.add(new BooleanClause(defaultQuery, BooleanClause.Occur.SHOULD));
        return builder.build();
    }
}
