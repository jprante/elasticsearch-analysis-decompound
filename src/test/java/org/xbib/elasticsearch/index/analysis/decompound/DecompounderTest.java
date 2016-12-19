package org.xbib.elasticsearch.index.analysis.decompound;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

public class DecompounderTest extends Assert {

    @Test
    public void testGerman() throws IOException, ClassNotFoundException {
        InputStream forwfile = getClass().getResourceAsStream("/decompound/kompVVic.tree");
        InputStream backfile = getClass().getResourceAsStream("/decompound/kompVHic.tree");
        InputStream reducfile = getClass().getResourceAsStream("/decompound/grfExt.tree");
        Decompounder d = new Decompounder(forwfile, backfile, reducfile, 0.51);
        
        String[] word = {
            "Jahresfeier",
            "Kinderernährung",
            "Donaudampfschiff",
            "Ökosteuer",
            "Rechtsanwaltskanzleien",
            "",
            "tomaten"
        };
        String[] decomp = {
            "[Jahr, feier]",
            "[Kind, ernährung]",
            "[Donau, dampf, schiff]",
            "[Ökosteuer]",
            "[Recht, anwalt, kanzlei]",
            "[]",
            "[tomaten]"
        };
    
        for (int i = 0; i < word.length; i++) {
            //logger.info(word[i] +" => " + d.decompound(word[i]));
            assertEquals(decomp[i], d.decompound(word[i]).toString());
        }
    }

}
