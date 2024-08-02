package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import fr.cnrs.opentheso.bdd.helper.dao.NodeFullConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Data;

@Data
public class SearchCorpus2 {
    private boolean haveCorpus;

    public ArrayList<NodeCorpus> SearchCorpus(ArrayList<NodeCorpus> nodeCorpuses, NodeFullConcept nodeFullConcept) {
        haveCorpus = false;
        if (nodeFullConcept != null) {
            for (NodeCorpus nodeCorpus : nodeCorpuses) {
                // cas où on compose uniquement une URL de lien vers les notices
                if (nodeCorpus.isIsOnlyUriLink()) {
                    if (nodeCorpus.getUriLink().contains("##id##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##id##", nodeFullConcept.getIdentifier()));
                    }
                    if (nodeCorpus.getUriLink().contains("##value##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##value##", nodeFullConcept.getPrefLabel().getLabel()));
                    }
                } else {
                    // recherche par Id
                    /// pour le count par Id interne
                    if (nodeCorpus.getUriCount().contains("##id##")) {
                        if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                            nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##id##", nodeFullConcept.getIdentifier()));
                        }
                    }
                    /// pour le count par Id ark
                    if (nodeCorpus.getUriCount().contains("##arkid##")) {
                        if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                            nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##arkid##", nodeFullConcept.getPermanentId()));
                        }
                    }

                    /// pour la construction de l'URL avec Id interne
                    if (nodeCorpus.getUriLink().contains("##id##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##id##", nodeFullConcept.getIdentifier()));
                    }
                    /// pour la construction de l'URL avec Id Ark
                    if (nodeCorpus.getUriLink().contains("##arkid##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##arkid##", nodeFullConcept.getPermanentId()));
                    }

                    // recherche par value
                    if (nodeCorpus.getUriCount().contains("##value##")) {
                        if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                            nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##value##", nodeFullConcept.getPrefLabel().getLabel()));
                        }
                    }
                    if (nodeCorpus.getUriLink().contains("##value##")) {
                        nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##value##", nodeFullConcept.getPrefLabel().getLabel()));
                    }
                    setCorpusCount(nodeCorpus);
                }
            }
        }
        return nodeCorpuses;
    }

    private void setCorpusCount(NodeCorpus nodeCorpus) {
        if (nodeCorpus == null) {
            return;
        }
        nodeCorpus.setCount(getCountOfResourcesFromHttp(nodeCorpus.getUriCount()));
        /*if (nodeCorpus.getUriCount().contains("https://")) {
            nodeCorpus.setCount(getCountOfResourcesFromHttps(nodeCorpus.getUriCount()));
        }
        if (nodeCorpus.getUriCount().contains("http://")) {
            nodeCorpus.setCount(getCountOfResourcesFromHttp(nodeCorpus.getUriCount()));
        }*/
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
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            int status = conn.getResponseCode();
            if (status != 200) {
                return -1;
            }
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
                while ((output = br.readLine()) != null) {
                    json += output;
                }
            }
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
            int count = jsonObject.getInt("count");
            if (count > 0) {
                haveCorpus = true;
            }
            return count;
        } catch (Exception e) {
            System.err.println(e + " " + jsonText );
            return -1;
        }
    }    
    
    
/*
    private int getCountOfResourcesFromHttps(String uri) {
        String output;
        String json = "";
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (KeyManagementException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

        // récupération du total des notices
        try {
            URL url = new URL(uri);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(2000); 
            conn.setReadTimeout(2000);
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
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex);
        }
        return -1;
    }
    */    
    
}
