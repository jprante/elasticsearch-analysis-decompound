package org.xbib.decompound;

import org.xbib.elasticsearch.index.analysis.decompound.CompactPatriciaTrie;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

public class LemmatizeBuildTests {

    public void trainReduce() throws IOException, ClassNotFoundException {
        Trainer trainer = new Trainer();
        CompactPatriciaTrie reduce = new CompactPatriciaTrie();
        reduce.setIgnoreCase(true);
        reduce.setReverse(true);
        InputStream in = getClass().getResourceAsStream("/morphy-mapping-20110717.latin1.gz");
        GZIPInputStream gzip = new GZIPInputStream(in);
        Reader reader = new InputStreamReader(gzip, "ISO-8859-1");
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            //System.err.println(line);
            String[] forms = line.split("\t");
            if (forms.length == 2) {
                String wclass = trainer.createRule(forms[0], forms[1]);
                //System.err.println("w=" + forms[0]  + " wclass="+ wclass);
                reduce.train(forms[0], wclass);
            }
        }
        br.close();
        FileOutputStream f = new FileOutputStream("target/morphyLemmaForms.tree");
        reduce.save(f);
        f.close();
    }
}
