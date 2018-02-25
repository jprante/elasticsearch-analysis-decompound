package org.xbib.elasticsearch.common.decompound.patricia;

import com.carrotsearch.randomizedtesting.annotations.SuppressForbidden;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

@SuppressForbidden(value = "Trainer for Decompounder")
public class Trainer {

    private CompactPatriciaTrie reduce;

    private CompactPatriciaTrie forward;

    private CompactPatriciaTrie backward;

    /*public static void main(String[] args) {

        try {
            OptionParser parser = new OptionParser() {
                {
                    accepts("file").withRequiredArg().ofType(String.class).required();
                    accepts("reduce").withRequiredArg().ofType(String.class);
                    accepts("forward").withRequiredArg().ofType(String.class).required();
                    accepts("backward").withRequiredArg().ofType(String.class).required();
                    accepts("help");
                }
            };
            final OptionSet options = parser.parse(args);
            if (options.hasArgument("help")) {
                System.err.println("Trainer");
                System.err.println("--file <uri>");
                System.err.println("--reduce <name>");
                System.err.println("--forward <name>");
                System.err.println("--backward <uri>");
                System.exit(1);
            }
            final URI uri = URI.create(options.valueOf("file").toString());
            String path = uri.getSchemeSpecificPart();
            InputStream in = new FileInputStream(path);
            Reader reader = new InputStreamReader(in, "UTF-8");
            Trainer trainer = new Trainer();
            trainer.train(reader, options.valueOf("forward").toString(), options.valueOf("backward").toString(),
            options.valueOf("reduce").toString());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }*/

    public Trainer() {
    }
    
    public Trainer loadReduce(InputStream in, double threshold) throws IOException {
        reduce = new CompactPatriciaTrie();
        reduce.setIgnoreCase(true);
        reduce.setReverse(true);
        reduce.setThreshold(threshold);
        reduce.load(in);
        return this;
    }

    public CompactPatriciaTrie getForward() {
        return forward;
    }

    public CompactPatriciaTrie getBackward() {
        return backward;
    }

    public CompactPatriciaTrie getReduce() {
        return reduce;
    }

