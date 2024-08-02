/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package concept;

import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class Corpus {
    
    public Corpus() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void getCount() {
        int count = getCountOfResourcesFromHttp("https://pro.frantiq.fr/es/koha_frantiq_biblios/_count?q=koha-auth-number:\"26678/pcrtSkipOsBGML\"");
        System.out.println("" + count);
        
    }
    
    private int getCountOfResourcesFromHttp(String uri) {
        String output;
        String json = "";

        // récupération du total des notices
        try {
            URL url = new URL(uri);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            int status = conn.getResponseCode();
            if (status != 200) {
                return -1;
            }
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((output = br.readLine()) != null) {
                json += output;
            }
            br.close();
            return getCountFromJson(json);

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (IOException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
        }
        return -1;
    }

    private int getCountFromJson(String jsonText) {
        if (jsonText == null) {
            return -1;
        }
        JsonObject jsonObject;
        try {
            JsonReader reader = Json.createReader(new StringReader(jsonText));
            jsonObject = reader.readObject();
            //         System.err.println(jsonText + " #### " + nodeConcept.getConcept().getIdConcept());
            int count = jsonObject.getInt("count");
            return count;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return -1;
        }
    }    
    
}
