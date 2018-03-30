import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
This class contains details such as the word frequency count for each term and term frequency for each term of the document.
 */
 class DocumentProperties{


public

     HashMap<String,Double> getTermFreqMap()
     {
         return termFreqMap;
     }

     HashMap<String,Integer> getWordCountMap()
     {
         return DocWordCounts;
     }

     void setTermFreqMap(HashMap<String,Double> inMap)
     {
          termFreqMap = new HashMap<String, Double>(inMap);
     }


     void setWordCountMap(HashMap<String,Integer> inMap)
     {
         DocWordCounts =new HashMap<String, Integer>(inMap);
     }
private
     HashMap<String,Double> termFreqMap ;
     HashMap<String,Integer> DocWordCounts;
}


public class TfidfCalculation {

    SortedSet<String> wordList = new TreeSet(String.CASE_INSENSITIVE_ORDER);

    //Calculates inverse Doc frequency.
   public HashMap<String,Double> calculateInverseDocFrequency(DocumentProperties [] docProperties)
    {

      HashMap<String,Double> InverseDocFreqMap = new HashMap<>();
        int size = docProperties.length;
        double wordCount ;
        for (String word : wordList) {
            wordCount = 0;
            for(int i=0;i<size;i++)
            {
                HashMap<String,Integer> tempMap = docProperties[i].getWordCountMap();
                if(tempMap.containsKey(word))
                {
                    wordCount++;
                    continue;
                }
            }
            double temp = size/ wordCount;
            double idf = 1 + Math.log(temp);
            InverseDocFreqMap.put(word,idf);
        }
        return InverseDocFreqMap;
    }

    //calculates Term frequency for all terms
    public HashMap<String,Double> calculateTermFrequency(HashMap<String,Integer>inputMap) {

        HashMap<String ,Double> termFreqMap = new HashMap<>();
        double sum = 0.0;
        //Get the sum of all elements in hashmap
        for (float val : inputMap.values()) {
            sum += val;
        }

        //create a new hashMap with Tf values in it.
        Iterator it = inputMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            double tf = (Integer)pair.getValue()/ sum;
            termFreqMap.put((pair.getKey().toString()),tf);
        }
        return termFreqMap;
    }

    //Returns if input contains numbers or not
    public  boolean isDigit(String input)
    {
        String regex = "(.)*(\\d)(.)*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        boolean isMatched = matcher.matches();
        if (isMatched) {
            return true;
        }
        return false;
    }

    //Writes the contents of hashmap to CSV file
    public  void outputAsCSV(HashMap<String,Double>treeMap,String OutputPath) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Double> keymap : treeMap.entrySet()) {
            builder.append(keymap.getKey());
            builder.append(",");
            builder.append(keymap.getValue());
            builder.append("\r\n");
        }
        String content = builder.toString().trim();
        BufferedWriter writer = new BufferedWriter(new FileWriter(OutputPath));
        writer.write(content);
        writer.close();
    }
    //cleaning up the input by removing .,:"
    public  String cleanseInput(String input)
    {
        String newStr = input.replaceAll("[, . : ;\"]", "");
        newStr = newStr.replaceAll("\\p{P}","");
        newStr = newStr.replaceAll("\t","");
        return newStr;
    }
    // Converts the input text file to hashmap and even dumps the final output as CSV files
    public  HashMap<String, Integer> getTermsFromFile(String Filename,int count,File folder) {
        HashMap<String,Integer> WordCount = new HashMap<String,Integer>();
        BufferedReader reader = null;
        HashMap<String, Integer> finalMap = new HashMap<>();
        try
        {
            reader = new BufferedReader(new FileReader(Filename));
            String line = reader.readLine();
            while(line!=null)
            {
                String[] words = line.toLowerCase().split(" ");
                for(String term : words)
                {
                    //cleaning up the term ie removing .,:"
                    term = cleanseInput(term);
                    //ignoring numbers
                    if(isDigit(term))
                    {
                        continue;
                    }
                    if(term.length() == 0)
                    {
                        continue;
                    }
                    wordList.add(term);
                    if(WordCount.containsKey(term))
                    {
                        WordCount.put(term,WordCount.get(term)+1);
                    }
                    else
                    {
                        WordCount.put(term,1);
                    }
                }
                line = reader.readLine();
            }
            // sorting the hashmap
            Map<String, Integer> treeMap = new TreeMap<>(WordCount);
            finalMap = new HashMap<String, Integer>(treeMap);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return finalMap;
    }


    public static void main(String Args[]) throws IOException {
        System.out.print("Enter path for input files ");
        Scanner scan = new Scanner(System.in);
        int count = 0;
        TfidfCalculation TfidfObj = new TfidfCalculation();
        File folder = new File(scan.nextLine());
        // loops over files available in the path except for hidden files.
        File[] listOfFiles = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return !file.isHidden();
            }
        });
        int noOfDocs = listOfFiles.length;
        //containers for documents and their properties required to calculate final score
        DocumentProperties [] docProperties = new DocumentProperties[noOfDocs];


        // Get wordcount from file and calculate TermFrequency
        for (File file : listOfFiles) {
            if (file.isFile()) {
                docProperties[count] = new DocumentProperties();
                HashMap<String,Integer> wordCount = TfidfObj.getTermsFromFile(file.getAbsolutePath(),count, folder);
                docProperties[count].setWordCountMap(wordCount);
                HashMap<String,Double> termFrequency = TfidfObj.calculateTermFrequency(docProperties[count].DocWordCounts);
                docProperties[count].setTermFreqMap(termFrequency);
                count++;
            }
        }
        //calculating InverseDocument frequency
        HashMap<String,Double> inverseDocFreqMap = TfidfObj.calculateInverseDocFrequency(docProperties);

        //Calculating tf-idf
        count = 0;
        for (File file : listOfFiles) {
            if (file.isFile()) {
                HashMap<String,Double> tfIDF = new HashMap<>();
                double tfIdfValue = 0.0;
                double idfVal = 0.0;
                HashMap<String,Double> tf = docProperties[count].getTermFreqMap();
                Iterator itTF = tf.entrySet().iterator();
                while (itTF.hasNext()) {
                    Map.Entry pair = (Map.Entry)itTF.next();
                    double tfVal  = (Double)pair.getValue() ;
                    if(inverseDocFreqMap.containsKey((String)pair.getKey()))
                    {
                         idfVal = inverseDocFreqMap.get((String)pair.getKey());
                    }
                    tfIdfValue = tfVal *idfVal;
                    tfIDF.put((pair.getKey().toString()),tfIdfValue);
                }
                int fileNameNumber = (count+1);
                String OutPutPath = folder.getAbsolutePath()+"/csvOutput"+file.getName()+fileNameNumber+".csv";
                TfidfObj.outputAsCSV(tfIDF,OutPutPath);
                count++;
            }
        }
    }
}
