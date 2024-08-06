package api;

import fr.cnrs.opentheso.bdd.helper.CandidateHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAutoCompletion;
//import fr.cnrs.opentheso.ws.openapi.helper.ApiKeyHelper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

//import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;
import static org.junit.Assert.assertNotNull;

public class apiTest {
    String apiKey = "";
//    ApiKeyHelper apiKeyHelper = new ApiKeyHelper();
    UserHelper userHelper = new UserHelper();
    SearchHelper searchHelper = new SearchHelper();
    HikariDataSource ds;
/*
    @Before
    public void setUp() {
        HikariConfig config = new HikariConfig();
        ds = connect();
    }

    @Test
    public void testGenerateApiKey(){
        System.out.println("Test génération: " + apiKeyHelper.generateApiKey("ot_", 64));

    }

    @Test
    public void testCheckApiKey() {

        try {
            System.out.println("Test checkApiKey: " + apiKeyHelper.checkApiKey(apiKey));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void testGetGroupId() {

        try {
            System.out.println("Test getGroupId: " + userHelper.getUserGroupId(1, "th2"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void testSaveConcept() {
        CandidateHelper candidateHelper = new CandidateHelper();
        String candidateJson = "{"
                + "\"title\": \"Candidat Exemple\","
                + "\"definition\": \"Ceci est une définition d'exemple.\","
                + "\"thesoId\": \"th2\","
                + "\"source\": \"tonton\""
                + "}";

        System.out.println("Test saveConcept: " + candidateHelper.saveCandidat(candidateJson, 1));

    }

    @Test
    public void testSearchAutoCompletionWS_AllParameters() {
        String value = "Ampho";
        String idLang = "fr";
        String[] idGroups = {};
        String idTheso = "th2";
        boolean withNotes = true;

        ArrayList<String> result = searchHelper.searchAutoCompletionWSForWidget(ds, value, idLang, idGroups, idTheso);
        System.out.println(result);
    }

    @Test
    public void testSearchAutoCompletionWS_MissingValue() {
        String value = null;
        String idLang = "en";
        String[] idGroups = {"group1"};
        String idTheso = "theso1";
        boolean withNotes = true;

        ArrayList<NodeAutoCompletion> result = searchHelper.searchAutoCompletionWS(ds, value, idLang, idGroups, idTheso, withNotes);
        assertNotNull(result);
    }

    @Test
    public void testSearchAutoCompletionWS_MissingIdLang() {
        String value = "test";
        String idLang = null;
        String[] idGroups = {"group1"};
        String idTheso = "theso1";
        boolean withNotes = true;

        ArrayList<NodeAutoCompletion> result = searchHelper.searchAutoCompletionWS(ds, value, idLang, idGroups, idTheso, withNotes);
        assertNotNull(result);
    }

    @Test
    public void testSearchAutoCompletionWS_MissingIdGroups() {
        String value = "test";
        String idLang = "en";
        String[] idGroups = null;
        String idTheso = "theso1";
        boolean withNotes = true;

        ArrayList<NodeAutoCompletion> result = searchHelper.searchAutoCompletionWS(ds, value, idLang, idGroups, idTheso, withNotes);
        assertNotNull(result);
    }

    @Test
    public void testSearchAutoCompletionWS_MissingIdTheso() {
        String value = "test";
        String idLang = "en";
        String[] idGroups = {"group1"};
        String idTheso = null;
        boolean withNotes = true;

        ArrayList<NodeAutoCompletion> result = searchHelper.searchAutoCompletionWS(ds, value, idLang, idGroups, idTheso, withNotes);
        assertNotNull(result);
    }

    @Test
    public void testSearchAutoCompletionWS_MissingWithNotes() {
        String value = "test";
        String idLang = "en";
        String[] idGroups = {"group1"};
        String idTheso = "theso1";
        Boolean withNotes = false;

        ArrayList<NodeAutoCompletion> result = searchHelper.searchAutoCompletionWS(ds, value, idLang, idGroups, idTheso, withNotes);
        assertNotNull(result);
    }*/
}
