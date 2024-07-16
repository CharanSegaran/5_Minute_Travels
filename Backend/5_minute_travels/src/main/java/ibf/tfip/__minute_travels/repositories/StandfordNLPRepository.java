package ibf.tfip.__minute_travels.repositories;

import org.springframework.stereotype.Repository;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.*;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.amadeus.exceptions.ResponseException;



@Repository
public class StandfordNLPRepository {

    @Autowired
    BackEndRepository backEndRepository;


    private final StanfordCoreNLP pipeline;


    public StandfordNLPRepository(){
        //set up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        props.setProperty("ner.docdate.usePresent", "true");
        props.setProperty("sutime.includeRange", "true");
        props.setProperty("sutime.markTimeRanges", "true");
        pipeline = new StanfordCoreNLP(props);
        
    }

    public Map<String,String> extractRequestDetails(String query) throws ResponseException{
        Map<String,String> details = new HashMap<>();
        CoreDocument document = new CoreDocument(query);
        pipeline.annotate(document);

        List<CoreEntityMention> entityMentions = document.entityMentions();

        for (int i = 0; i < entityMentions.size(); i++) {
            CoreEntityMention em = entityMentions.get(i);
            if(details.get("departureDate")!=null &&
               details.get("returnDate") != null &&
               details.get("originLocationCode") != null &&
               details.get("destinationLocationCode")!=null) i=entityMentions.size();
            switch (em.entityType()) {
                case "DATE":
                    String convertedDate = backEndRepository.convertDate(em.text());
                    if(details.get("departureDate") == null)  details.put("departureDate", convertedDate);
                    else details.put("returnDate", convertedDate);
                    break;
                case "CITY":
                    String iataCodeFromCity = backEndRepository.findIATACodeFromCity(em.text());
                    if(details.get("originLocationCode") == null) details.put("originLocationCode", iataCodeFromCity);
                    else details.put("destinationLocationCode", iataCodeFromCity);
                    break;
                case "STATE_OR_PROVINCE":
                    String iataCodeFromState = backEndRepository.findIATACodeFromCity(em.text());
                    if(details.get("originLocationCode") == null) details.put("originLocationCode", iataCodeFromState);
                    else details.put("destinationLocationCode", iataCodeFromState);
                    break;
                case "LOCATION":
                    String iataCodeFromAirport = backEndRepository.findIATACodeFromAIrport(em.text());
                    if(details.get("originLocationCode") == null) details.put("originLocationCode", iataCodeFromAirport);
                    else details.put("destinationLocationCode", iataCodeFromAirport);
                    break;
                case "COUNTRY":
                    String iataCodeFromCountry = backEndRepository.findIATACodeFromCountry(em.text());
                    if(details.get("originLocationCode") == null) details.put("originLocationCode", iataCodeFromCountry);
                    else details.put("destinationLocationCode", iataCodeFromCountry);
                    break;
                case "NUMBER":
                    if(details.get("adults") == null) details.put("adults", em.text());
                    else details.put("children", em.text());
                default:
                    break;
                }
            }
        return details;
    }

    public void getNERTag(String query){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        Annotation document = new Annotation(query);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            // Get the tokens in the sentence
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // Get the word, its NER tag, and print them
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                System.out.println(word + " : " + ner);
            }
        }
    }
}
