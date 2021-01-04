package de.pansoft.lucene.queryparser.classic;

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

   protected String discardEscapeChar(String input) throws ParseException {
      return super.discardEscapeChar(input);
   }

   protected Query handleBoost(Query q, Token boost) {
      return super.handleBoost(q, boost);
   }

   protected Query handleBareTokenQuery(String qfield, Token term, Token fuzzySlop, Token frequency,
                                        boolean prefix, boolean wildcard, boolean fuzzy, boolean regexp, boolean hasFrequency) throws ParseException {
      if (!hasFrequency) {
         return super.handleBareTokenQuery(qfield, term, fuzzySlop, prefix, wildcard, fuzzy, regexp);
      } else {
         int minFrequency = 2;
         if (frequency != null && !frequency.image.isEmpty()) {
            minFrequency = Integer.parseInt(frequency.image);
         }
         if (!prefix) {
            Token newTerm = Token.newToken(0, term.image.substring(0, term.image.length()-1));
            Query query = super.handleBareTokenQuery(qfield, newTerm, fuzzySlop, false, wildcard, fuzzy, regexp);
            return transformTermToFrequencyQuery(query, minFrequency);
         } else {
            Token newTerm = Token.newToken(0, term.image.substring(0, term.image.length()-2));
            Query query = super.handleBareTokenQuery(qfield, newTerm, fuzzySlop, true, wildcard, fuzzy, regexp);
            return transformPrefixToFrequencyPrefixQuery(query, minFrequency);
         }
      }
   }

   protected Query handleQuotedTerm(String qfield, Token term, Token fuzzySlop) throws ParseException {
      return super.handleQuotedTerm(qfield, term, fuzzySlop);
   }

   protected Query handleBareFuzzy(String qfield, Token fuzzySlop, String termImage) throws ParseException {
      return super.handleBareFuzzy(qfield, fuzzySlop, termImage);
   }

   protected Query transformTermToFrequencyQuery(Query query, int minFrequency) {
      return query;
   }


   protected Query transformPrefixToFrequencyPrefixQuery(Query query, int minFrequency) {
      return query;
   }
}