    public void trainReduce(Reader reader, double threshold) throws IOException {
        reduce = new CompactPatriciaTrie();
        reduce.setIgnoreCase(true);
        //reduce.setReverse(true);
        reduce.setThreshold(threshold);
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] forms = line.split(" ");
            if (forms.length == 2) {
                // <fullform> <baseform>
                String wclass = createRule(forms[0], forms[1]);
                //String cl = reduce.classify(line);
                reduce.train(forms[0], wclass);
            }
        }
    }

    /**
     * Train compound words.
     *
     * Format per line:
     *
     * # <comp> " " <decomp1> " + " <decomp2> ...
     *
     * @param reader
     * @throws IOException
     */
    public void trainCompounds(Reader reader, OutputStream forw, OutputStream backw, double threshold) throws IOException {
        forward = new CompactPatriciaTrie();
        forward.setIgnoreCase(true);
        forward.setThreshold(threshold);
        backward = new CompactPatriciaTrie();
        backward.setIgnoreCase(true);
        backward.setThreshold(threshold);
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            String comp = line;
            int pos = line.indexOf(" ");
            if (pos >= 0) {
                comp = line.substring(0, pos);
                line = line.substring(pos + 1);
            }
            String[] decompArr = line.split(" \\+ ");
            //StringBuilder decomp = new StringBuilder();
            //for (String s : decompArr) {
            //    decomp.append(s).append(" ");
            //}
            //String cl = reduce.classify(comp);
            //System.err.println("comp=" + comp + " cl=" + cl);
            //reduce.train(comp, cl);
            //String baseform = applyRule(comp, cl);
            //System.err.println("baseform = '" + baseform + "' decomp=" + Arrays.asList(decompArr));
            //System.err.println("comp = '" + comp + "' decomp = '" + decomp + "'");
            //if (decompArr.length > 1) {
            trainTrees(decompArr, comp);
            //}
        }
        br.close();
        if (forw != null) {
            forward.save(forw);
            forw.close();
        }
        if (backw != null) {
            backward.save(backw);
            backw.close();
        }
    }

    private void trainTrees(String[] decomp, String compound) {
        if (compound == null) {
            return;
        }
        for (int i = 0; i < decomp.length; i++) {
            String s = decomp[i];
            String cl = reduce.classify(s);
            reduce.train(s, cl);
            String baseform = applyRule(s, cl);
            decomp[i] = baseform;
        }
        if (decomp.length > 1) {
            String compoundpart = compound;
            String currentcompound;
            for (int i = 0; i < decomp.length - 1; i++) {
                if (i > 0) {
                    compoundpart = compoundpart.substring(decomp[i - 1].length());
                }
                StringBuilder decomppart = new StringBuilder(decomp[i]);
                for (int j = i + 1; j < decomp.length; j++) {
                    decomppart.append(" ").append(decomp[j]);
                    currentcompound = compound.substring(compound.indexOf(decomp[i]), compound.indexOf(decomp[j]) + decomp[j].length());
                    System.err.println("decomppart=" + decomppart + " compoundpart=" + compoundpart);
                    String forwardRule = createForwardRule(decomppart.toString(), compoundpart);
                    String backwardRule = createBackwardRule(decomppart.toString());
                    System.err.println("currentcompound=" + currentcompound
                            + " for=" + forwardRule + " back=" + backwardRule);
                    forward.train(currentcompound, forwardRule);
                    backward.train(currentcompound, backwardRule);
                }
            }
        }
    }

    private String createForwardRule(String decomp, String compound) {
        String[] s = decomp.split("[ \\-/,;]");
        if (s.length < 2) {
            return "";
        }
        int occur1 = compound.indexOf(s[0]);
        int occur2 = compound.indexOf(s[1]);
        if (occur1 + s[0].length() == occur2) {
            return Integer.toString(s[0].length());
        } else {
            try {
                return Integer.toString(s[0].length()) + compound.substring(occur1 + s[0].length(), occur2);
            } catch (StringIndexOutOfBoundsException e) {
                return "";
            }
        }
    }

    private String createBackwardRule(String decomp) {
        String[] s = decomp.split("[ \\-/]");
        return Integer.toString(s[s.length - 1].length());
    }

    public String createRule(String fullform, String baseform) {
        String rule = "";
        String part;
        int bestlength = -1;
        int start;
        int end;
        int occur1;
        int occur2;
        int length = fullform.length();
        for (int i = 0; i < length; i++) {
            for (int j = length; j > i; j--) {
                part = fullform.substring(i, j);
                occur1 = baseform.indexOf(part);
                occur2 = fullform.indexOf(part);
                if ((part.length() > bestlength) && occur1 == 0) {
                    bestlength = part.length();
                    start = i;
                    end = j;
                    rule = "";
                    if (occur2 != 0) {
                        rule = fullform.substring(0, start) + "#";
                    }
                    rule = rule + (fullform.length() - end) + baseform.substring(part.length(), baseform.length());
                }
            }
        }
        if (rule.equals("")) {
            rule = fullform.length() + baseform;
        }
        return rule;
    }

    public String applyRule(String fullform, String wordClass) {
        String baseform = "fehlende Behandlung in applyRule";
        String pattern1 = "[0-9][0-9].*";
        String pattern2 = "[0-9].*";
        int temp;
        if ("undecided".equals(wordClass)) {
            baseform = "undecided";
        } else if (wordClass.substring(0, 1).equals("‰")) {
            temp = Integer.parseInt(wordClass.substring(1, 2));
            baseform = fullform.substring(0, fullform.length() - temp);
            baseform = substitute(baseform);
        } else {
            int i = wordClass.indexOf("#");
            if (i != -1) {
                baseform = fullform.substring(i);
                if (wordClass.substring(i + 1).matches(pattern1)) {
                    temp = Integer.parseInt(wordClass.substring(i + 1, i + 3));
                    baseform = baseform.substring(0, baseform.length() - temp) + wordClass.substring(i + 3);
                } else if (wordClass.substring(i + 1).matches(pattern2)) {
                    temp = Integer.parseInt(wordClass.substring(i + 1, i + 2));
                    baseform = baseform.substring(0, baseform.length() - temp) + wordClass.substring(i + 2);
                }
            } else {
                if (wordClass.matches(pattern1)) {
                    temp = Integer.parseInt(wordClass.substring(0, 2));
                    baseform = fullform.substring(0, fullform.length() - temp) + wordClass.substring(2);
                } else if (wordClass.substring(i + 1).matches(pattern2)) {
                    temp = Integer.parseInt(wordClass.substring(0, 1));
                    if (temp == 0) {
                        baseform = fullform;
                    } else {
                        temp = Integer.parseInt(wordClass.substring(0, 1));
                        baseform = fullform.length() > temp
                                ? fullform.substring(0, fullform.length() - temp) + wordClass.substring(1)
                                : fullform;
                    }
                }
            }
        }
        return baseform;
    }

    private String substitute(String word) {
        return word.replace('‰', 'a')
                .replace('ƒ', 'A')
                .replace('¸', 'u')
                .replace('‹', 'U')
                .replace('ˆ', 'o')
                .replace('÷', 'O');
    }
}
