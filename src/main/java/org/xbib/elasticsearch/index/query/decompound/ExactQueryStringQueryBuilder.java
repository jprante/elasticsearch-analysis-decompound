package org.xbib.elasticsearch.index.query.decompound;

import java.io.IOException;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

public class ExactQueryStringQueryBuilder extends QueryStringQueryBuilder {
	
    public static final String NAME = "exact_query_string";

	public ExactQueryStringQueryBuilder(String queryString) throws IOException {
		super(queryString);
	}

	public ExactQueryStringQueryBuilder(StreamInput in) throws IOException {
		super(in);
	}

	@Override
    public String getWriteableName() {
        return ExactQueryStringQueryBuilder.NAME;
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
    	super.doXContent(builder, params);
    }
}
