/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.ws.api;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.dao.DaoResourceHelper;
import fr.cnrs.opentheso.bdd.helper.dao.NodeConceptGraph;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import java.util.ArrayList;
import java.util.List;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author miledrousset
 */
public class D3jsHelper {

    private int count = 0;
    private NodePreference nodePreference;

    public String findDatasForGraph__(HikariDataSource ds, String idConcept, String idTheso, String idLang) {

        if(idTheso == null || idTheso.isEmpty()) {
            return null;
        }
    /*    if(idConcept == null || idConcept.isEmpty()) {
            return null;
        }*/
        if(idLang == null || idLang.isEmpty()) {
            return null;
        }
        nodePreference = new PreferencesHelper().getThesaurusPreferences(ds, idTheso);
        if (nodePreference == null) {
            return null;
        }

        DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
        
        List<NodeConceptGraph> nodeConceptGraphs_childs;
        
        // cas où on affiche tout le thésaurus
        if(StringUtils.isEmpty(idConcept)){
            nodeConceptGraphs_childs = daoResourceHelper.getConceptsTTForGraph(ds, idTheso, idLang);
        } else {
            nodeConceptGraphs_childs = daoResourceHelper.getConceptsNTForGraph(ds, idTheso, idConcept, idLang);
        }
        
        if(nodeConceptGraphs_childs == null || nodeConceptGraphs_childs.isEmpty())
            return null;

        /// limitation des frères à 2000
        if(nodeConceptGraphs_childs.size() > 2000) {
            nodeConceptGraphs_childs = nodeConceptGraphs_childs.subList(0, 2001);
        }
        
        NodeJsonD3js nodeJsonD3js = new NodeJsonD3js();
    
        nodeJsonD3js.setNodeDatas(getRootNode(ds, idTheso, idLang, idConcept, nodeConceptGraphs_childs));            

        return getJsonFromNodeJsonD3js(nodeJsonD3js);
    }

    private NodeDatas getRootNode(HikariDataSource ds, String idTheso, String idLang,
            String idTopConcept,  List<NodeConceptGraph> nodeConceptGraphs_childs){
        
        NodeDatas nodeDatas;
        if(StringUtils.isEmpty(idTopConcept)){
            nodeDatas = getTopNodeDatasForTheso(ds, idTheso, idLang);
        } else {
            nodeDatas = getTopNodeDatas(ds, idTopConcept, idTheso, idLang);
        }

        //Children
        List<NodeDatas> childrens = new ArrayList<>();

        // pour limiter les noeuds à 3000, sinon, c'est invisible sur le graphe
        count = 0;

        // boucle récursive pour récupérer les fils
        for (NodeConceptGraph nodeConceptGraph : nodeConceptGraphs_childs) {
            childrens.add(getNode(ds, nodeConceptGraph, idTheso, idLang));
        }
        nodeDatas.setChildrens(childrens);
        return nodeDatas;
    }

    private NodeDatas getNode(HikariDataSource ds, NodeConceptGraph nodeConceptGraph, String idTheso, String idLang){
        NodeDatas nodeDatas = getNodeDatas(nodeConceptGraph);
        DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
        count++;
        //Children
        List<NodeDatas> childrens = new ArrayList<>();
        if(count < 3000) {
            List<NodeConceptGraph> nodeConceptGraphs_childs = daoResourceHelper.getConceptsNTForGraph(ds, idTheso, nodeConceptGraph.getIdConcept(), idLang);
            
            if(nodeConceptGraphs_childs != null && !nodeConceptGraphs_childs.isEmpty()) {            
                /// limitation des frères à 2000
                if(nodeConceptGraphs_childs.size() > 2000) {
                    nodeConceptGraphs_childs = nodeConceptGraphs_childs.subList(0, 2001);
                }            
                for (NodeConceptGraph nodeConceptGraph1 : nodeConceptGraphs_childs) {
                    childrens.add(getNode(ds, nodeConceptGraph1, idTheso, idLang));
                    count++;
                    nodeDatas.setChildrens(childrens);
                }
            }
        }
        return nodeDatas;
    }

