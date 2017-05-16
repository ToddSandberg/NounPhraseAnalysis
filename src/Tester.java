
public class Tester {
public static void main(String[]args){
    CorpusAnalysis c = new CorpusAnalysis();
    c.setCorpus("Test.txt");
    //c.removeSentenceFragments();
    //c.test();
    c.readPargraphWriteSenetences();
}
}
