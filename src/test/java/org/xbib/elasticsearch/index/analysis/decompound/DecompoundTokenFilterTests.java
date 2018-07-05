package org.xbib.elasticsearch.index.analysis.decompound;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.NamedAnalyzer;
import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.test.ESTestCase;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.plugin.analysis.decompound.AnalysisDecompoundPlugin;

import java.io.IOException;
import java.io.StringReader;

/**
 *
 */
public class DecompoundTokenFilterTests extends ESTestCase {

    @Test
    public void test() throws IOException {

        String source = "Die Jahresfeier der Rechtsanwaltskanzleien auf dem Donaudampfschiff hat viel Ökosteuer gekostet";

        String[] expected = {
                "Die",
                "Die",
                "Jahresfeier",
                "Jahr",
                "feier",
                "der",
                "der",
                "Rechtsanwaltskanzleien",
                "Recht",
                "anwalt",
                "kanzlei",
                "auf",
                "auf",
                "dem",
                "dem",
                "Donaudampfschiff",
                "Donau",
                "dampf",
                "schiff",
                "hat",
                "hat",
                "viel",
                "viel",
                "Ökosteuer",
                "Ökosteuer",
                "gekostet",
                "gekosten"
        };
        String resource = "decompound_analysis.json";
        Settings nodeSettings = getNodeSettings();
        TestAnalysis analysis = createTestAnalysis(new IndexSettings(getIndexMetaData(resource), nodeSettings), nodeSettings, new AnalysisDecompoundPlugin());
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("decomp");
        Tokenizer tokenizer = analysis.tokenizer.get("standard").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    @Test
    public void testWithSubwordsOnly() throws IOException {
        String source = "Das ist ein Schlüsselwort, ein Bindestrichwort";
        String[] expected = {
                "Da",
                "ist",
                "ein",
                "Schlüssel",
                "wort",
                "ein",
                "Bindestrich",
                "wort"
        };
        String resource = "keywords_analysis.json";
        Settings nodeSettings = getNodeSettings();
        TestAnalysis analysis = createTestAnalysis(new IndexSettings(getIndexMetaData(resource), nodeSettings), nodeSettings, new AnalysisDecompoundPlugin());
        NamedAnalyzer analyzer = analysis.indexAnalyzers.get("with_subwords_only");
        assertNotNull(analyzer);
        assertSimpleTSOutput(analyzer.tokenStream("test-field", source), expected);
    }

    private void assertSimpleTSOutput(TokenStream stream, String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        Assert.assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            assertTrue(i < expected.length);
            assertEquals(expected[i], termAttr.toString());
            i++;
        }
        assertEquals(i, expected.length);
        stream.close();
    }

    private IndexMetaData getIndexMetaData(String resource) throws IOException {
        return IndexMetaData.builder("test")
            .settings(Settings.builder()
                          .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                          .loadFromStream(resource, ClassLoader.getSystemClassLoader().getResourceAsStream(resource), false)
                          .build())
            .numberOfShards(1)
            .numberOfReplicas(1)
            .build();
    }

    private Settings getNodeSettings() {
        return Settings.builder()
            .put("path.home", System.getProperty("path.home", "/tmp"))
            .build();
    }

}
