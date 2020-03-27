package org.xbib.elasticsearch.index.analysis.decompound;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.admin.cluster.node.info.NodeInfo;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.PluginInfo;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.StreamsUtils;
import org.elasticsearch.test.hamcrest.ElasticsearchAssertions;
//import org.elasticsearch.test.junit.annotations.TestLogging;
import org.elasticsearch.transport.Netty4Plugin;
import org.junit.Before;
import de.pansoft.elasticsearch.index.query.exact.ExactPhraseQueryBuilder;
import de.pansoft.elasticsearch.index.query.string.GeniosQueryStringQueryBuilder;
import org.xbib.elasticsearch.plugin.analysis.decompound.AnalysisDecompoundPlugin;

//@TestLogging("level:DEBUG")
public class DecompoundQueryIntegTest extends ESIntegTestCase {

    @Override
    protected Collection<Class<? extends Plugin>> nodePlugins() {
        return Arrays.asList(Netty4Plugin.class, AnalysisDecompoundPlugin.class);
    }
    
    public void testPluginIsLoaded() {
        NodesInfoResponse response = client().admin().cluster().prepareNodesInfo().setPlugins(true).get();
        for (NodeInfo nodeInfo : response.getNodes()) {
            boolean pluginFound = false;
            for (PluginInfo pluginInfo : nodeInfo.getPlugins().getPluginInfos()) {
                if (pluginInfo.getName().equals(AnalysisDecompoundPlugin.class.getName())) {
                    pluginFound = true;
                    break;
                }
            }
            assertThat(pluginFound, is(true));
        }
    }

    @Before
    public  void setup() throws Exception {
        String indexBody = StreamsUtils.copyToStringFromClasspath("/decompound_query.json");
        prepareCreate("test").setSource(indexBody, XContentType.JSON).get();
        ensureGreen("test");
    }

