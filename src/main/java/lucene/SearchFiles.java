package lucene;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

public class SearchFiles {

    private SearchFiles() {}

    // Reads the index at "index" and writes results to "my-results.txt"
    public static void main(String[] args) throws Exception {

        //--------- Create reader, writer & searcher ----------

        String index = "src/main/resources/index";
        String results_path = "src/main/resources/my-results-bm25-eng";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        PrintWriter writer = new PrintWriter(results_path, "UTF-8");
        IndexSearcher searcher = new IndexSearcher(reader);

        // Choose Analyzer
        // initializing  stop words and using EnglishAnalyzer
        List<String> stopWordList = Arrays.asList("a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from", "further", "had", "has", "have", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "it", "it's", "its", "itself", "let's", "me", "more", "most", "my", "myself", "nor", "of", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "she", "she'd", "she'll", "she's", "should", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "we", "we'd", "we'll", "we're", "we've", "were", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "would", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves");
        CharArraySet stopWordSet = new CharArraySet( stopWordList, true);

        //WhitespaceAnalyzer – Splits tokens at whitespace
//        Analyzer analyzer = new WhitespaceAnalyzer();

        //SimpleAnalyzer – Divides text at non letter characters and lowercases
//        Analyzer analyzer = new SimpleAnalyzer();

//        //StopAnalyzer – Divides text at non letter characters, lowercases, and removes stop words
//        Analyzer analyzer = new StopAnalyzer(stopWordSet);



//        //StandardAnalyzer - Tokenizes based on sophisticated grammar that recognizes e-mail addresses, acronyms, etc.; lowercases and removes stop words (optional)
//                Analyzer analyzer = new StandardAnalyzer(stopWordSet);


        //EnglishAnalyzer - Analyzer with enhancements for stemming English words
        Analyzer analyzer = new EnglishAnalyzer(stopWordSet);

        //---------------- Choose Scoring method ----------------

        //Vector Space Model
//        searcher.setSimilarity(new ClassicSimilarity());

        //BM25
        searcher.setSimilarity(new BM25Similarity());

        // Read in and parse queries

        String queriesPath = "src/main/resources/cran.qry";
        BufferedReader buffer = Files.newBufferedReader(Paths.get(queriesPath), StandardCharsets.UTF_8);
        MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[] {"title","author","bibliography","content"}, analyzer);

        String queryString = "";
        Integer queryID = 1;
        String line;
        Boolean first = true;

//        System.out.println("Reading in queries and creating search results.");

        while ((line = buffer.readLine()) != null){

            if(line.substring(0,2).equals(".I")){
                if(!first){
                    Query query = parser.parse(QueryParser.escape(queryString));
                    performSearch(searcher,writer,queryID,query);
                    queryID++;
                }
                else{ first=false; }
                queryString = "";
            } else {
                queryString += " " + line;
            }
        }

        Query query = parser.parse(QueryParser.escape(queryString));
        performSearch(searcher,writer,queryID,query);

        writer.close();
        reader.close();
    }

    // Performs search and writes results to the writer
    public static void performSearch(IndexSearcher searcher, PrintWriter writer, Integer queryID, Query query) throws IOException {
        TopDocs results = searcher.search(query, 50);
        ScoreDoc[] hits = results.scoreDocs;

        // Write the results for each hit
        //format : query-id Q0 document-id rank score STANDARD
        for(int i=0;i<hits.length;i++){
            Document doc = searcher.doc(hits[i].doc);
            writer.println(queryID + " Q0 " + doc.get("id") + " " + i + " " + hits[i].score + " STANDARD");
        }
    }

}
