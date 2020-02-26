# Lucene-Cranfield
Lucene-Cranfield 

### Build Source code:

cd Lucene-Cranfield/

mvn clean install

### Run Source code:

mvn exec:java -Dexec.mainClass="lucene.IndexFiles"

mvn exec:java -Dexec.mainClass="lucene.SearchFiles"

### Evaluate:

execute from /home/sarvani/Lucene-Cranfield

../trec_eval-9.0.7/trec_eval ../QRelsCorrectedforTRECeval src/main/resources/results.txt 