    public void testBoostedExactTokenQuery() throws Exception {
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "Spielbankgesellschaft Spielbankgesellschaft"));
        reqs.add(client().prepareIndex("test", "_doc", "2").setSource("text", "deutsche Bank und sonstiges das nicht von Belang ist"));
        indexRandom(true, false, reqs);

        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("text:bank");
        ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(queryStringQueryBuilder, false, 2.0f);
        SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
        ElasticsearchAssertions.assertHitCount(resp, 2L);
        assertHits(resp.getHits(), "2", "1");

    }
   
    public void testNestedCommonPhraseQuery() throws Exception {
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "deutsche Spielbankgesellschaft"));
        indexRandom(true, false, reqs);
       
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("text:\"deutsche spielbankgesellschaft\"");
        ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(queryStringQueryBuilder, false);
        SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
        ElasticsearchAssertions.assertHitCount(resp, 1L);
        assertHits(resp.getHits(), "1");

        QueryStringQueryBuilder queryStringQueryBuilder2 = QueryBuilders.queryStringQuery("text:\"deutsche bank\"");
        ExactPhraseQueryBuilder exactPhraseQueryBuilder2 = new ExactPhraseQueryBuilder(queryStringQueryBuilder2, false);
        SearchResponse resp2 = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder2).get();
        ElasticsearchAssertions.assertHitCount(resp2, 0L);

        QueryStringQueryBuilder queryStringQueryBuilder3 = QueryBuilders.queryStringQuery("text:\"deutsche spielbankgesellschaft\" AND NOT text:\"deutsche bank\"");
        ExactPhraseQueryBuilder exactPhraseQueryBuilder3 = new ExactPhraseQueryBuilder(queryStringQueryBuilder3, false);
        SearchResponse resp3 = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder3).get();
        ElasticsearchAssertions.assertHitCount(resp3, 1L);
        assertHits(resp3.getHits(), "1");
    }
    
    
    public void testAllQueryTypesExactQuery() throws Exception {
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "deutsche Spielbankgesellschaft"));
        indexRandom(true, false, reqs);
      
        {
			QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("text:bank");
			ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(queryStringQueryBuilder, true);
			SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        {
			QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("text:spielbankgesell*");
			ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(queryStringQueryBuilder, true);
			SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 1L);
        }
        {
			QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("text:ban*");
			ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(queryStringQueryBuilder, true);
			SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        {
			QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("text:spielbunkgesellschuft~2");
			ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(queryStringQueryBuilder, true);
			SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 1L);
        }
        {
			QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("text:bunk~2");
			ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(queryStringQueryBuilder, true);
			SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
    }
    
    public void testMinFrequencyExactQuery() throws Exception {
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "deutsche Spielbankgesellschaft als Bank"));
        indexRandom(true, false, reqs);
       
        {
        	SearchSourceBuilder sourceBuilder = getFromSource("/minFrequencyTerm.json", "bank", 2, false);
			SearchResponse resp = client().prepareSearch("test").setSource(sourceBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 1L);
        }
        {
        	SearchSourceBuilder sourceBuilder = getFromSource("/minFrequencyTerm.json", "bank", 3, false);
			SearchResponse resp = client().prepareSearch("test").setSource(sourceBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        {
        	SearchSourceBuilder sourceBuilder = getFromSource("/minFrequencyTerm.json", "bank", 2, true);
			SearchResponse resp = client().prepareSearch("test").setSource(sourceBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        {
        	SearchSourceBuilder sourceBuilder = getFromSource("/minFrequencyTerm.json", "bank", 1, true);
			SearchResponse resp = client().prepareSearch("test").setSource(sourceBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 1L);
        }
        {
        	SearchSourceBuilder sourceBuilder = getFromSource("/minFrequencyPrefix.json", "spie", 2, false);
			SearchResponse resp = client().prepareSearch("test").setSource(sourceBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 1L);
        }
        {
        	SearchSourceBuilder sourceBuilder = getFromSource("/minFrequencyPrefix.json", "spie", 3, false);
			SearchResponse resp = client().prepareSearch("test").setSource(sourceBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        {
        	SearchSourceBuilder sourceBuilder = getFromSource("/minFrequencyPrefix.json", "spie", 2, true);
			SearchResponse resp = client().prepareSearch("test").setSource(sourceBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        {
        	SearchSourceBuilder sourceBuilder = getFromSource("/minFrequencyPrefix.json", "spie", 1, true);
			SearchResponse resp = client().prepareSearch("test").setSource(sourceBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 1L);
        }

    }

    private SearchSourceBuilder getFromSource(String resource, Object... arguments) throws IOException {
        String rawResourceString = StreamsUtils.copyToStringFromClasspath(resource);
        String resourceString = rawResourceString;
        if (arguments != null && arguments.length > 0) {
        	rawResourceString = rawResourceString.replaceAll("\\{(?![0-9])", "'{'").replaceAll("(?<![0-9])\\}", "'}'");
        	resourceString = MessageFormat.format(rawResourceString, arguments);
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        XContentParser parser = XContentFactory.xContent(XContentType.JSON).
        		createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, resourceString);
        searchSourceBuilder.parseXContent(parser);
		return searchSourceBuilder;
	}

	public void testCommonPhraseQuery() throws Exception {
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "deutsche Spielbankgesellschaft"));
        indexRandom(true, false, reqs);
       
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery("text:\"deutsche bank\"");
        SearchResponse resp = client().prepareSearch("test").setQuery(queryStringQueryBuilder).get();
        ElasticsearchAssertions.assertHitCount(resp, 1L);
        assertHits(resp.getHits(), "1");
    }

    public void testOneTermQuery() throws Exception {
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "deutsche Spielbankgesellschaft"));
        indexRandom(true, false, reqs);

        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("text:\"bank\"");
            SearchResponse resp = client().prepareSearch("test").setQuery(geniosQueryStringQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 1L);
        }

        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("text:\"bank\"");
            ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(geniosQueryStringQueryBuilder, false);
            SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 0L);
        }

        GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder2 = new GeniosQueryStringQueryBuilder("text:bank");
        SearchResponse resp2 = client().prepareSearch("test").setQuery(geniosQueryStringQueryBuilder2).get();
        ElasticsearchAssertions.assertHitCount(resp2, 1L);
        assertHits(resp2.getHits(), "1");

        GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder3 = new GeniosQueryStringQueryBuilder("text:\"spielbankgesellschaft\"");
        SearchResponse resp3 = client().prepareSearch("test").setQuery(geniosQueryStringQueryBuilder3).get();
        ElasticsearchAssertions.assertHitCount(resp3, 1L);
        assertHits(resp3.getHits(), "1");
    }

    public void testFrequencyQuery() throws Exception {
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "deutsche Spielbankgesellschaft in der Bank"));
        indexRandom(true, false, reqs);

        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("text:bank#2");
            SearchResponse resp = client().prepareSearch("test").setQuery(geniosQueryStringQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        /*
        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("text:bank#2");
            SearchResponse resp = client().prepareSearch("test").setQuery(geniosQueryStringQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 1L);
        }
        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("text:ban*#2");
            SearchResponse resp = client().prepareSearch("test").setQuery(geniosQueryStringQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 1L);
        }
        */
        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("text:bank#2");
            ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(geniosQueryStringQueryBuilder, true);
            SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        /*
        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("text:ban*#2");
            ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(geniosQueryStringQueryBuilder, true);
            SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        */
    }

    public void testFrequencyQueryWithMultipleFields() throws Exception {

        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "deutsche Spielbankgesellschaft in der Bank", "keyword", "bank"));
        indexRandom(true, false, reqs);
        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("bank#2");
            SearchResponse resp = client().prepareSearch("test").setQuery(geniosQueryStringQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        /*
        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("bank#2");
            SearchResponse resp = client().prepareSearch("test").setQuery(geniosQueryStringQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 1L);
        }
        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("ban*#2");
            SearchResponse resp = client().prepareSearch("test").setQuery(geniosQueryStringQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 1L);
        }
        */
        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("bank#2");
            ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(geniosQueryStringQueryBuilder, true);
            SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        /*
        {
            GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("ban*#2");
            ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(geniosQueryStringQueryBuilder, true);
            SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
            ElasticsearchAssertions.assertHitCount(resp, 0L);
        }
        */
    }

    public void testKeywordOneTermQuery() throws Exception {
    	
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("keyword", "spielbankgesellschaft"));
        indexRandom(true, false, reqs);

        GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("keyword:\"spielbankgesellschaft\"");
        SearchResponse resp = client().prepareSearch("test").setQuery(geniosQueryStringQueryBuilder).get();
        ElasticsearchAssertions.assertHitCount(resp, 1L);
        assertHits(resp.getHits(), "1");
    }
    
    public void testBoostedTermQuery() throws Exception {
    	
        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "spielbankgesellschaft", "text2", "spielbankgesellschaft"));
        indexRandom(true, false, reqs);

        GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("\"bank\"");
        Map<String, Float> fields = new HashMap<>();
        fields.put("text", 2.0f);
        fields.put("text2", 1.0f);
        geniosQueryStringQueryBuilder.fields(fields);
        ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(geniosQueryStringQueryBuilder, false);
        SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
        ElasticsearchAssertions.assertHitCount(resp, 0L);
    }
    
    public void testGermanQuerySyntax() throws Exception {

        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("text", "spiel bank"));
        reqs.add(client().prepareIndex("test", "_doc", "2").setSource("text", "spiel gesellschaft"));
        reqs.add(client().prepareIndex("test", "_doc", "3").setSource("text", "bank gesellschaft"));
        indexRandom(true, false, reqs);

        {
			GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("bank UND spiel");
			ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(geniosQueryStringQueryBuilder, false);
			SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 1L);
        }

        {
			GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("bank ODER spiel");
			ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(geniosQueryStringQueryBuilder, false);
			SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 3L);
        }
    	
        {
			GeniosQueryStringQueryBuilder geniosQueryStringQueryBuilder = new GeniosQueryStringQueryBuilder("bank NICHT spiel");
			ExactPhraseQueryBuilder exactPhraseQueryBuilder = new ExactPhraseQueryBuilder(geniosQueryStringQueryBuilder, false);
			SearchResponse resp = client().prepareSearch("test").setQuery(exactPhraseQueryBuilder).get();
			ElasticsearchAssertions.assertHitCount(resp, 1L);
        }
    }

    private void assertHits(SearchHits hits, String... ids) {
        assertThat(hits.getTotalHits(), equalTo((long) ids.length));
        Set<String> hitIds = new HashSet<>();
        for (SearchHit hit : hits.getHits()) {
            hitIds.add(hit.getId());
        }
        assertThat(hitIds, containsInAnyOrder(ids));
    }

    public void testSerialization() throws Exception {

        List<IndexRequestBuilder> reqs = new ArrayList<>();
        reqs.add(client().prepareIndex("test", "_doc", "1").setSource("keyword", "spielbankgesellschaft"));
        indexRandom(true, false, reqs);
        
        String queryJson = StreamsUtils.copyToStringFromClasspath("/complex_query.json");
        final XContent xContent = XContentFactory.xContent(XContentType.JSON);
        XContentParser xContentParser = xContent.createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, queryJson);
        QueryBuilder queryBuilder = AbstractQueryBuilder.parseInnerQueryBuilder(xContentParser);
       
        final XContent xContentNew = XContentFactory.xContent(XContentType.JSON);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XContentBuilder xContentBuilder = new XContentBuilder(xContentNew, baos);
        queryBuilder.toXContent(xContentBuilder, ToXContent.EMPTY_PARAMS);
        xContentBuilder.close();
        baos.close();
        
        byte[] byteArray = baos.toByteArray();
        final XContent xContentCompare = XContentFactory.xContent(XContentType.JSON);
        XContentParser xContentParserCompare = xContentCompare.createParser(xContentRegistry(), LoggingDeprecationHandler.INSTANCE, byteArray);
        QueryBuilder queryBuilderCompare = AbstractQueryBuilder.parseInnerQueryBuilder(xContentParserCompare);
        assertEquals(queryBuilder, queryBuilderCompare);
        
    	
    }
}
