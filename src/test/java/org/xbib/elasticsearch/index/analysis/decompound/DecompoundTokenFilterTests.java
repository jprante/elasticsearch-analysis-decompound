package org.xbib.elasticsearch.index.analysis.decompound;

import java.io.IOException;
import java.io.StringReader;

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
        Settings settings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .loadFromStream(resource, ClassLoader.getSystemClassLoader().getResourceAsStream(resource), false)
                .build();
        IndexMetaData indexMetaData = IndexMetaData.builder("test")
                .settings(settings)
                .numberOfShards(1)
                .numberOfReplicas(1)
                .build();
        Settings nodeSettings = Settings.builder()
        			.put(AnalysisDecompoundPlugin.SETTING_MAX_CACHE_SIZE.getKey(), 131072)
                .put("path.home", System.getProperty("path.home", "/tmp"))
                .build();
        TestAnalysis analysis = createTestAnalysis(new IndexSettings(indexMetaData, nodeSettings), nodeSettings, new AnalysisDecompoundPlugin(nodeSettings));
        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("decomp");
        Tokenizer tokenizer = analysis.tokenizer.get("standard").create();
        tokenizer.setReader(new StringReader(source));
        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);
    }

    @Test
    public void testFalsePositive() throws IOException {

        String source = "Deutsche Spielbankgesellschaft";

        String[] expected = {
                "Deutsche",
                "Deutsche",
                "Spielbankgesellschaft",
                "Spiel",
                "bank",
                "gesellschaft"
        };
        String resource = "decompound_analysis.json";
        Settings settings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .loadFromStream(resource, ClassLoader.getSystemClassLoader().getResourceAsStream(resource), false)
                .build();
        IndexMetaData indexMetaData = IndexMetaData.builder("test")
                .settings(settings)
                .numberOfShards(1)
                .numberOfReplicas(1)
                .build();
        Settings nodeSettings = Settings.builder()
        			.put(AnalysisDecompoundPlugin.SETTING_MAX_CACHE_SIZE.getKey(), 131072)
                .put("path.home", System.getProperty("path.home", "/tmp"))
                .build();
        TestAnalysis analysis = createTestAnalysis(new IndexSettings(indexMetaData, nodeSettings), nodeSettings, new AnalysisDecompoundPlugin(nodeSettings));
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
        Settings settings = Settings.builder()
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put("path.home", System.getProperty("path.home", "/tmp"))
                .loadFromStream(resource, ClassLoader.getSystemClassLoader().getResourceAsStream(resource), false)
                .build();
        IndexMetaData indexMetaData = IndexMetaData.builder("test")
                .settings(settings)
                .numberOfShards(1)
                .numberOfReplicas(1)
                .build();
        Settings nodeSettings = Settings.builder()
        			.put(AnalysisDecompoundPlugin.SETTING_MAX_CACHE_SIZE.getKey(), 131072)
                .put("path.home", System.getProperty("path.home", "/tmp"))
                .build();
        TestAnalysis analysis = createTestAnalysis(new IndexSettings(indexMetaData, nodeSettings), nodeSettings, new AnalysisDecompoundPlugin(nodeSettings));
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
}
