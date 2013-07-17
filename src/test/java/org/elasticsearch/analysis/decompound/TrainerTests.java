package org.elasticsearch.analysis.decompound;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import org.testng.annotations.Test;

public class TrainerTests {

    public void train() throws IOException, ClassNotFoundException {
        Trainer trainer = new Trainer();
        StringReader nouns = new StringReader(
                "Rente Rente\n"
                + "Renten Rente\n"
                + "Versicherung Versicherung\n"
                + "Versicherungen Versicherung\n"
                + "Reich Reich\n"
                + "Reichs Reich\n"
                + "Kanzler Kanzler\n"
                + "Thron Thron\n"
                + "Folge Folge\n"
                + "Industrie Industrie\n"
                + "Wert Wert\n"
                + "Werte Wert\n"
                + "Bund Bund\n"
                + "Bundes Bund\n"
                + "Ministerium Ministerium\n"
                + "Ministeriums Ministerium\n"
                + "Bier Bier\n"
                + "Zelt Zelt\n"
                + "Wald Wald\n"
                + "Sterben Sterben\n"
                + "Kunde Kunde\n"
                + "Kunden Kunden\n"
                + "Kinder Kind\n"
                + "Garten Garten"
                + "Dampf Dampf\n"
                + "Schiff Schiff\n"
                );
        StringReader compounds = new StringReader(
                "Rentenversicherungen Renten + versicherungen\n"
                + "Reichskanzler Reichs + kanzler\n"
                + "Thronfolge Thron + folge\n"
                + "Industriewerte Industrie + werte\n"
                + "Bundesministeriums Bundes + ministeriums\n"
                + "Bierzelt Bier + zelt\n"
                + "Waldsterben Wald + sterben\n"
                + "Industriekunden Industrie + kunden\n"
                + "Publikumsmagnet Publikums + magnet \n"
                + "Bundesbildungsminister Bundes + bildungs + minister\n"
                + "Binnenland Binnen + land\n"
                + "Postleitzahlen Post + leit + zahlen\n"
                + "Mutprobe Mut + probe\n"
                + "Firefox Fire + fox\n"
                + "Neonlicht Neon + licht\n"
                + "Messerstiche Messer + stiche\n"
                + "Materialkosten Material + kosten\n"
                + "Hochschulreife Hochschul + reife\n"
                + "Blutspuren Blut + spuren\n"
                + "Kulturausschuss Kultur + ausschuss\n"
                + "Doppelmoral Doppel + moral\n"
                + "Tourismusindustrie Tourismus + industrie\n"
                + "Weihwasser Weih + wasser\n"
                + "Vorschulalter Vorschul + alter\n"
                + "Kulturausschuss Kultur + ausschuss\n"
                + "Ausbildungszeiten Ausbildungs + zeiten\n"
                + "Stromkunden Strom + kunden\n"
                + "Kindergarten Kinder + garten\n"
                + "Dampfschiff Dampf + schiff\n"
                );
        
        trainer.trainReduce(nouns);
        //trainer.loadReduce(getClass().getResourceAsStream("/grfExt.tree"));
        trainer.trainCompounds(compounds, null, null);
        //InputStream forwfile = getClass().getResourceAsStream("/kompVVic.tree");
        //InputStream backfile = getClass().getResourceAsStream("/kompVHic.tree");
        //InputStream reducfile = getClass().getResourceAsStream("/grfExt.tree");
        Decompounder d = new Decompounder(trainer.getForward(), trainer.getBackward(), trainer.getReduce());
        String s[] = new String[]{
            "Rentenversicherungen",
            "Dampfschiff",
            "Kinder",
            "Kindergarten",
            "Waldsterben"
        };
        for (String w : s) {
            System.err.println("word = " + w + " decompound = " + d.decompound(w));
        }
    }
}
