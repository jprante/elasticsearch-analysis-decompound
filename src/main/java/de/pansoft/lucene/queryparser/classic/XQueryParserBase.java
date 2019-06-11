package de.pansoft.lucene.queryparser.classic;

import de.pansoft.lucene.index.query.frequency.MinFrequencyPrefixQuery;
import de.pansoft.lucene.index.query.frequency.MinFrequencyTermQuery;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.Token;
import org.apache.lucene.search.Query;

public abstract class XQueryParserBase extends QueryParserBase {

   protected static final int CONJ_NONE = 0;
   protected static final int CONJ_AND = 1;
   protected static final int CONJ_OR = 2;

   protected static final int MOD_NONE = 0;
   protected static final int MOD_NOT = 10;
   protected static final int MOD_REQ = 11;

   protected String discardEscapeChar(String input) throws org.apache.lucene.queryparser.classic.ParseException {
      return super.discardEscapeChar(input);
   }

   protected Query handleBoost(Query q, org.apache.lucene.queryparser.classic.Token boost) {
      return super.handleBoost(q, boost);
   }

   protected Query handleBareTokenQuery(String qfield, org.apache.lucene.queryparser.classic.Token term, org.apache.lucene.queryparser.classic.Token fuzzySlop, org.apache.lucene.queryparser.classic.Token freqency,
                                        boolean prefix, boolean wildcard, boolean fuzzy, boolean regexp, boolean hasFrequency) throws org.apache.lucene.queryparser.classic.ParseException {
      if (!hasFrequency) {
         return super.handleBareTokenQuery(qfield, term, fuzzySlop, prefix, wildcard, fuzzy, regexp);
      } else {
         if (!prefix) {
            return handleFreqencyQuery(qfield, freqency, term.image.substring(0, term.image.length()-1));
         } else {
            return handleFreqencyPrefixQuery(qfield, freqency, term.image.substring(0, term.image.length()-2));
         }
      }
   }

   protected Query handleFreqencyQuery(String qfield, org.apache.lucene.queryparser.classic.Token frequency, String termImage) {
      Term term = new Term(qfield, termImage);
      int minFrequency = 2;
      if (frequency != null && !frequency.image.isEmpty()) {
         minFrequency = Integer.parseInt(frequency.image);
      }
      return newFreqencyQuery(term, minFrequency);
   }

   protected Query handleFreqencyPrefixQuery(String qfield, org.apache.lucene.queryparser.classic.Token frequency, String termImage) {
      Term term = new Term(qfield, termImage);
      int minFrequency = 2;
      if (frequency != null && !frequency.image.isEmpty()) {
         minFrequency = Integer.parseInt(frequency.image);
      }
      return newFreqencyPrefixQuery(term, minFrequency);
   }

   protected Query newFreqencyQuery(Term term, int minFrequency) {
      return new MinFrequencyTermQuery(term, minFrequency);
   }

   protected Query newFreqencyPrefixQuery(Term term, int minFrequency) {
      return new MinFrequencyPrefixQuery(term, minFrequency);
   }

   protected Query handleQuotedTerm(String qfield, org.apache.lucene.queryparser.classic.Token term, org.apache.lucene.queryparser.classic.Token fuzzySlop) throws org.apache.lucene.queryparser.classic.ParseException {
      return super.handleQuotedTerm(qfield, term, fuzzySlop);
   }

   protected Query handleBareFuzzy(String qfield, Token fuzzySlop, String termImage) throws ParseException {
      return super.handleBareFuzzy(qfield, fuzzySlop, termImage);
   }
}
