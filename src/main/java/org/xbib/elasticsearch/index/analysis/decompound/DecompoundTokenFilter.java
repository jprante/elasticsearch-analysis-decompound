package org.xbib.elasticsearch.index.analysis.decompound;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

/**
 *
 */
public class DecompoundTokenFilter extends TokenFilter {

	private static final Logger LOG = LogManager.getLogger(DecompoundTokenFilter.class);
	
    private static ConcurrentHashMap<String, String[]> TERM_CACHE;
    
    private static final AtomicLong termCacheCount = new AtomicLong(0);
    
    private static final AtomicBoolean needsClearCache = new AtomicBoolean(false);
    
    private static long MAX_CACHE_SIZE;
    
    private static final String[] NO_TERMS = new String[0];
	
    private final LinkedList<DecompoundToken> tokens;

    private final Decompounder decomp;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);

    private final boolean respectKeywords;

    private final boolean subwordsonly;

    private AttributeSource.State current;

    protected DecompoundTokenFilter(TokenStream input, Decompounder decomp, boolean respectKeywords, boolean subwordsonly, long maxCacheSize) {
        super(input);
        this.tokens = new LinkedList<>();
        this.decomp = decomp;
        this.respectKeywords = respectKeywords;
        this.subwordsonly = subwordsonly;
        if (TERM_CACHE == null) {
    			TERM_CACHE = new ConcurrentHashMap<String, String[]>();
    			MAX_CACHE_SIZE = maxCacheSize;
        }
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            assert current != null;
            DecompoundToken token = tokens.removeFirst();
            restoreState(current);
            termAtt.setEmpty().append(token.txt);
            offsetAtt.setOffset(token.startOffset, token.endOffset);
            if (!subwordsonly) {
                posIncAtt.setPositionIncrement(0);
            }
            return true;
        }
        if (!input.incrementToken()) {
            return false;
        }
        if (respectKeywords && keywordAtt.isKeyword()) {
            return true;
        }
        if (!decompound()) {
            current = captureState();
            if (subwordsonly) {
            		DecompoundToken token = tokens.removeFirst();
                restoreState(current);
                termAtt.setEmpty().append(token.txt);
                offsetAtt.setOffset(token.startOffset, token.endOffset);
                return true;
            }
        }
        return true;
    }

    protected boolean decompound() {
		int start = offsetAtt.startOffset();
		int len = termAtt.length();
		String term = new String(termAtt.buffer(), 0, len);
        if (needsClearCache.get()) {
        		clearCache();
        }
		String[] cachedTerms = TERM_CACHE.computeIfAbsent(term, t -> {
			if (termCacheCount.incrementAndGet() > MAX_CACHE_SIZE) {
				needsClearCache.set(true);
			}
			List<String> decompound = decomp.decompound(t);
			if (decompound.isEmpty()) {
				return NO_TERMS;
			}
			return decompound.toArray(new String[decompound.size()]);
		});

		for (String s : cachedTerms) {
			tokens.add(new DecompoundToken(s, start, len));
		}
		return tokens.isEmpty();
    }
    
	private void clearCache() {
		needsClearCache.set(false);
		final Runtime runtime = Runtime.getRuntime();
		long memoryUsage = runtime.totalMemory() - runtime.freeMemory();
		TERM_CACHE = new ConcurrentHashMap<String, String[]>();
		termCacheCount.set(0);
		LOG.debug("Clearing term cache for decompound, memory usage: " + memoryUsage);
	}

    @Override
    public void reset() throws IOException {
        super.reset();
        tokens.clear();
        current = null;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof DecompoundTokenFilter &&
                tokens.equals(((DecompoundTokenFilter)object).tokens) &&
                respectKeywords == ((DecompoundTokenFilter)object).respectKeywords &&
                subwordsonly == ((DecompoundTokenFilter)object).subwordsonly;
    }

    @Override
    public int hashCode() {
        return tokens.hashCode() ^ Boolean.hashCode(respectKeywords) ^ Boolean.hashCode(subwordsonly);
    }
    
    protected class DecompoundToken {

        public final CharSequence txt;
        public final int startOffset;
        public final int endOffset;

        public DecompoundToken(CharSequence txt, int offset, int length) {
            this.txt = txt;
            int startOff = DecompoundTokenFilter.this.offsetAtt.startOffset();
            int endOff = DecompoundTokenFilter.this.offsetAtt.endOffset();
            if (endOff - startOff != DecompoundTokenFilter.this.termAtt.length()) {
                this.startOffset = startOff;
                this.endOffset = endOff;
            } else {
                this.startOffset = offset;
                this.endOffset = offset + length;
            }
        }
    }
}