    private String getJsonFromNodeJsonD3js(NodeJsonD3js nodeJsonD3js) {

        JsonObjectBuilder nodeRoot = Json.createObjectBuilder();
        nodeRoot.add("name", nodeJsonD3js.getNodeDatas().getName());
        nodeRoot.add("type", "type1");
        nodeRoot.add("url", nodeJsonD3js.getNodeDatas().getUrl());
        if(nodeJsonD3js.getNodeDatas().getDefinition() == null){
            nodeRoot.add("definition", "");
        } else {
            nodeRoot.add("definition", nodeJsonD3js.getNodeDatas().getDefinition().toString());
        }

        if(nodeJsonD3js.getNodeDatas().getSynonym() == null){
            nodeRoot.add("synonym", "");
        } else {
            JsonArrayBuilder jsonArrayBuilderSynonyms = Json.createArrayBuilder();
            for (String synonym : nodeJsonD3js.getNodeDatas().getSynonym()) {
                jsonArrayBuilderSynonyms.add(synonym);
            }
            nodeRoot.add("synonym", jsonArrayBuilderSynonyms.build());            
        }
        
        JsonArrayBuilder jsonArrayBuilderChilds = Json.createArrayBuilder();

        for (NodeDatas nodeData : nodeJsonD3js.getNodeDatas().getChildrens()) {
            jsonArrayBuilderChilds.add(getChild(nodeData).build());
        }

        nodeRoot.add("children", jsonArrayBuilderChilds.build());

        return nodeRoot.build().toString();
    }


    private JsonObjectBuilder getChild(NodeDatas nodeData) {

        JsonObjectBuilder nodeChild = Json.createObjectBuilder();
        nodeChild.add("name", nodeData.getName());
        nodeChild.add("type", nodeData.getType());
        nodeChild.add("url", nodeData.getUrl());

        // altLabels
        JsonArrayBuilder jsonArrayBuilderSynonyms = Json.createArrayBuilder();
        if(nodeData.getSynonym() == null){
            nodeChild.add("synonym", jsonArrayBuilderSynonyms.build());
        } else {
            for (String synonym : nodeData.getSynonym()) {
                jsonArrayBuilderSynonyms.add(synonym);
            }
            nodeChild.add("synonym", jsonArrayBuilderSynonyms.build());
        }
      
        // définition
        JsonArrayBuilder jsonArrayBuilderDefinition = Json.createArrayBuilder();
        if(nodeData.getDefinition() == null){
            nodeChild.add("definition", jsonArrayBuilderSynonyms.build());
        } else {
            for (String definition : nodeData.getDefinition()) {
                jsonArrayBuilderDefinition.add(definition);
            }
            nodeChild.add("definition", jsonArrayBuilderDefinition.build());              
        }
        
        
        JsonArrayBuilder jsonArrayBuilderChilds = Json.createArrayBuilder();
        if(nodeData.getChildrens() != null && !nodeData.getChildrens().isEmpty()){
            for (NodeDatas nodeDataChild : nodeData.getChildrens()) {
                jsonArrayBuilderChilds.add(getChild(nodeDataChild).build());
            }
        }
        nodeChild.add("children", jsonArrayBuilderChilds.build());

        return nodeChild;
    }


    private NodeDatas getNodeDatas(NodeConceptGraph nodeConceptGraph){
        NodeDatas nodeDatas = new NodeDatas();
        
        if(StringUtils.isEmpty(nodeConceptGraph.getPrefLabel())) {
            nodeDatas.setName("(" + nodeConceptGraph.getIdConcept() + ")");
        } else 
            nodeDatas.setName(nodeConceptGraph.getPrefLabel());

        nodeDatas.setUrl(nodeConceptGraph.getUri());
        nodeDatas.setDefinition(nodeConceptGraph.getDefinitions());
        nodeDatas.setSynonym(nodeConceptGraph.getAltLabels());
        
        if(nodeConceptGraph.isHaveChildren()){
            nodeDatas.setType("type2");
        } else
            nodeDatas.setType("type3");
        return nodeDatas;
    }    


    private NodeDatas getTopNodeDatas(HikariDataSource ds,
                                   String idConcept, String idTheso, String idLang){
        ConceptHelper conceptHelper = new ConceptHelper();
        conceptHelper.setNodePreference(nodePreference);

        NodeDatas nodeDatas = conceptHelper.getConceptForGraph(ds, idConcept, idTheso, idLang);
        if(conceptHelper.haveChildren(ds, idTheso, idConcept)) {
            nodeDatas.setType("type2");
        } else
            nodeDatas.setType("type3");
        return nodeDatas;
    }
    
    private NodeDatas getTopNodeDatasForTheso(HikariDataSource ds,
                                   String idTheso, String idLang){
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();
        String title = thesaurusHelper.getTitleOfThesaurus(ds, idTheso, idLang);
        NodeDatas nodeDatas = new NodeDatas();
        
        nodeDatas.setType("type2");
        nodeDatas.setName(title);
        nodeDatas.setUrl(getUri(idTheso));
        nodeDatas.setDefinition(null);
        return nodeDatas;
    }    

    private String getUri(String idTheso) {
        if (idTheso == null) {
            return "";
        }
        return nodePreference.getCheminSite() + "?idt=" + idTheso;
    }    
}