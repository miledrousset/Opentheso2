package fr.cnrs.opentheso.ws.handlestandard;

import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.PrivateKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.json.Json;
import net.handle.hdllib.AbstractMessage;
import net.handle.hdllib.AbstractResponse;
import net.handle.hdllib.AddValueRequest;
import net.handle.hdllib.AdminRecord;
import net.handle.hdllib.CreateHandleRequest;
import net.handle.hdllib.DeleteHandleRequest;
import net.handle.hdllib.HandleException;
import net.handle.hdllib.HandleResolver;
import net.handle.hdllib.HandleValue;
import net.handle.hdllib.PublicKeyAuthenticationInfo;
import net.handle.hdllib.RemoveValueRequest;
import net.handle.hdllib.Util;

import jakarta.json.JsonObject;
import jakarta.json.JsonReader;

public class HandleService {

    private int responseMsg;
    private String pathKey;
    private String pass;
    private String adminHandle;
    private String prefix;
    private String privatePrefix;
    
    private String serverHandle;
    private int index;    
    private AdminRecord admin;
    
    private PublicKeyAuthenticationInfo auth;
    private static HandleService hs;
    private HandleResolver resolver;

    private String message;
    
    /**
     * retourne le constructeur
     * 
     * @return 
     */
    public static HandleService getInstance() {
        return hs;
    }
    
    static {
        hs = new HandleService();

    }

    private HandleService() {  
    }

    public void applyNodePreference(NodePreference nodePreference){
        serverHandle = nodePreference.getUrlApiHandle();
        pass = nodePreference.getPassHandle();
        prefix = nodePreference.getPrefixIdHandle(); // exp : 20.500.11859
        privatePrefix = nodePreference.getPrivatePrefixHandle();
        
        pathKey = ("certificat/admpriv.bin");
        try {
            index = nodePreference.getIndexHandle(); // exp : 300
        } catch (Exception e) {
        }
        adminHandle = nodePreference.getAdminHandle(); // exp : 0.NA/20.500.11859
    }    
    
    public void connectHandle(){
        
        byte[] key = null;
        try {
            File f = new File(Thread.currentThread().getContextClassLoader().getResource(pathKey).getFile());
            FileInputStream fs = new FileInputStream(f);
            key = Util.getBytesFromInputStream(fs);
        } catch (Throwable t) {
            System.err.println("Cannot read private key " + pathKey + ": " + t);
            System.exit(-1);
        }

        // A HandleResolver object is used not just for resolution, but for
        // all handle operations(including create)
        resolver = new HandleResolver();

        // Check to see if the private key is encrypted.  If so, read in the
        // user's passphrase and decrypt.  Finally, convert the byte[] 
        // representation of the private key into a PrivateKey object.
        PrivateKey privkey = null;
        byte secKey[] = null;
        try {
            if (Util.requiresSecretKey(key)) {
                secKey = pass.getBytes();
            }
            key = Util.decrypt(key, secKey);
            privkey = Util.getPrivateKeyFromBytes(key, 0);
        } catch (Throwable t) {
            System.err.println("Can't load private key in " + pathKey + ": " + t);
            System.exit(-1);
        }

        try {
            // Create a PublicKeyAuthenticationInfo object to pass to HandleResolver.
            // This is constructed with the admin handle, index, and PrivateKey as
            // arguments.
            auth = new PublicKeyAuthenticationInfo(adminHandle.getBytes("UTF8"),
                            index,
                            privkey);

            // We don't want to create a handle without an admin value-- otherwise
            // we would be locked out.  Give ourselves all permissions, even
            // ones that only apply for NA handles.
            admin = new AdminRecord(adminHandle.getBytes("UTF8"), index,
                    true, true, true, true, true, true,
                    true, true, true, true, true, true);

        } catch (Throwable t) {
            System.err.println("\nError: " + t);
        }
    }

    /**
     * peut créer un handle
     * 
     * @param handle
     * @param url
     * @return
     * @throws UnsupportedEncodingException
     * @throws HandleException 
     */
    public boolean createHandle(String handle, String url) throws UnsupportedEncodingException, HandleException {

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            //prefix by http by default
            url = "https://" + url;
        }
        HandleValue[] val = {new HandleValue(100, "URL", url)};

        // Now we can build our CreateHandleRequest object.  As its first
        // parameter it takes the handle we are going to create.  The second
        // argument is the array of initial values the handle should have.
        // The final argument is the authentication object that should be
        // used to gain permission to perform the creation.
        String handleId = prefix + "/" + handle;
        CreateHandleRequest req = new CreateHandleRequest(handleId.getBytes("UTF8"), val, auth);

        AbstractResponse response = resolver.processRequest(req);

