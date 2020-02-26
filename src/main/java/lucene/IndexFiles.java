package lucene;

// Input / Output
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

// Lucene Analyzers
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

// Lucene index creation methods
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class IndexFiles {

    private IndexFiles() {}

    /** Index all text files under a directory. */
    public static void main(String[] args) {

        //---------------- Set up file paths ----------------

        String indexPath = "src/main/resources/index";
        String docsPath = "src/main/resources/cran.all.1400";
        final Path docDir = Paths.get(docsPath);
        if (!Files.isReadable(docDir)) {
            System.out.println("Document directory '" +docDir.toAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        try {
            Directory directory = FSDirectory.open(Paths.get(indexPath));
            // initializing  stop words and using EnglishAnalyzer
            List<String> stopWordList = Arrays.asList("a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "could", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from", "further", "had", "has", "have", "having", "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is", "it", "it's", "its", "itself", "let's", "me", "more", "most", "my", "myself", "nor", "of", "on", "once", "only", "or", "other", "ought", "our", "ours", "ourselves", "out", "over", "own", "same", "she", "she'd", "she'll", "she's", "should", "so", "some", "such", "than", "that", "that's", "the", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've", "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "we", "we'd", "we'll", "we're", "we've", "were", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom", "why", "why's", "with", "would", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves");
            CharArraySet stopWordSet = new CharArraySet( stopWordList, true);

            //Choose Analyzer

            //WhitespaceAnalyzer – Splits tokens at whitespace
            //Analyzer analyzer = new WhitespaceAnalyzer();

            //SimpleAnalyzer – Divides text at non letter characters and lowercases
//            Analyzer analyzer = new SimpleAnalyzer();

            //StopAnalyzer – Divides text at non letter characters, lowercases, and removes stop words
//            Analyzer analyzer = new StopAnalyzer(stopWordSet);

            //StandardAnalyzer - Tokenizes based on sophisticated grammar that recognizes e-mail addresses, acronyms, etc.; lowercases and removes stop words (optional)
//            Analyzer analyzer = new StandardAnalyzer(stopWordSet);

            //EnglishAnalyzer
            Analyzer analyzer = new EnglishAnalyzer(stopWordSet);

            //CustomAnalyzer - Defined in CustomAnalyzer.java
//            Analyzer analyzer = new CustomAnalyzer();

            // Create indexes

            // Create IndexWriter
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            iwc.setOpenMode(OpenMode.CREATE);
            IndexWriter writer = new IndexWriter(directory, iwc);

            // Index the files
            indexFiles(writer, docDir);

            writer.close();

        } catch (IOException e) {
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
        }
    }


    // Creates a document with the fields specified to be written to an index
    static Document createDocument(String id, String title, String author, String bibliography, String content){
        Document doc = new Document();
        doc.add(new StringField("id", id, Field.Store.YES));
        doc.add(new StringField("path", id, Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("author", author, Field.Store.YES));
        doc.add(new TextField("bibliography", bibliography, Field.Store.YES));
        doc.add(new TextField("content", content, Field.Store.YES));
        return doc;
    }

//    Indexes the cranfield collection
    static void indexFiles(IndexWriter writer, Path file) throws IOException {
        try (InputStream stream = Files.newInputStream(file)) {

            BufferedReader br = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

            String id = "", title = "", author = "", bib = "", content = "", state = "";
            Boolean create = true;
            String line;

            System.out.println("Indexing documents...");

            // Read in lines from the cranfield collection and create indexes for them
            while ((line = br.readLine()) != null){
                switch(line.substring(0,2)){
                    case ".I":
                        if(!create){
                            Document doc = createDocument(id,title,author,bib,content);
                            writer.addDocument(doc);
                        }
                        else{ create=false; }
                        title = ""; author = ""; bib = ""; content = "";
                        id = line.substring(3,line.length()); break;
                    case ".T":
                    case ".A":
                    case ".B":
                    case ".W":
                        state = line; break;
                    default:
                        switch(state){
                            case ".T": title += line + " "; break;
                            case ".A": author += line + " "; break;
                            case ".B": bib += line + " "; break;
                            case ".W": content += line + " "; break;
                        }
                }
            }
            Document document = createDocument(id,title,author,bib,content);
            writer.addDocument(document);
        }
    }
}
