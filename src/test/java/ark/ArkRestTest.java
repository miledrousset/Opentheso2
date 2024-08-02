package ark;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
//import com.sun.jersey.api.client.Client;
import java.io.StringReader;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
@Ignore
public class ArkRestTest {
    
    public ArkRestTest() {
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
    
    private String getTokenFromString(String tokenString){
        String token;
        try (jakarta.json.JsonReader jsonReader = jakarta.json.Json.createReader(new StringReader(tokenString))) {
            JsonObject jo = jsonReader.readObject();
            token = jo.getString("token").trim();
        } catch (Exception e) {
            return null;
        }
        return token;
    }  
    
    /**
     * pour établir la connexion
     * @return
     * #MR
     * OK validé
     */
    public String login() {
        Client client= ClientBuilder.newClient();
        String user = "demo";
        String pass = "demo2";
        String naan = "66666";
        String url = "http://localhost:8082/Arkeo/rest/login/";        
        
      
        String path =  "username=" +
                        user + 
                        "&password=" +
                        pass +
                        "&naan=" + 
                        naan;
        
        WebTarget webTarget = client.target(url).path(path);
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);        
        Response response = invocationBuilder.get();        
        if(response.getStatus() != 200) {
             return null;
        }
        String value = response.readEntity(String.class);
        return value;
    }
    
    @Test
    public void putArk() {
        String tokenString = login();
        String token = getTokenFromString(tokenString);
        if(token == null) return;
      
  /*      
        
        String output;
        String xmlRecord = "";

        String urlArkeo = "http://localhost:8082/Arkeo/rest/v1/ark/add";

        String jsonData = "{\"token\":\"" + token + "\",\"ark\":\"\",\"naan\":\"66666\",\"type\":\"prefix\",\"urlTarget\":\"http://testgg11.fr\",\"title\":\"test1\",\"creator\":\"Miled23\",\"useHandle\":false,\"modificationDate\":\"2019-12-10\",\"dcElements\":[{\"name\":\"creator\",\"value\":\"miledDC\",\"language\":\"fr\"},{\"name\":\"title\",\"value\":\"pour tester1\",\"language\":\"fr\"},{\"name\":\"description\",\"value\":\"description 1\",\"language\":\"fr\"}]}";          
    //    String jsonData = getJsonData("http://www.mom.fr");
        
        Client client= Client.create();

        WebResource webResource = client
                .resource(urlArkeo);

        ClientResponse response = webResource.type("application/json")
                .put(ClientResponse.class, jsonData);
        if (response.getStatus() == 200) {
            String jsonArk = response.getEntity(String.class);
            System.err.println(jsonArk);
           // setIdArkHandle();
           // return true;
        }
   */
    }       
    
    
    

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
    public void getJsonFromArk() {
        String test = getJsonFromArk__();
    }
    
    private String getJsonFromArk__(){
        
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();        
        
        jsonObjectBuilder.add("status", "OK");
        jsonObjectBuilder.add("description", "Ark added");        
        jsonObjectBuilder.add("token", "newtoken");
        
        // les objets
        JsonObjectBuilder job = Json.createObjectBuilder();
        
        // pour DC
        JsonArrayBuilder jsonArrayBuilderDC = Json.createArrayBuilder();
        JsonObjectBuilder jobDC = Json.createObjectBuilder();
        
        job.add("ark", "123");
        job.add("naan", "6666");
        job.add("handle", "8008/666.123");
        job.add("handle_prefix", "fsdf");
        job.add("urlTarget", "http://testgg8.fr");
        job.add("title", "test1");
        job.add("creator", "Miled23");      
        
        for (int i = 0; i < 4; i++) {
            jobDC.add("name", "name" + i);
            jobDC.add("value", "value" + i);
            jobDC.add("language", "language" + i);
            i++;
            jsonArrayBuilderDC.add(jobDC.build());
        }
        job.add("dcElements", jsonArrayBuilderDC.build()); 

        jsonObjectBuilder.add("result", job.build()); 
        return jsonObjectBuilder.build().toString();
    }
    

      
    
/*    
    private String connect() {

        String output = "";
        String xmlRecord = "";
        String user = "demo";
        String pass = "demo2";
        String naan = "66666";
        
        String urlArk = "http://localhost:8082/Arkeo/rest/login/";

        try {
            URL url = new URL(urlArk + "username=" + user + "&password=" + pass + "&naan=" + naan);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int status = conn.getResponseCode();
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            // status = 200 = La lecture a réussie
            

            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            while ((output = br.readLine()) != null) {
                xmlRecord += output;
            }
            byte[] bytes = xmlRecord.getBytes();
            xmlRecord = new String(bytes, Charset.forName("UTF-8"));

            System.err.println("Status de la réponse : " + status);            
            System.out.println(xmlRecord);
            conn.disconnect();
       //     System.out.println(getIdHandle(xmlRecord));

        } catch (UnsupportedEncodingException ex) {
            System.out.println(ex);
        } catch (MalformedURLException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return xmlRecord;
        
    /*    HandleClient handleClient = new HandleClient();
        String test = handleClient.getHandle(pass, pathKey, pathCert, urlHandle, idHandle, internalId);
        System.err.println(test);
        System.err.println(handleClient.getMessage());*/
  /*  }  */  
    
    

    
    
/*    public String login() {
        Client client= ClientBuilder.newClient();
        String user = "demo";
        String pass = "demo2";
        String naan = "66666";
        String url = "http://localhost:8082/Arkeo/rest/login/";        
        
      
        String path =  "username=" +
                        user + 
                        "&password=" +
                        pass +
                        "&naan=" + 
                        naan;
        
        WebTarget webTarget = client.target(url).path(path);
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);        
        Response response = invocationBuilder.get();        
        if(response.getStatus() != 200) {
             return null;
        }
        String value = response.readEntity(String.class);
        return value;

        
//////////// ancienne version ////////////
//////////// dépriécié ///////////////////

    //    String response = target.request().get(String.class);

           /* throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());*/

     //       return null;
    //    }        
        
 //       Response response = target.request().put(Entity.json(url2));        
  //      String entity = response.getEntity(String.class);
   //     return entity;
  /*      WebResource webResource = client
                .resource(
                        urlArk +
                        "rest/login/username=" +
                        user + 
                        "&password=" +
                        pass +
                        "&naan=" + 
                        naan);

        ClientResponse response = webResource.accept("application/json")
                .get(ClientResponse.class);

        if (response.getStatus() != 200) {
           /* throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());*/

     //       return null;
    //    }
  //      return response.getEntity(String.class);
//        loginJson = new JSONObject(response.getEntity(String.class));
//        return true;
    //} 
    
 
    
}
