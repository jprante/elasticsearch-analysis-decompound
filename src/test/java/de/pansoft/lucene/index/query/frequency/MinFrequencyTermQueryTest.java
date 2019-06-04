package de.pansoft.lucene.index.query.frequency;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.MockAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.RandomIndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryUtils;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.TestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MinFrequencyTermQueryTest extends LuceneTestCase {

	protected static Directory dir;
	protected static Analyzer anlzr;
	protected static final int N_DOCS = 13; // select a primary number > 2

	protected static final String ID_FIELD = "id";
	protected static final String TEXT_FIELD = "text";

	private static final String DOC_TEXT_LINES[] = { 
			"Well, this is just some plain text we use for creating the ",
			"test documents. It used to be a text from an online collection ",
			"devoted to first aid, but if there was there an (online) lawyers that and those ",
			"first aid collection with legal advices, \"it\" might have quite ",
			"probably advised one not to include \"it\"'s text or the text of ",
			"any other online collection in one's code, unless one has money ",
			"that one don't need and one is happy to donate for lawyers ",
			"charity. Anyhow at some point, rechecking the usage of this text, ",
			"it became uncertain that this text is free to use, because ",
			"the web site in the disclaimer of he eBook containing that text ",
			"was not responding anymore, and at the same time, in projGut, ",
			"searching for first aid no longer found that eBook as well. ",
			"So here we are, with a perhaps much less interesting ", 
			"text for the test, but oh much much safer. ", };

	@AfterClass
	public static void afterClassFunctionTestSetup() throws Exception {
		dir.close();
		dir = null;
		anlzr.close();
		anlzr = null;
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		createIndex(true);
	}

	protected static void createIndex(boolean doMultiSegment) throws Exception {
		if (VERBOSE) {
			System.out.println("TEST: setUp");
		}
		// prepare a small index with just a few documents.
		dir = newDirectory();
		anlzr = new MockAnalyzer(random());
		IndexWriterConfig iwc = newIndexWriterConfig(anlzr).setMergePolicy(newLogMergePolicy());
		if (doMultiSegment) {
			iwc.setMaxBufferedDocs(TestUtil.nextInt(random(), 2, 7));
		}
		RandomIndexWriter iw = new RandomIndexWriter(random(), dir, iwc);
		// add docs not exactly in natural ID order, to verify we do check the order of
		// docs by scores
		int remaining = N_DOCS;
		boolean done[] = new boolean[N_DOCS];
		int i = 0;
		while (remaining > 0) {
			if (done[i]) {
				throw new Exception(
						"to set this test correctly N_DOCS=" + N_DOCS + " must be primary and greater than 2!");
			}
			addDoc(iw, i);
			done[i] = true;
			i = (i + 4) % N_DOCS;
			remaining--;
		}
		if (!doMultiSegment) {
			if (VERBOSE) {
				System.out.println("TEST: setUp full merge");
			}
			iw.forceMerge(1);
		}
		iw.close();
		if (VERBOSE) {
			System.out.println("TEST: setUp done close");
		}
	}

	private static void addDoc(RandomIndexWriter iw, int i) throws Exception {
		Document d = new Document();
		Field f;
		int scoreAndID = i + 1;

		FieldType customType = new FieldType(TextField.TYPE_STORED);
		customType.setTokenized(false);
		customType.setOmitNorms(true);

		f = newField(ID_FIELD, id2String(scoreAndID), customType); // for debug purposes
		d.add(f);
		d.add(new SortedDocValuesField(ID_FIELD, new BytesRef(id2String(scoreAndID))));

		FieldType customType2 = new FieldType(TextField.TYPE_NOT_STORED);
		customType2.setOmitNorms(true);
		f = newField(TEXT_FIELD, "text of doc" + scoreAndID + textLine(i), customType2); // for regular search
		d.add(f);

		log("adding: " + d);
		iw.addDocument(d);
	}

	protected static String id2String(int scoreAndID) {
		String s = "000000000" + scoreAndID;
		int n = ("" + N_DOCS).length() + 3;
		int k = s.length() - n;
		return "ID" + s.substring(k);
	}

	protected static int string2id(String idString) {
		StringBuilder builder = new StringBuilder(idString.substring(2));
		while(builder.charAt(0) == '0') {
			builder.deleteCharAt(0);
		}
		return new Integer(builder.toString()).intValue();
	}

	// some text line for regular search
	private static String textLine(int docNum) {
		return DOC_TEXT_LINES[docNum % DOC_TEXT_LINES.length];
	}

	// debug messages (change DBG to true for anything to print)
	protected static void log(Object o) {
		if (VERBOSE) {
			System.out.println(o.toString());
		}
	}

	@Test
	public void testMinFrequencyTermQuery() throws IOException {
		Query functionQuery = new MinFrequencyPrefixQuery(new Term(TEXT_FIELD, "th"), 3);
		IndexReader r = DirectoryReader.open(dir);
		IndexSearcher s = newSearcher(r);
		log("test: " + functionQuery);
		QueryUtils.check(random(), functionQuery, s);
		ScoreDoc[] h = s.search(functionQuery, 1000).scoreDocs;
		//assertEquals("All docs should be matched!", N_DOCS, h.length);
		String prevID = "ID" + (N_DOCS + 1); // greater than all ids of docs in this test
		for (int i = 0; i < h.length; i++) {
			String resID = s.doc(h[i].doc).get(ID_FIELD);
			log(textLine(string2id(resID) - 1));
			log(i + ".   score=" + h[i].score + "  -  " + resID);
			log(s.explain(functionQuery, h[i].doc));
			//assertTrue("res id " + resID + " should be < prev res id " + prevID, resID.compareTo(prevID) < 0);
			prevID = resID;
		}
		r.close();
	}

}
