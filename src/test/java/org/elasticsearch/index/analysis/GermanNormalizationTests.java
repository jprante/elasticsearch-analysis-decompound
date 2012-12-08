package org.elasticsearch.index.analysis;

import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.elasticsearch.common.inject.Injector;
import org.elasticsearch.common.inject.ModulesBuilder;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.settings.SettingsModule;
import org.elasticsearch.env.Environment;
import org.elasticsearch.env.EnvironmentModule;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.IndexNameModule;
import org.elasticsearch.index.settings.IndexSettingsModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisModule;
import org.elasticsearch.indices.analysis.IndicesAnalysisService;
import org.elasticsearch.plugin.analysis.decompound.AnalysisDecompoundPlugin;
import org.testng.Assert;
import org.testng.annotations.Test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class GermanNormalizationTests extends Assert {

    @Test
    public void test() throws IOException {
        AnalysisService analysisService = createAnalysisService();

        TokenFilterFactory tokenFilter = analysisService.tokenFilter("umlaut");
        assertThat(tokenFilter, instanceOf(GermanNormalizationFilterFactory.class));

        String source = "Ein schöner Tag in Köln im Café an der Straßenecke";

        String[] expected = {
            "Ein",
            "schoner",
            "Tag",
            "in",
            "Koln",
            "im",
            "Café",
            "an",
            "der",
            "Strassenecke"
        };

        Tokenizer tokenizer = new StandardTokenizer(Version.LUCENE_36, new StringReader(source));

        assertSimpleTSOutput(tokenFilter.create(tokenizer), expected);

    }

    public AnalysisService createAnalysisService() {
        Settings settings = ImmutableSettings.settingsBuilder().loadFromClasspath("org/elasticsearch/index/analysis/german_normalization_analysis.json").build();

        Index index = new Index("test");

        Injector parentInjector = new ModulesBuilder().add(new SettingsModule(settings),
                new EnvironmentModule(new Environment(settings)),
                new IndicesAnalysisModule())
                .createInjector();

        AnalysisModule analysisModule = new AnalysisModule(settings, parentInjector.getInstance(IndicesAnalysisService.class));
        new AnalysisDecompoundPlugin().onModule(analysisModule);

        Injector injector = new ModulesBuilder().add(
                new IndexSettingsModule(index, settings),
                new IndexNameModule(index),
                analysisModule)
                .createChildInjector(parentInjector);

        return injector.getInstance(AnalysisService.class);
    }

    public static void assertSimpleTSOutput(TokenStream stream,
            String[] expected) throws IOException {
        stream.reset();
        CharTermAttribute termAttr = stream.getAttribute(CharTermAttribute.class);
        assertNotNull(termAttr);
        int i = 0;
        while (stream.incrementToken()) {
            assertTrue(i < expected.length);
            assertEquals(expected[i++], termAttr.toString(), "expected different term at index " + i);
        }
        assertEquals(i, expected.length, "not all tokens produced");
    }
}