package de.pansoft.lucene.search.traversal;

public class TraverserContext {

    private final Float boostExactTokens;

    private TraverserContext(final Float boostExactTokens) {
        this.boostExactTokens = boostExactTokens;
    }

    public Float getBoostExactTokens() {
        return boostExactTokens;
    }

    public static TraverserContext getContext(final Float boostExactTokens) {
        return new TraverserContext(boostExactTokens);
    }

    public static TraverserContext getContext() {
        return new TraverserContext(null);
    }
}
