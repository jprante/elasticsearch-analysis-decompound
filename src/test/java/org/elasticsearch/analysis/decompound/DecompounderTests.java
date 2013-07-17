package org.elasticsearch.analysis.decompound;

import java.io.IOException;
import java.io.InputStream;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.testng.annotations.Test;
import org.testng.Assert;

public class DecompounderTests extends Assert {

    private final ESLogger logger = Loggers.getLogger(DecompounderTests.class.getName());

    @Test
    public void testGerman() throws IOException, ClassNotFoundException {
        InputStream forwfile = getClass().getResourceAsStream("/kompVVic.tree");
        InputStream backfile = getClass().getResourceAsStream("/kompVHic.tree");
        InputStream reducfile = getClass().getResourceAsStream("/grfExt.tree");
        Decompounder d = new Decompounder(forwfile, backfile, reducfile);
        
        String[] word = {
            "Jahresfeier",
            "Kinderernährung",
            "Donaudampfschiff",
            "Ökosteuer",
            "Rechtsanwaltskanzleien"
        };
        String[] decomp = {
            "[Jahr, feier]",
            "[Kind, ernährung]",
            "[Donau, dampf, schiff]",
            "[Ökosteuer]",
            "[Recht, anwalt, kanzlei]"
        };
    
        for (int i = 0; i < word.length; i++) {
            logger.info(word[i] +" => " + d.decompound(word[i]));
            assertEquals(d.decompound(word[i]).toString(), decomp[i].toString());
        }

    }

}
