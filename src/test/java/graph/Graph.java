package graph;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PathHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.dao.ConceptIdLabel;
import fr.cnrs.opentheso.bdd.helper.dao.ConceptLabel;
import fr.cnrs.opentheso.bdd.helper.dao.ConceptRelation;
import fr.cnrs.opentheso.bdd.helper.dao.NodeFullConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.core.exports.UriHelper;
import fr.cnrs.opentheso.skosapi.SKOSProperty;

import fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph.Node;
import fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph.NodeGraphD3js;


import fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph.Properties;
import fr.cnrs.opentheso.ws.openapi.helper.d3jsgraph.Relationship;
import java.util.ArrayList;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class Graph {

    public Graph() {
    }

    private NodeGraphD3js nodeGraphD3js;
    private String defaultLang;
    private NodePreference nodePreference;
    private UriHelper uriHelper;
    
    @Test
    public void getDataForGraphD3jsConcept() {
        String idTheso = "th3";
        String idConcept = "4";
        String idLang = "fr";
        defaultLang = idLang;
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();
        
        initGraph();
        
        getGraphByConcept(ds, idTheso, idConcept, idLang);
        
//        idTheso = "th12";
//        idConcept = "4";
//        idLang = "fr";
//        getGraphByConcept(ds, idTheso, idConcept, idLang);
        
        String json = getJsonFromNodeGraphD3js();
    }
    
    @Test
    public void getDataForGraphD3jsTheso() {
        String idTheso = "th15";
        String idLang = "fr";
        defaultLang = idLang;
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();
        
        initGraph();
        getGraphByTheso(ds, idTheso, idLang);
        idTheso = "th14";
        idLang = "fr";        
        getGraphByTheso(ds, idTheso, idLang);        
        
        String json = getJsonFromNodeGraphD3js();
    }    
    
    
    private void initGraph(){
        nodeGraphD3js = new NodeGraphD3js();
        nodeGraphD3js.setNodes(new ArrayList<>());
        nodeGraphD3js.setRelationships(new ArrayList<>()); 
    }
    
    private void getGraphByTheso(HikariDataSource ds, String idTheso, String idLang){
        ConceptHelper conceptHelper = new ConceptHelper();
        nodePreference = new PreferencesHelper().getThesaurusPreferences(ds, idTheso);
        uriHelper = new UriHelper(ds, nodePreference, idTheso);
        
        // récupérer les conceptScheme
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        NodeThesaurus nodeThesaurus = thesaurusHelper.getNodeThesaurus(ds, idTheso);
        nodeGraphD3js.addNewNode(getDatasOfThesaurus(nodeThesaurus));
        
        ArrayList<NodeUri> nodeTTs = conceptHelper.getAllTopConcepts(ds, idTheso);
        nodeGraphD3js.getRelationships().addAll(getRelationshipOfTheso(nodeTTs, idTheso));        
        
        /// récupérer les concepts
        List<String> listIdConcept = conceptHelper.getAllIdConceptOfThesaurus(ds, idTheso);
        for (String idC : listIdConcept) {
            NodeFullConcept nodeFullConcept = conceptHelper.getConcept2(ds, idC, idTheso, idLang, -1, -1); 
            nodeGraphD3js.addNewNode(getDatasOfNode(nodeFullConcept, idTheso));
            nodeGraphD3js.getRelationships().addAll(getRelationship(nodeFullConcept, idTheso));
        }
        

    }     
    
    
    private void getGraphByConcept(HikariDataSource ds, String idTheso, String idConcept,
                        String idLang){
        ConceptHelper conceptHelper = new ConceptHelper();
        
        // récupérer le noeud du thésaurus
        // récupérer les conceptScheme
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        NodeThesaurus nodeThesaurus = thesaurusHelper.getNodeThesaurus(ds, idTheso);
        nodeGraphD3js.addNewNode(getDatasOfThesaurus(nodeThesaurus));
        
        /// récupérer les concepts
        List<String> listIdConcept = conceptHelper.getIdsOfBranch2(ds, idTheso, idConcept);
        for (String idC : listIdConcept) {
            NodeFullConcept nodeFullConcept = conceptHelper.getConcept2(ds, idC, idTheso, idLang, -1, -1); 
            nodeGraphD3js.addNewNode(getDatasOfNode(nodeFullConcept, idTheso));
            nodeGraphD3js.getRelationships().addAll(getRelationship(nodeFullConcept, idTheso));
        }
    }    
    
    
    
    private Node getDatasOfThesaurus(NodeThesaurus nodeThesaurus){
        Node node = new Node();
        
        node.setId(uriHelper.getUriForTheso(nodeThesaurus.getIdThesaurus(), "", ""));
        List<String> labels = new ArrayList<>();
        labels.add("Resource");
        labels.add("skos__ConceptScheme");
        node.setLabels(labels);
        
        Properties properties = new Properties();
        properties.setUri(uriHelper.getUriForTheso(nodeThesaurus.getIdThesaurus(), "", ""));
        properties.setPropertiesLabel("skos__prefLabel");

        List<String> prefLabels = new ArrayList<>();
        for (Thesaurus thesaurus : nodeThesaurus.getListThesaurusTraduction()) {
            prefLabels.add(thesaurus.getTitle() + "@" + thesaurus.getLanguage());
        }
        properties.setPrefLabels(prefLabels);          
        node.setProperties(properties);
        return node;
    }    
    private List<Relationship> getRelationshipOfTheso(ArrayList<NodeUri> nodeUri, String idTheso){
        List<Relationship> relationships = new ArrayList<>();
        for (NodeUri nodeUri1 : nodeUri) {
            Relationship relationship = new Relationship();

            relationship.setStart(uriHelper.getUriForTheso(idTheso, "", ""));
            relationship.setEnd(uriHelper.getUriForConcept(nodeUri1.getIdConcept(), nodeUri1.getIdArk(), null));
            relationship.setRelation("skos__hasTopConcept");
            relationships.add(relationship);            
        }
        return relationships;
    }    
    
    
    
    
    
    
    
    
    
    

    
    private Node getDatasOfCollection(ConceptIdLabel conceptIdLabel){
        Node node = new Node();
        
        node.setId(conceptIdLabel.getUri());
        List<String> labels = new ArrayList<>();
        labels.add("Resource");
        labels.add("skos__Collection");
        node.setLabels(labels);
        
        Properties properties = new Properties();
        properties.setUri(conceptIdLabel.getUri());
        properties.setPropertiesLabel("skos__prefLabel");

        List<String> prefLabels = new ArrayList<>();
        prefLabels.add(conceptIdLabel.getLabel() + "@" + defaultLang);
        properties.setPrefLabels(prefLabels);        
        
        node.setProperties(properties);
        return node;
    }
    
    /**
     * Datas for external links
    {
      id: "http://wikidata/...."
      labels: ["Resource"]
      properties: {
        uri: "http://wikidata/..."
      }
     */
    private Node getDatasOfExternalLink(String id){
        Node node = new Node();
        
        node.setId(id);
        node.setLabels(getNodeLabel(null));
        node.setProperties(getPropertiesOfExternalLink(id));
        return node;
    }
    private Properties getPropertiesOfExternalLink(String id){
        Properties properties = new Properties();
        properties.setUri(id);
        return properties;
    } 
    
    
    /**
     * Datas for Concepts
     * @param nodeFullConcept
     * @param idTheso
     * @return 
     */
    private Node getDatasOfNode(NodeFullConcept nodeFullConcept, String idTheso){
        Node node = new Node();
        node.setId(nodeFullConcept.getUri());//idTheso + "." + nodeFullConcept.getIdentifier());
        node.setLabels(getNodeLabel(nodeFullConcept));
        node.setProperties(getPropertiesOfNode(nodeFullConcept));
        return node;
    }
    
    
    private Properties getPropertiesOfNode(NodeFullConcept nodeFullConcept){
        Properties properties = new Properties();
        properties.setUri(nodeFullConcept.getUri());
        properties.setPropertiesLabel("skos__prefLabel");

        List<String> prefLabels = new ArrayList<>();
        prefLabels.add(nodeFullConcept.getPrefLabel().getLabel() + "@" + nodeFullConcept.getPrefLabel().getIdLang());
        if(nodeFullConcept.getPrefLabelsTraduction() != null){
            for (ConceptLabel conceptLabel : nodeFullConcept.getPrefLabelsTraduction()) {
                prefLabels.add(conceptLabel.getLabel() + "@" + conceptLabel.getIdLang());
            }
        }
        properties.setPrefLabels(prefLabels);
        return properties;
    }       
    private List<String> getNodeLabel(NodeFullConcept nodeFullConcept){
        List<String> labels = new ArrayList<>();
        labels.add("Resource");
        if(nodeFullConcept != null){
            switch (nodeFullConcept.getResourceType()) {
                case SKOSProperty.CONCEPT:
                    labels.add("skos__Concept");
                    break;
                case SKOSProperty.COLLECTION:
                    labels.add("skos__Collection");
                    break;        
                case SKOSProperty.CONCEPT_SCHEME:
                    labels.add("skos__ConceptScheme");
                    break;           
                default:
                    break;
            }
        }
        return labels;
    }    
    
    
    
    
    
    private List<Relationship> getRelationship(NodeFullConcept nodeFullConcept, String idTheso){
        List<Relationship> relationships = new ArrayList<>();
        if(nodeFullConcept.getNarrowers() != null){
            for (ConceptRelation narrower : nodeFullConcept.getNarrowers()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(narrower.getUri());
                relationship.setRelation("skos__narrower");
                relationships.add(relationship);
            }
        }
        if(nodeFullConcept.getBroaders() != null){
            for (ConceptRelation broader : nodeFullConcept.getBroaders()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(broader.getUri());
                relationship.setRelation("skos__broader");
                relationships.add(relationship);
            }
        }
        
        if(nodeFullConcept.getRelateds()!= null){
            for (ConceptRelation related : nodeFullConcept.getRelateds()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(related.getUri());
                relationship.setRelation("skos__related");
                relationships.add(relationship);
            }
        }
        if(nodeFullConcept.getReplacedBy() != null){
            for (ConceptIdLabel conceptIdLabel : nodeFullConcept.getReplacedBy()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(conceptIdLabel.getUri());
                relationship.setRelation("ns0__isReplacedBy");
                relationships.add(relationship);
            }
        }     
        if(nodeFullConcept.getReplaces()!= null){
            for (ConceptIdLabel conceptIdLabel : nodeFullConcept.getReplaces()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(conceptIdLabel.getUri());
                relationship.setRelation("ns0__replace");
                relationships.add(relationship);
            }
        }
        if(nodeFullConcept.getMembres()!= null){
            for (ConceptIdLabel conceptIdLabel : nodeFullConcept.getMembres()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(conceptIdLabel.getUri());
                relationship.setRelation("ns2__memberOf");
                relationships.add(relationship);
                Node node = getDatasOfCollection(conceptIdLabel);
                nodeGraphD3js.addNewNode(node);
            }
        }           
        
        
        if(nodeFullConcept.getExactMatchs()!= null){
            for (String exactMatch : nodeFullConcept.getExactMatchs()) {
                Relationship relationship = new Relationship();
                
                relationship.setStart(nodeFullConcept.getUri());
                relationship.setEnd(exactMatch);
                relationship.setRelation("skos__exactMatch");
                relationships.add(relationship);
                nodeGraphD3js.addNewNode(getDatasOfExternalLink(exactMatch));
            }
        }  
        
        Relationship relationship = new Relationship();

        relationship.setStart(nodeFullConcept.getUri());
        relationship.setEnd(uriHelper.getUriForTheso(idTheso, "", ""));
        relationship.setRelation("skos__inScheme");
        relationships.add(relationship);

        return relationships;
    }
    
    
    
    
    
    
    private String getJsonFromNodeGraphD3js(){
        
        JsonObjectBuilder nodeRoot = Json.createObjectBuilder();
        

        
        JsonArrayBuilder jsonArrayNodes = Json.createArrayBuilder();
        
        
        for (Node node : nodeGraphD3js.getNodes()) {
            JsonObjectBuilder nodeDatas = Json.createObjectBuilder();
            
            // id
            nodeDatas.add("id", node.getId());
            
            // label of node
            JsonArrayBuilder nodeLables = Json.createArrayBuilder();
            for (String label : node.getLabels()) {
                nodeLables.add(label);
            }
            nodeDatas.add("labels", nodeLables.build());  
            
            // properties // prefLabel
            JsonObjectBuilder nodeProperties = Json.createObjectBuilder();
            JsonArrayBuilder jsonArrayPrefLabels = Json.createArrayBuilder();
            
            if(node.getProperties().getPrefLabels() != null){
                for (String prefLabel : node.getProperties().getPrefLabels()) {
                    jsonArrayPrefLabels.add(prefLabel);
                }
                nodeProperties.add(node.getProperties().getPropertiesLabel(), jsonArrayPrefLabels.build());
            }
            nodeProperties.add("uri", node.getProperties().getUri());
            
            nodeDatas.add("properties", nodeProperties.build());

            jsonArrayNodes.add(nodeDatas.build());
        }
    
        // add nodes
        nodeRoot.add("nodes", jsonArrayNodes.build());        
        
        
        
        // add relationships
        JsonArrayBuilder jsonArrayRelationships = Json.createArrayBuilder();
        
        for (Relationship relationship : nodeGraphD3js.getRelationships()) {
             JsonObjectBuilder nodeRelation = Json.createObjectBuilder();
             nodeRelation.add("start", relationship.getStart());
             nodeRelation.add("end", relationship.getEnd());
             nodeRelation.add("label", relationship.getRelation());
             jsonArrayRelationships.add(nodeRelation.build());
        }
        nodeRoot.add("relationships", jsonArrayRelationships.build());        
        return nodeRoot.build().toString();
    }         
    
    



    
    
    @Test
    public void getChildren() {
        String idTheso = "th5";
        String idConcept = "31";

        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();

        PathHelper pathHelper = new PathHelper();

        List<String> paths = pathHelper.getGraphOfConcept(ds, idConcept, idTheso);
        for (String path : paths) {
            System.out.println(path);
        }

        List<List<String>> allPaths = pathHelper.getPathFromGraph(paths);
        for (List path : allPaths) {
            System.out.println(path.toString());
        }
    }



}

