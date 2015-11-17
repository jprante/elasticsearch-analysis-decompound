/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.xbib.elasticsearch.index.analysis.decompound;

import java.io.IOException;
import java.util.LinkedList;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

public class DecompoundTokenFilter extends TokenFilter {

    protected final LinkedList<DecompoundToken> tokens;
    protected final Decompounder decomp;
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);
    private AttributeSource.State current;
    private boolean respectKeywords = false;
    private boolean subwordsonly = false;

    protected DecompoundTokenFilter(TokenStream input, Decompounder decomp, boolean respectKeywords, boolean subwordsonly) {
        super(input);
        this.tokens = new LinkedList<DecompoundToken>();
        this.decomp = decomp;
        this.respectKeywords = respectKeywords;
        this.subwordsonly = subwordsonly;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            DecompoundToken token = tokens.removeFirst();
            restoreState(current);
            termAtt.setEmpty().append(token.txt);
            offsetAtt.setOffset(token.startOffset, token.endOffset);
            posIncAtt.setPositionIncrement(0);
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
        for (String s : decomp.decompound(term)) {
            tokens.add(new DecompoundToken(s, start, len));
        }
        return tokens.isEmpty();
    }


    @Override
    public void reset() throws IOException {
        super.reset();
        tokens.clear();
        current = null;
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