        // The responseCode value for a response indicates the status of
        // the request.  A successful resolution will always return
        // RC_SUCCESS.  Failed resolutions could return one of several
        // response codes, including RC_ERROR, RC_INVALID_ADMIN, and
        // RC_INSUFFICIENT_PERMISSIONS.
        if (response.responseCode == AbstractMessage.RC_SUCCESS) {
            System.out.println("\nGot Response: \n" + response);
            message = response.toString();
            this.setResponseMsg(response.responseCode);
            return true;
        } else {
            System.out.println("\nGot Error: \n" + response);
            message = response.toString();
            this.setResponseMsg(response.responseCode);
        }
        return false;
    }

    /**
     * peut mettre à jour l'url d'un Handle
     * 
     * @param handle
     * @param newUrl
     * @return
     * @throws HandleException
     * @throws UnsupportedEncodingException 
     */
    public boolean updateHandleUrl(String handle, String newUrl) throws HandleException, UnsupportedEncodingException {

        String handleId = getPrefix() + "/" + handle;

        if (!newUrl.startsWith("http://") && !newUrl.startsWith("https://")) {
            //prefix by http by default
            newUrl = "http://" + newUrl;
        }

        HandleValue[] val = {new HandleValue(100, "URL", newUrl)};
        RemoveValueRequest removeValueRequest = new RemoveValueRequest(Util.encodeString(handleId), 100, auth);

        AbstractResponse removeValueResponse = resolver.processRequest(removeValueRequest);
        if (removeValueResponse == null || removeValueResponse.responseCode != AbstractMessage.RC_SUCCESS) {
            System.out.println("error removing previous value '" + handle + "': " + removeValueResponse);

        } else {
            AddValueRequest addValueRequest = new AddValueRequest(handleId.getBytes("UTF8"), val, auth);

            AbstractResponse addValueResponse = resolver.processRequest(addValueRequest);
            if (addValueResponse.responseCode == AbstractMessage.RC_SUCCESS) {
                System.out.println("\nGot Response: \n" + addValueResponse);
                return true;
            } else {
                System.out.println("\nGot Error: \n" + addValueResponse);
                this.setResponseMsg(addValueResponse.responseCode);
            }

        }
        return false;

    }

    /**
     * peut supprimer un Handle
     * 
     * @param handleId
     * @return
     * @throws HandleException 
     */
    public boolean deleteHandle(String handleId) throws HandleException {
        DeleteHandleRequest req = new DeleteHandleRequest(Util.encodeString(handleId), auth);
        AbstractResponse response = resolver.processRequest(req);
        if (response == null || response.responseCode != AbstractMessage.RC_SUCCESS) {
            System.out.println("error deleting '" + handleId + "': " + response);
            this.setResponseMsg(response.responseCode);
        } else {
            System.out.println("deleted " + handleId);
            return true;
        }
        return false;
    }

    /**
     * peut récupérer un Handle
     * 
     * @param handle
     * @return 
     */
    public String getHandle(String handle) {
        String output;
        String xmlRecord = "";
        try {
            URL url = new URL(serverHandle + prefix + "/" + handle);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            int status = conn.getResponseCode();
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

            try ( BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                while ((output = br.readLine()) != null) {
                    xmlRecord += output;
                }
                byte[] bytes = xmlRecord.getBytes();
                xmlRecord = new String(bytes, Charset.forName("UTF-8"));
                
                message = "";
                switch (status) {
                    case 200:
                        message = "Récupération du Handle réussie";
                        break;
                    default:
                        message = "Handle n'existe pas";
                        break;
                }
                conn.disconnect();
                br.close();
            }

            if (status == 200) {
                return xmlRecord;
            } else {
                return null;
            }

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(HandleService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(HandleService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(HandleService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(HandleService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * peut vérifier si l'id handle existe
     * 
     * @param handle_id
     * @return 
     */
    public boolean handleExist(String handle_id) {
        try {
            JsonObject jsonObject;
            try (JsonReader reader = Json.createReader(new StringReader(handle_id))) {
                jsonObject = reader.readObject();        
            }
            if (jsonObject.getInt("responseCode") == 1) {
                return true;
            }        
        } catch (NullPointerException ex) {
        }        
        return false;
        
        /*
        String res = getHandle(handle_id);
        JsonObject json;
        try {
            json = new JsonObject(res);
        } catch (NullPointerException ex) {
            return false;
        }
        if (json.getInt("responseCode") == 1) {
            return true;
        }
        return true;*/
    }

    /**
     * @return the responseMsg
     */
    public int getResponseMsg() {
        return responseMsg;
    }

    /**
     * @param responseMsg the responseMsg to set
     */
    public void setResponseMsg(int responseMsg) {
        this.responseMsg = responseMsg;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return if isSuccess
     */
    public boolean isSuccess() {
        return responseMsg == AbstractMessage.RC_SUCCESS;
    }

    public void cleanResponseMsg() {

        responseMsg = 0;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    public String getPrivatePrefix() {
        return privatePrefix;
    }

    public void setPrivatePrefix(String privatePrefix) {
        this.privatePrefix = privatePrefix;
    }
   
}
