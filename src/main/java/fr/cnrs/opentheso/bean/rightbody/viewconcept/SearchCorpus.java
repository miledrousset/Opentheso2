package fr.cnrs.opentheso.bean.rightbody.viewconcept;

import fr.cnrs.opentheso.bdd.helper.nodes.NodeCorpus;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SearchCorpus implements Callable<NodeCorpus> {

    private NodeCorpus nodeCorpus;
    private NodeConcept nodeConcept;

    public SearchCorpus(NodeCorpus nodeCorpus, NodeConcept nodeConcept) {
        this.nodeCorpus = nodeCorpus;
        this.nodeConcept = nodeConcept;
    }

    @Override
    public NodeCorpus call() throws Exception {
        // cas où on compose uniquement une URL de lien vers les notices
        if (nodeCorpus.isIsOnlyUriLink()) {
            if (nodeCorpus.getUriLink().contains("##id##")) {
                nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##id##", nodeConcept.getConcept().getIdConcept()));
            }
            if (nodeCorpus.getUriLink().contains("##value##")) {
                nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##value##", nodeConcept.getTerm().getLexical_value()));
            }
        } else {
            // recherche par Id
            /// pour le count par Id interne
            if (nodeCorpus.getUriCount().contains("##id##")) {
                if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                    nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##id##", nodeConcept.getConcept().getIdConcept()));
                }
            }
            /// pour le count par Id ark
            if (nodeCorpus.getUriCount().contains("##arkid##")) {
                if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                    nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##arkid##", nodeConcept.getConcept().getIdArk()));
                }
            }

            /// pour la construction de l'URL avec Id interne
            if (nodeCorpus.getUriLink().contains("##id##")) {
                nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##id##", nodeConcept.getConcept().getIdConcept()));
            }
            /// pour la construction de l'URL avec Id Ark
            if (nodeCorpus.getUriLink().contains("##arkid##")) {
                nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##arkid##", nodeConcept.getConcept().getIdArk()));
            }

            // recherche par value
            if (nodeCorpus.getUriCount().contains("##value##")) {
                if (nodeCorpus.getUriCount() != null && !nodeCorpus.getUriCount().isEmpty()) {
                    nodeCorpus.setUriCount(nodeCorpus.getUriCount().replace("##value##", nodeConcept.getTerm().getLexical_value()));
                }
            }
            if (nodeCorpus.getUriLink().contains("##value##")) {
                nodeCorpus.setUriLink(nodeCorpus.getUriLink().replace("##value##", nodeConcept.getTerm().getLexical_value()));
            }
            setCorpusCount(nodeCorpus);
        }

        return nodeCorpus;
    }

    private void setCorpusCount(NodeCorpus nodeCorpus) {
        if (nodeConcept != null) {
            if (nodeCorpus == null) {
                return;
            }
            nodeCorpus.setCount(getCountOfResourcesFromHttp(nodeCorpus.getUriCount()));
        }
    }

    private int getCountFromJson(String jsonText) {
        if (jsonText == null) {
            return -1;
        }

        try {
            JsonReader reader = Json.createReader(new StringReader(jsonText));
            JsonObject jsonObject = reader.readObject();
            return jsonObject.getInt("count");
        } catch (Exception e) {
            System.err.println(e + " " + jsonText + " " + nodeConcept.getConcept().getIdConcept());
            return -1;
        }
    }

    private int getCountOfResourcesFromHttp(String uri) {

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
            int status = conn.getResponseCode();
            if (status != 200) {
                return -1;
            }
            InputStream in = status >= 400 ? conn.getErrorStream() : conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String json = "";
            String output;
            while ((output = br.readLine()) != null) {
                json += output;
            }
            br.close();
            return getCountFromJson(json);
        } catch (Exception ex) {
            Logger.getLogger(ConceptView.class.getName()).log(Level.SEVERE, null, ex + " " + uri);
            return -1;
        }
    }
}
