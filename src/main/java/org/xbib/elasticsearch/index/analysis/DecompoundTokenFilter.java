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
package org.xbib.elasticsearch.index.analysis;

import java.io.IOException;
import java.util.LinkedList;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.xbib.elasticsearch.analysis.decompound.Decompounder;

public class DecompoundTokenFilter extends TokenFilter {

    protected final LinkedList<DecompoundToken> tokens;
    protected final Decompounder decomp;
    protected final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    protected final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final PositionIncrementAttribute posIncAtt = addAttribute(PositionIncrementAttribute.class);
    private AttributeSource.State current;

    protected DecompoundTokenFilter(TokenStream input, Decompounder decomp) {
        super(input);
        this.tokens = new LinkedList<DecompoundToken>();
        this.decomp = decomp;
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (!tokens.isEmpty()) {
            assert current != null;
            DecompoundToken token = tokens.removeFirst();
            restoreState(current);
            termAtt.setEmpty().append(token.txt);
            offsetAtt.setOffset(token.startOffset, token.endOffset);
            posIncAtt.setPositionIncrement(0);
            return true;
        }
        if (input.incrementToken()) {
            decompound();
            if (!tokens.isEmpty()) {
                current = captureState();
            }
            return true;
        } else {
            return false;
        }
    }

    protected void decompound() {
        int start = offsetAtt.startOffset();
        CharSequence term = new String(termAtt.buffer(), 0, termAtt.length());
        for (String s : decomp.decompound(term.toString())) {
            start = term.toString().indexOf(s, start) + 1;
            int len = s.length();
            tokens.add(new DecompoundToken(s, start, len));
            start += len;
        }
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
