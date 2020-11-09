package de.pansoft.lucene.search.traversal;

public class TraverserContext {

    private final Float boostExactTokens;
    private final TraversalPhase traversalPhase;

    private TraverserContext(final Float boostExactTokens, final TraversalPhase traversalPhase) {
        this.boostExactTokens = boostExactTokens;
        this.traversalPhase = traversalPhase;
    }

    public Float getBoostExactTokens() {
        return boostExactTokens;
    }

    public TraversalPhase getTraversalPhase() {
        return traversalPhase;
    }

    public TraverserContext inTraversalPhase(TraversalPhase traversalPhase) {
        return new TraverserContext(this.boostExactTokens, traversalPhase);
    }

    public static TraverserContext getContext(final Float boostExactTokens) {
        return new TraverserContext(boostExactTokens, TraversalPhase.BUILD_PHASE);
    }

    public static TraverserContext getContext() {
        return new TraverserContext(null, TraversalPhase.BUILD_PHASE);
    }
}
