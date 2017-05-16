
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class CorpusAnalysis {
    public enum Stage {
        LOAD, KILL
    }

    private BufferedReader buffReader;
    private BufferedWriter buffWriter;
    private FileWriter fileWriter;
    private ArrayList<Noun> nouns = new ArrayList<Noun>();
    private StanfordCoreNLP pipeline;
    private String corpusPath = "";

    public CorpusAnalysis() {
        Properties props = new Properties();
        props.put("annotators",
                "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        // props.put("annotators", "tokenize, ssplit, pos");
        pipeline = new StanfordCoreNLP(props);
    }

    /**
     * Sets the path to the corpus and opens the buffered readers / writers.
     */
    public void setCorpus(String corpusPath) {
        this.corpusPath = corpusPath;
        File file;
        String pathAndFilenameWithExtension;
        String pathAndFilenameNoExtension;

        try {
            file = new File(corpusPath);
            System.out.println("\n");
            pathAndFilenameWithExtension = file.getParent() + "/"
                    + file.getName();
            pathAndFilenameNoExtension = pathAndFilenameWithExtension
                    .substring(0, pathAndFilenameWithExtension.indexOf("."));
            System.out.println(pathAndFilenameWithExtension);
            System.out.println(pathAndFilenameNoExtension);

            buffReader = new BufferedReader(new FileReader(corpusPath));

            /*
             * fileWriter = new FileWriter("output.txt"); buffWriter = new
             * BufferedWriter(fileWriter);
             */
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert from run-on paragraph format to individual sentences with line
     * breaks separating each sentence.
     */
    public void readPargraphWriteSenetences() {
        String line;
        Annotation document = null;

        try {
            document = new Annotation(buffReader.readLine());
            pipeline.annotate(document);

            while ((line = buffReader.readLine()) != null) {
                // Annotates the text file sentence by sentence
                document = new Annotation(line);
                pipeline.annotate(document);
                List<CoreMap> sentences = document
                        .get(SentencesAnnotation.class);
                for (CoreMap sentence : sentences) {
                    // Arraylist of words in the sentence
                    ArrayList<Word> words = new ArrayList<Word>();
                    for (CoreLabel token : sentence
                            .get(TokensAnnotation.class)) {
                        // this is the text of the token
                        String word = token.get(TextAnnotation.class);
                        // this is the POS tag of the token
                        String pos = token.get(PartOfSpeechAnnotation.class);
                        // this is the NER label of the token
                        String ne = token.get(NamedEntityTagAnnotation.class);
                        Word w = new Word(word, pos, ne);
                        // System.out.println(word +" "+pos+ " "+ne);
                        words.add(w);
                    }
                    // Goes through the Words arraylist
                    for (int i = 0; i < words.size() - 1; i++) {
                        // Checks if the words part of speech is an adjective
                        if (words.get(i).pos.equals("JJ")) {
                            String adjectiveString = words.get(i).word;
                            // goes through the proceeding words and adds them
                            // into the adjective string until it reaches a
                            // nonadjective
                            i++;
                            while (i < words.size() - 1
                                    && words.get(i).pos == "JJ") {
                                adjectiveString += "," + words.get(i).word;
                                i++;
                            }
                            // if the next word is a noun save it
                            if (words.get(i).pos.equals("NN")) {
                                Noun n = new Noun(words.get(i).word);
                                //Check if the noun arraylist already has said noun
                                if (nouns.contains(n)) {
                                    //find said noun in the arraylist
                                    for (int x = 0; x < nouns.size(); x++) {
                                        if (nouns.get(x).equals(n)) {
                                            nouns.get(x).count++;
                                            addAdjective(nouns.get(x),
                                                    adjectiveString);
                                        }
                                    }
                                }
                                else {
                                    addAdjective(n, adjectiveString);
                                    nouns.add(n);

                                }
                            }
                        }

                    }

                    /*
                     * buffWriter.append(sentence.toString());
                     * buffWriter.newLine();
                     */
                }
                //print all nouns in the noun arraylist
                for (int x = 0; x < nouns.size(); x++) {
                    System.out.println(nouns.get(x).toString());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (buffWriter != null)
                    buffWriter.close();
                if (fileWriter != null)
                    fileWriter.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /*
     * public void test(){ HashMap<String,Integer> p = new
     * HashMap<String,Integer>(); int pizzaget = 0; p.put("pizza", 1); pizzaget
     * = p.get("pizza"); System.out.println(pizzaget); if(pizzaget>0){
     * p.put("pizza", pizzaget+1); } System.out.println(p.get("pizza")); }
     */
    /**
     * adds the adjective String into the given noun
     * @param n
     * @param adjectives formatted like "fast,quick"
     */
    public void addAdjective(Noun n, String adjectives) {
        String[] split = adjectives.split(",");
        for (int c = 0; c < split.length; c++) {
            if (n.adjectives.get(split[c]) != null) {
                n.adjectives.put(split[c], n.adjectives.get(split[c]) + 1);
            }
            else
                n.adjectives.put(split[c], 0);
        }
    }

    /**
     * We're trying to find sentence fragments and remove them. If a "line of
     * text" doesn't have a "root" that is a verb, we consider it a fragment and
     * filter it out.
     */
    public void removeSentenceFragments() {
        String line;
        Annotation document = null;
        int c;

        try {
            document = new Annotation(buffReader.readLine());
            pipeline.annotate(document);

            while ((line = buffReader.readLine()) != null) {

                document = new Annotation(line);
                pipeline.annotate(document);
                // System.out.println(line);

                List<CoreMap> sentences = document
                        .get(SentencesAnnotation.class);
                for (CoreMap sentence : sentences) {
                    System.out.println(sentence);
                    buffWriter.append(sentence.toString());
                    buffWriter.newLine();

                    /*
                     * for (CoreLabel token:
                     * sentence.get(TokensAnnotation.class)) { // this is the
                     * text of the token String word =
                     * token.get(TextAnnotation.class); // this is the POS tag
                     * of the token //String pos =
                     * token.get(PartOfSpeechAnnotation.class); // this is the
                     * NER label of the token //String ne =
                     * token.get(NamedEntityTagAnnotation.class);
                     * 
                     * //System.out.println(word + " " + pos + " " + ne); }
                     * 
                     */

                }

            }

        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            try {
                if (buffWriter != null)
                    buffWriter.close();
                if (fileWriter != null)
                    fileWriter.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void extractSentencesWith(String regexString) {
        String line;
        Annotation document = null;
        int c;

        try {
            document = new Annotation(buffReader.readLine());
            pipeline.annotate(document);

            while ((line = buffReader.readLine()) != null) {

                document = new Annotation(line);
                pipeline.annotate(document);
                // System.out.println(line);

                List<CoreMap> sentences = document
                        .get(SentencesAnnotation.class);
                for (CoreMap sentence : sentences) {
                    System.out.println(sentence);
                    buffWriter.append(sentence.toString());
                    buffWriter.newLine();

                    /*
                     * for (CoreLabel token:
                     * sentence.get(TokensAnnotation.class)) { // this is the
                     * text of the token String word =
                     * token.get(TextAnnotation.class); // this is the POS tag
                     * of the token //String pos =
                     * token.get(PartOfSpeechAnnotation.class); // this is the
                     * NER label of the token //String ne =
                     * token.get(NamedEntityTagAnnotation.class);
                     * 
                     * //System.out.println(word + " " + pos + " " + ne); }
                     * 
                     */

                }

            }

        }
        catch (Exception e) {
            System.out.println(e);
        }
        finally {
            try {
                if (buffWriter != null)
                    buffWriter.close();
                if (fileWriter != null)
                    fileWriter.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}