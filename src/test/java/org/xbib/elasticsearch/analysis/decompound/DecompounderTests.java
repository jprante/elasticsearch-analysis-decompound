package org.xbib.elasticsearch.analysis.decompound;

import java.io.IOException;
import java.io.InputStream;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.junit.Assert;
import org.junit.Test;
import org.xbib.elasticsearch.index.analysis.decompound.Decompounder;

public class DecompounderTests extends Assert {

    private final ESLogger logger = Loggers.getLogger(DecompounderTests.class.getName());

    @Test
    public void testGerman() throws IOException, ClassNotFoundException {
    	
    	InputStream forwfile = getClass().getResourceAsStream("/kompVVic.tree");
        InputStream backfile = getClass().getResourceAsStream("/kompVHic.tree");
        InputStream reducfile = getClass().getResourceAsStream("/grfExt.tree");
        Decompounder d = new Decompounder(forwfile, backfile, reducfile, 0.51);
        
        
        long begin = System.currentTimeMillis();
        
        String[] word = {
            "Jahresfeier",
            "Kinderernährung",
            "Donaudampfschiff",
            "Ökosteuer",
            "Rechtsanwaltskanzleien",
            "",
            "tomaten",
            "Taschenhersteller",
            "Taschenhersteller",
            "Taschenhersteller",
            "Taschenhersteller",
            "Kindergarten",
            "Laufschritt",
            "Geschirrtuch",
            "Speisekarte",
            "Brieftaube",
            "Briefträger",
            "Blitzschlag",
            "Sonnenstrahl",
            "Ersatzteil"
            
        };
        String[] decomp = {
            "[Jahr, feier]",
            "[Kind, ernährung]",
            "[Donau, dampf, schiff]",
            "[Ökosteuer]",
            "[Recht, anwalt, kanzlei]",
            "[]",
            "[tomaten]",
            "[Tasche, hersteller]",
            "[Tasche, hersteller]",
            "[Tasche, hersteller]",
            "[Tasche, hersteller]",
            "[Kind, garten]",
            "[Lauf, schritt]",
            "[Geschirr, tuch]",
            "[Speise, karte]",
            "[Brief, taube]",
            "[Brief, träger]",
            "[Blitz, schlag]",
            "[Sonne, strahl]",
            "[Ersatz, teil]"
            
        };
        
        
        
    
        for (int i = 0; i < word.length; i++) {
            logger.info(word[i] +" => " + d.decompound(word[i]));
            assertEquals(d.decompound(word[i]).toString(), decomp[i]);
        }
        
        long end = System.currentTimeMillis();
        System.out.println("total time : " + (end-begin));
    }

}
