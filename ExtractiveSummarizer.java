import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.ling.*;
import java.util.*;

public class ExtractiveSummarizer {
    public static void main(String[] args) {
    	Scanner SC = new Scanner(System.in);
        String inputText = SC.nextLine();

        // Create a StanfordCoreNLP object with tokenize and ssplit annotators
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Create an Annotation object with the input text
        Annotation document = new Annotation(inputText);

        // Run the pipeline on the Annotation object
        pipeline.annotate(document);

        // Get sentences from the Annotation object
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        // Calculate importance scores (example: using sentence length)
        double[] importanceScores = new double[sentences.size()];
        for (int i = 0; i < sentences.size(); i++) {
            importanceScores[i] = sentences.get(i).get(CoreAnnotations.TokensAnnotation.class).size();
        }

        // Rank sentences based on importance
        int[] rankedSentences = rankSentences(importanceScores);

        // Select top sentences for summary
        int summaryLength = Math.max(1, rankedSentences.length/3); // Adjust summary length as needed
        System.out.println("\n");
        StringBuilder summary = new StringBuilder();
        for (int i = 0; i < summaryLength; i++) {
        	System.out.println(sentences.get(rankedSentences[i]).toString());
            summary.append(sentences.get(rankedSentences[i]).toString()).append(" ");
        }

        // Output the summary
        //System.out.println("Original Text:\n" + inputText + "\n");
        //System.out.println("Summary:\n" + summary.toString());
    }

    // Function to rank sentences based on importance scores
    private static int[] rankSentences(double[] scores) {
        // Implement your ranking logic (e.g., sorting indices based on scores)
        // This example uses a simple approach; you may want to explore more sophisticated methods
        int[] indices = new int[scores.length];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        // Sort indices based on scores (descending order)
        for (int i = 0; i < scores.length - 1; i++) {
            for (int j = i + 1; j < scores.length; j++) {
                if (scores[indices[j]] > scores[indices[i]]) {
                    int temp = indices[i];
                    indices[i] = indices[j];
                    indices[j] = temp;
                }
            }
        }

        return indices;
    }
}
