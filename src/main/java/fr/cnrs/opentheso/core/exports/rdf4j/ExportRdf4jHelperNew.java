package fr.cnrs.opentheso.core.exports.rdf4j;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.datas.DcElement;

import fr.cnrs.opentheso.bdd.datas.Thesaurus;
import fr.cnrs.opentheso.bdd.helper.*;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeAlignmentSmall;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeFacet;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeGps;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeHieraRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeImage;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConceptExport;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupLabel;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroupTraductions;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.status.NodeStatus;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import fr.cnrs.opentheso.bdd.helper.nodes.thesaurus.NodeThesaurus;
import fr.cnrs.opentheso.bean.candidat.dto.MessageDto;
import fr.cnrs.opentheso.bean.candidat.dto.VoteDto;
import fr.cnrs.opentheso.skosapi.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

/**
 *
 * @author MiledRousset
 */
public class ExportRdf4jHelperNew {

    private NodePreference nodePreference;
    private SKOSXmlDocument skosXmlDocument;

    private ArrayList<NodeUri> nodeTTs;
   
    private String messages;
    
    // pour gérer les group/sousGroups
    private HashMap<String, String> superGroupHashMap;    

    public ExportRdf4jHelperNew() {
        skosXmlDocument = new SKOSXmlDocument();
        superGroupHashMap = new HashMap();
    }

    public boolean setInfos(NodePreference nodePreference) {
        this.nodePreference = nodePreference;
        messages = "";
        return true;
    }
    
    /**
     * fonction qui permet de récuperer les concepts avec les labels pour les relations RT BT NT
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param idLang 
     * @param showLabels 
     */
    public void addSignleConceptByLang(HikariDataSource ds, String idTheso, String idConcept, String idLang, boolean showLabels) {

        SKOSResource sKOSResource = new SKOSResource();
        NodeConceptExport nodeConcept = new ConceptHelper().getConceptForExport(ds, idConcept, idTheso,
                false, false);

        if (nodeConcept == null) {
            return;
        }

        sKOSResource.setUri(getUri(nodeConcept));
        sKOSResource.setProperty(SKOSProperty.CONCEPT);

        // prefLabel
        for (NodeTermTraduction traduction : nodeConcept.getNodeTermTraductions()) {
            if(traduction.getLang().equalsIgnoreCase(idLang))
                sKOSResource.addLabel(traduction.getLexicalValue(), traduction.getLang(), SKOSProperty.PREF_LABEL);
        }

        // altLabel
        for (NodeEM nodeEM : nodeConcept.getNodeEM()) {
            if(nodeEM.getLang().equalsIgnoreCase(idLang)) {
                if(nodeEM.isHiden())
                    sKOSResource.addLabel(nodeEM.getLexical_value(), nodeEM.getLang(), SKOSProperty.HIDDEN_LABEL);
                else
                    sKOSResource.addLabel(nodeEM.getLexical_value(), nodeEM.getLang(), SKOSProperty.ALT_LABEL);   
            }
        }
        ArrayList<NodeNote> nodeNotes = new ArrayList<>();
        for (NodeNote nodeNote : nodeConcept.getNodeNotes()) {
            if(nodeNote.getLang().equalsIgnoreCase(idLang))
                nodeNotes.add(nodeNote);
        }
/*        for (NodeNote nodeNote : nodeConcept.getNodeNoteTerm()) {
            if(nodeNote.getLang().equalsIgnoreCase(idLang))
                nodeNotes.add(nodeNote);
        }        */
        addNoteGiven(nodeNotes, sKOSResource);
        addGPSGiven(nodeConcept.getNodeGps(), sKOSResource);
        addAlignementGiven(nodeConcept.getNodeAlignmentsList(), sKOSResource);
        
        if(!showLabels) {
            addRelationGiven(nodeConcept.getNodeListOfBT(), nodeConcept.getNodeListOfNT(),
                nodeConcept.getNodeListIdsOfRT(), sKOSResource, nodeConcept.getConcept().getIdThesaurus());
        }
        String notation = nodeConcept.getConcept().getNotation();
        String created = nodeConcept.getConcept().getCreated().toString();
        String modified = nodeConcept.getConcept().getModified().toString();

        if (notation != null && !notation.equals("null")) {
            sKOSResource.addNotation(notation);
        }
        if (created != null) {
            sKOSResource.addDate(created, SKOSProperty.CREATED);
        }
        if (modified != null) {
            sKOSResource.addDate(modified, SKOSProperty.MODIFIED);
        }
        sKOSResource.addRelation(idTheso, getUriFromId(idTheso), SKOSProperty.INSCHEME);
        for (NodeUri nodeUri : nodeConcept.getNodeListIdsOfConceptGroup()) {
            sKOSResource.addRelation(nodeUri.getIdConcept(), getUriGroupFromNodeUri(nodeUri,idTheso), SKOSProperty.MEMBER_OF);
        }           
        sKOSResource.addIdentifier(idConcept, SKOSProperty.IDENTIFIER);
        

    /*    ArrayList<String> first = new ArrayList<>();
        first.add(idConcept);
        ArrayList<ArrayList<String>> paths = new ArrayList<>();
            
        paths = new ConceptHelper().getPathOfConceptWithoutGroup(ds, idConcept, idTheso, first, paths);
        ArrayList<String> pathFromArray = getPathFromArray(paths);
        if(!pathFromArray.isEmpty())
            sKOSResource.setPaths(pathFromArray);*/
        skosXmlDocument.addconcept(sKOSResource);
    }        
    
    public void addSingleGroup(HikariDataSource ds, String idThesaurus, String idGroup) {

        NodeGroupLabel nodeGroupLabel;
        nodeGroupLabel = new GroupHelper().getNodeGroupLabel(ds, idGroup, idThesaurus);
        SKOSResource sKOSResource = new SKOSResource();
        sKOSResource.setUri(getUriFromGroup(nodeGroupLabel));
        sKOSResource.setProperty(SKOSProperty.CONCEPT_GROUP);

        for (NodeGroupTraductions traduction : nodeGroupLabel.getNodeGroupTraductionses()) {
            sKOSResource.addLabel(traduction.getTitle(), traduction.getIdLang(), SKOSProperty.PREF_LABEL);
            //dates
            String created;
            String modified;
            created = traduction.getCreated().toString();
            modified = traduction.getModified().toString();
            if (created != null) {
                sKOSResource.addDate(created, SKOSProperty.CREATED);
            }
            if (modified != null) {
                sKOSResource.addDate(modified, SKOSProperty.MODIFIED);
            }
        }
        
        // pour exporter les membres (tous les concepts du group

        ArrayList<String> childURI = new GroupHelper().getListGroupChildIdOfGroup(ds, idGroup, idThesaurus);
        HashMap<String, String> superGroupHashMapTemp = new HashMap();
        for (String id : childURI) {
            sKOSResource.addRelation(id, getUriFromId(id), SKOSProperty.SUBGROUP);
            superGroupHashMapTemp.put(id, idGroup);
        }
        String idSuperGroup = superGroupHashMapTemp.get(idGroup);

        if (idSuperGroup != null) {
            sKOSResource.addRelation(idSuperGroup, getUriFromId(idSuperGroup), SKOSProperty.SUPERGROUP);
            superGroupHashMapTemp.remove(idGroup);
        }
        sKOSResource.addIdentifier(idGroup, SKOSProperty.IDENTIFIER);
        skosXmlDocument.addGroup(sKOSResource);
    }     
    
    /**
     * permet de récupérer les informations du thésaurus et les TopConcept pour
     * construire SKOSResource #MR
     *
     * @param ds
     * @param idTheso
     * @param nodePreference 
     */
    public void exportTheso(HikariDataSource ds, String idTheso, NodePreference nodePreference) {
        this.nodePreference = nodePreference;
        NodeThesaurus nodeThesaurus = new ThesaurusHelper().getNodeThesaurus(ds, idTheso);
        
        SKOSResource conceptScheme = new SKOSResource(getUriFromId(nodeThesaurus.getIdThesaurus()), SKOSProperty.CONCEPT_SCHEME);
        
        for (Thesaurus thesaurus : nodeThesaurus.getListThesaurusTraduction()) {
            
            if (thesaurus.getCreator() != null && !thesaurus.getCreator().equalsIgnoreCase("null")) {
                conceptScheme.addAgent(thesaurus.getCreator(), SKOSProperty.CREATOR);
            }
            if (thesaurus.getContributor() != null && !thesaurus.getContributor().equalsIgnoreCase("null")) {
                conceptScheme.addAgent(thesaurus.getContributor(), SKOSProperty.CONTRIBUTOR);
            }
            if (thesaurus.getTitle() != null && thesaurus.getLanguage() != null) {
                conceptScheme.addLabel(thesaurus.getTitle(), 
                        thesaurus.getLanguage(), SKOSProperty.PREF_LABEL);
            }

            //dates
            if (thesaurus.getCreated().toString() != null) {
                conceptScheme.addDate(thesaurus.getCreated().toString(), SKOSProperty.CREATED);
            }
            if (thesaurus.getModified().toString() != null) {
                conceptScheme.addDate(thesaurus.getModified().toString(), SKOSProperty.MODIFIED);
            }
            conceptScheme.setThesaurus(thesaurus);
        }
        
        /// ajout des DCMI
        conceptScheme.getThesaurus().setDcElement(new DcElementHelper().getDcElementOfThesaurus(ds, idTheso));        

        //liste top concept
        ConceptHelper conceptHelper = new ConceptHelper();
        nodeTTs = conceptHelper.getAllTopConcepts(ds, idTheso);

        nodeTTs.forEach((nodeTT) -> {
            conceptScheme.addRelation(nodeTT.getIdConcept(), getUriFromNodeUri(nodeTT, idTheso), SKOSProperty.HAS_TOP_CONCEPT);
        });
        skosXmlDocument.setConceptScheme(conceptScheme);
    }
    
    public void exportSelectedCollections(HikariDataSource ds, String idTheso, List<String> selectedGroups){
        GroupHelper groupHelper = new GroupHelper();
        NodeGroupLabel nodeGroupLabel;
        for (String idGroup : selectedGroups) {
            nodeGroupLabel = groupHelper.getNodeGroupLabel(ds, idGroup, idTheso);
            SKOSResource sKOSResource = new SKOSResource(getUriFromGroup(nodeGroupLabel), SKOSProperty.CONCEPT_GROUP);
            sKOSResource.addRelation(nodeGroupLabel.getIdGroup(), getUriFromGroup(nodeGroupLabel), SKOSProperty.MICROTHESAURUS_OF);
            addChildsGroupRecursive(ds, idTheso, idGroup, sKOSResource);
        }
    }

    public void exportFacettes(HikariDataSource ds, String idTheso){
        FacetHelper facetHelper = new FacetHelper();
        
        ArrayList<NodeFacet> facets = facetHelper.getAllFacetsDetailsOfThesaurus(ds, idTheso);
        
        for (NodeFacet facet : facets) {
            SKOSResource sKOSResource =  new SKOSResource(getUriForFacette(facet.getIdFacet(), idTheso), SKOSProperty.FACET);
            sKOSResource.addRelation(facet.getIdFacet(), getUriFromNodeUri(facet.getNodeUri(), idTheso), SKOSProperty.SUPER_ORDINATE);
            sKOSResource.addLabel(facet.getLexicalValue(), facet.getLang(), SKOSProperty.PREF_LABEL);
            sKOSResource.addDate(facet.getCreated(), SKOSProperty.CREATED);
            sKOSResource.addDate(facet.getModified(), SKOSProperty.MODIFIED);
            addFacetMembers(ds, facetHelper, sKOSResource, facet, idTheso);
            skosXmlDocument.addFacet(sKOSResource);
        }
    }
    
    private void addFacetMembers(HikariDataSource ds, FacetHelper facetHelper, SKOSResource sKOSResource,
            NodeFacet nodeFacet, String idTheso){

        List<String> members = facetHelper.getAllMembersOfFacet(ds, nodeFacet.getIdFacet(), idTheso);
        for (String idConcept : members) {
            NodeUri nodeUri = new ConceptHelper().getNodeUriOfConcept(ds, idConcept, idTheso);
            sKOSResource.addRelation(nodeUri.getIdConcept(), getUriFromNodeUri(nodeUri, idTheso), SKOSProperty.MEMBER);
        }
    }

    public void exportThisCollection(HikariDataSource ds, String idTheso, String idGroup){
        GroupHelper groupHelper = new GroupHelper();
        NodeGroupLabel nodeGroupLabel;
        nodeGroupLabel = groupHelper.getNodeGroupLabel(ds, idGroup, idTheso);
        SKOSResource sKOSResource = new SKOSResource(getUriFromGroup(nodeGroupLabel), SKOSProperty.CONCEPT_GROUP);
        sKOSResource.addRelation(nodeGroupLabel.getIdGroup(), getUriFromGroup(nodeGroupLabel), SKOSProperty.MICROTHESAURUS_OF);
        writeGroupInfo(ds, sKOSResource, idTheso, idGroup);
    }    
    
    public void exportCollections(HikariDataSource ds, String idTheso){
        GroupHelper groupHelper = new GroupHelper();
        ArrayList<String> rootGroupList = groupHelper.getListIdOfRootGroup(ds, idTheso);
        NodeGroupLabel nodeGroupLabel;
        for (String idGroup : rootGroupList) {
            nodeGroupLabel = groupHelper.getNodeGroupLabel(ds, idGroup, idTheso);
            SKOSResource sKOSResource = new SKOSResource(getUriFromGroup(nodeGroupLabel), SKOSProperty.CONCEPT_GROUP);
            sKOSResource.setIdentifier(idGroup);
            sKOSResource.addRelation(nodeGroupLabel.getIdGroup(), getUriFromGroup(nodeGroupLabel), SKOSProperty.MICROTHESAURUS_OF);
            addChildsGroupRecursive(ds, idTheso, idGroup, sKOSResource);
        }
    }
    public void addChildsGroupRecursive(HikariDataSource ds,
            String idTheso,
            String idParent,
            SKOSResource sKOSResource) {
        GroupHelper groupHelper = new GroupHelper();

        ArrayList<String> listIdsOfGroupChilds = groupHelper.getListGroupChildIdOfGroup(ds, idParent, idTheso);
        writeGroupInfo(ds, sKOSResource, idTheso, idParent);

        for (String idOfGroupChild : listIdsOfGroupChilds) {
            sKOSResource = new SKOSResource();
            sKOSResource.setIdentifier(idOfGroupChild);
            addChildsGroupRecursive(ds, idTheso, idOfGroupChild, sKOSResource);
        }
    }

    private void writeGroupInfo(HikariDataSource ds, SKOSResource sKOSResource, String idTheso, String idOfGroupChild) {

        GroupHelper groupHelper = new GroupHelper();
        NodeGroupLabel nodeGroupLabel;
        nodeGroupLabel = groupHelper.getNodeGroupLabel(ds, idOfGroupChild, idTheso);

        sKOSResource.setUri(getUriFromGroup(nodeGroupLabel));
        sKOSResource.setProperty(SKOSProperty.CONCEPT_GROUP);
    //    sKOSResource.setIdentifier(idOfGroupChild);

        //dates
        String created = null;
        String modified = null;
        if(nodeGroupLabel.getCreated()!=null)
            created = nodeGroupLabel.getCreated().toString();
        if(nodeGroupLabel.getModified()!=null)
            modified = nodeGroupLabel.getModified().toString();
        if (created != null) {
            sKOSResource.addDate(created, SKOSProperty.CREATED);
        }
        if (modified != null) {
            sKOSResource.addDate(modified, SKOSProperty.MODIFIED);
        }
        
        for (NodeGroupTraductions traduction : nodeGroupLabel.getNodeGroupTraductionses()) {
            sKOSResource.addLabel(traduction.getTitle(), traduction.getIdLang(), SKOSProperty.PREF_LABEL);
        }

        ArrayList<NodeUri> childURIs = groupHelper.getListGroupChildOfGroup(ds, idTheso, idOfGroupChild);
        ArrayList<NodeUri> nodeUris = new ConceptHelper().getListConceptsOfGroup(ds, idTheso, idOfGroupChild);

        for (NodeUri nodeUri : nodeUris) {
            sKOSResource.addRelation(nodeUri.getIdConcept(), getUriFromNodeUri(nodeUri, idTheso), SKOSProperty.MEMBER);
        }

        for (NodeUri nodeUri : childURIs) {
            sKOSResource.addRelation(nodeUri.getIdConcept(), getUriGroupFromNodeUri(nodeUri, idTheso), SKOSProperty.SUBGROUP);
            superGroupHashMap.put(nodeUri.getIdConcept(), idOfGroupChild);
        }

        String idSuperGroup = superGroupHashMap.get(idOfGroupChild);
        if (idSuperGroup != null) {
            NodeUri nodeUri1 = groupHelper.getThisGroupIds(ds, idSuperGroup, idTheso);
            if(nodeUri1 != null){
                sKOSResource.addRelation(idSuperGroup, getUriGroupFromNodeUri(nodeUri1, idTheso), SKOSProperty.SUPERGROUP);
                superGroupHashMap.remove(idOfGroupChild);
            }
        }

        // ajout de la notation
        if (nodeGroupLabel.getNotation() != null && !nodeGroupLabel.getNotation().equals("null")) {
            if(!nodeGroupLabel.getNotation().isEmpty())
                sKOSResource.addNotation(nodeGroupLabel.getNotation());
        }
        
        /// Ajout des notes
        NoteHelper noteHelper = new NoteHelper();
        ArrayList<NodeNote> nodeNotes = noteHelper.getListNotesAllLang(ds, idOfGroupChild, idTheso);
        addNoteGiven(nodeNotes, sKOSResource);
        
        skosXmlDocument.addGroup(sKOSResource);
    }


    /**
     * permet d'ajouter un concept à l'export en cours
     *
     * @param ds
     * @param idTheso
     * @param idConcept
     * @param isCandidatExport
     */
    public void exportConcept(HikariDataSource ds, String idTheso, String idConcept, boolean isCandidatExport) {

        SKOSResource sKOSResource = new SKOSResource();
        ConceptHelper conceptHelper = new ConceptHelper();
        NodeConceptExport nodeConcept = conceptHelper.getConceptForExport(ds, idConcept, idTheso, false, isCandidatExport);

        if (nodeConcept == null) {
            messages = messages + ("Erreur concept non exporté importé: " + idConcept + "\n");
            return;
        }

        if(nodeConcept.getNodeListOfBT().isEmpty() ){
            sKOSResource.addRelation(idTheso, getUriFromId(idTheso), SKOSProperty.TOP_CONCEPT_OF);
        }

        sKOSResource.setUri(getUri(nodeConcept));
        sKOSResource.setLocalUri(getLocalUri(nodeConcept));
        sKOSResource.setProperty(SKOSProperty.CONCEPT);

        //// définir le status du Concept (CA=candidat, DEP= déprécié, autre= concept)
        setStatusOfConcept(nodeConcept.getConcept().getStatus(), sKOSResource);

        // ajout des concepts de remplacements ReplacedBy et Replaces
        if(nodeConcept.getReplacedBy() != null && !nodeConcept.getReplacedBy().isEmpty()) {
            addReplaces(nodeConcept.getReplacedBy(), sKOSResource, idTheso);
        }
        if(nodeConcept.getReplaces() != null && !nodeConcept.getReplaces().isEmpty()) {
            addReplaces(nodeConcept.getReplaces(), sKOSResource, idTheso);
        }    
        

        // pour l'export des données du module candidat
        if (isCandidatExport) {
            sKOSResource.setSkosStatus(addStatut(conceptHelper.getNodeStatus(ds, idConcept, idTheso)));
            addDiscussions(nodeConcept.getMessages(), sKOSResource);
            addVotes(nodeConcept.getVotes(), sKOSResource, ds);
        }

        // prefLabel
        for (NodeTermTraduction traduction : nodeConcept.getNodeTermTraductions()) {
            sKOSResource.addLabel(traduction.getLexicalValue(), traduction.getLang(), SKOSProperty.PREF_LABEL);
        }
        // altLabel
        for (NodeEM nodeEM : nodeConcept.getNodeEM()) {
            if (nodeEM.isHiden()) {
                sKOSResource.addLabel(nodeEM.getLexical_value(), nodeEM.getLang(), SKOSProperty.HIDDEN_LABEL);
            } else {
                sKOSResource.addLabel(nodeEM.getLexical_value(), nodeEM.getLang(), SKOSProperty.ALT_LABEL);
            }
        }
        //ArrayList<NodeNote> nodeNotes = new ArrayList<>();//nodeConcept.getNodeNoteConcept();
        
        ArrayList<NodeNote> nodeNotes = nodeConcept.getNodeNotes();
//        nodeNotes.addAll(nodeConcept.getNodeNoteTerm());
//        nodeNotes.addAll(nodeConcept.getNodeNoteConcept());        
        addNoteGiven(nodeNotes, sKOSResource);
        addGPSGiven(nodeConcept.getNodeGps(), sKOSResource);
        addAlignementGiven(nodeConcept.getNodeAlignmentsList(), sKOSResource);
        
        // relations        
        addRelationGiven(nodeConcept.getNodeListOfBT(), nodeConcept.getNodeListOfNT(),
                nodeConcept.getNodeListIdsOfRT(), sKOSResource, nodeConcept.getConcept().getIdThesaurus());


        if (nodeConcept.getConcept().getNotation() != null && !nodeConcept.getConcept().getNotation().equals("null")) {
            sKOSResource.addNotation(nodeConcept.getConcept().getNotation());
        }

        sKOSResource.addRelation(idTheso, getUriFromId(idTheso), SKOSProperty.INSCHEME);

        for (NodeUri nodeUri : nodeConcept.getNodeListIdsOfConceptGroup()) {
            sKOSResource.addRelation(nodeUri.getIdConcept(), getUriGroupFromNodeUri(nodeUri, idTheso), SKOSProperty.MEMBER_OF);
        }

        sKOSResource.addIdentifier(idConcept, SKOSProperty.IDENTIFIER);
        if(nodeConcept.getConcept().getIdArk() != null && !nodeConcept.getConcept().getIdArk().isEmpty() ){
           sKOSResource.setArkId(nodeConcept.getConcept().getIdArk());
        }


        if(nodeConcept.getListFacetsOfConcept() != null) {
            for (String idFacette : nodeConcept.getListFacetsOfConcept()) {
                int prop = SKOSProperty.SUB_ORDINATE_ARRAY;
                sKOSResource.addRelation(idFacette, getUriForFacette(idFacette, idTheso), prop);
            }
        }

    /*    ArrayList<String> first = new ArrayList<>();
        first.add(idConcept);
        ArrayList<ArrayList<String>> paths = new ArrayList<>();

        paths = new ConceptHelper().getPathOfConceptWithoutGroup(ds, idConcept, idTheso, first, paths);
        ArrayList<String> pathFromArray = getPathFromArray(paths);
        if (!pathFromArray.isEmpty()) {
            sKOSResource.setPaths(pathFromArray);
        }
        */
        
        // les images
        if(nodeConcept.getNodeImages() != null || (!nodeConcept.getNodeImages().isEmpty())) {
            for (NodeImage nodeImage : nodeConcept.getNodeImages()) {
                sKOSResource.addNodeImage(nodeImage);
            }
        }

        // createur et contributeur
        if (nodeConcept.getConcept().getCreatorName()!= null && !nodeConcept.getConcept().getCreatorName().isEmpty()) {
            sKOSResource.addAgent(nodeConcept.getConcept().getCreatorName(), SKOSProperty.CREATOR);
        }
        if (nodeConcept.getConcept().getContributorName()!= null && !nodeConcept.getConcept().getContributorName().isEmpty()) {
            sKOSResource.addAgent(nodeConcept.getConcept().getContributorName(), SKOSProperty.CONTRIBUTOR);
        }

        // dates
        if (nodeConcept.getConcept().getCreated() != null) {
            sKOSResource.addDate(nodeConcept.getConcept().getCreated().toString(), SKOSProperty.CREATED);
        }
        if (nodeConcept.getConcept().getModified() != null) {
            sKOSResource.addDate(nodeConcept.getConcept().getModified().toString(), SKOSProperty.MODIFIED);
        }

        skosXmlDocument.addconcept(sKOSResource);
    }
    private void setStatusOfConcept(String status, SKOSResource sKOSResource){
        switch (status.toLowerCase()) {
            case "ca":
                sKOSResource.setStatus(SKOSProperty.CANDIDATE);
                break;
            case "dep":
                sKOSResource.setStatus(SKOSProperty.DEPRECATED);
                break;
            default:
                sKOSResource.setStatus(SKOSProperty.CONCEPT);
                break;              
        }
        
    }
    private SKOSStatus addStatut(NodeStatus nodeStatus) {
        SKOSStatus skosStatus = new SKOSStatus();
        skosStatus.setDate(nodeStatus.getDate());
        skosStatus.setIdConcept(nodeStatus.getIdConcept());
        skosStatus.setIdStatus(nodeStatus.getIdStatus());
        skosStatus.setMessage(nodeStatus.getMessage());
        skosStatus.setIdThesaurus(nodeStatus.getIdThesaurus());
        skosStatus.setIdUser(nodeStatus.getIdUser());
        return skosStatus;
    }

    private ArrayList<String> getPathFromArray(ArrayList<ArrayList<String>> paths) {
        String pathFromArray = "";
        ArrayList<String> allPath = new ArrayList<>();
        for (ArrayList<String> path : paths) {
            for (String string1 : path) {
                if(pathFromArray.isEmpty())
                    pathFromArray = string1;
                else
                    pathFromArray = pathFromArray + "##" + string1;
            }
            allPath.add(pathFromArray);
            pathFromArray = "";
        }
        return allPath;
    }
    
    private void addNoteGiven(ArrayList<NodeNote> nodeNotes, SKOSResource resource) {
        for (NodeNote note : nodeNotes) {
            int prop;
            switch (note.getNotetypecode()) {
                case "note":
                    prop = SKOSProperty.NOTE;
                    break;                  
                case "scopeNote":
                    prop = SKOSProperty.SCOPE_NOTE;
                    break;
                case "historyNote":
                    prop = SKOSProperty.HISTORY_NOTE;
                    break;
                case "example":
                    prop = SKOSProperty.EXAMPLE;
                    break;                    
                case "definition":
                    prop = SKOSProperty.DEFINITION;
                    break;
                case "editorialNote":
                    prop = SKOSProperty.EDITORIAL_NOTE;
                    break;
                case "changeNote":
                    prop = SKOSProperty.CHANGE_NOTE;
                    break;             
                default:
                    prop = SKOSProperty.NOTE;
                    break;
            }
            resource.addDocumentation(note.getLexicalvalue(), note.getLang(), prop);
        }
    }


    private void addDiscussions(List<MessageDto> messages, SKOSResource resource) {
        for (MessageDto message : messages) {
            SKOSDiscussion skosDiscussion = new SKOSDiscussion();
            skosDiscussion.setMsg(message.getMsg());
            skosDiscussion.setIdUser(message.getIdUser());
            skosDiscussion.setDate(message.getDate());
            resource.addMessage(skosDiscussion);
        }
    }


    private void addVotes(List<VoteDto> votes, SKOSResource resource, HikariDataSource ds) {
        for (VoteDto vote : votes) {
            SKOSVote skosVote = new SKOSVote();
            skosVote.setIdNote(vote.getIdNote());
            skosVote.setIdUser(vote.getIdUser());
            skosVote.setIdThesaurus(vote.getIdThesaurus());
            skosVote.setIdConcept(vote.getIdConcept());
            skosVote.setTypeVote(vote.getTypeVote());
            if (!StringUtils.isEmpty(vote.getIdNote()) && !"null".equalsIgnoreCase(vote.getIdNote())) {
                String htmlTagsRegEx = "<[^>]*>";
                NodeNote nodeNote = new NoteHelper().getNoteByIdNote(ds, Integer.parseInt(vote.getIdNote()));
                if (nodeNote != null) {
                    String str = ConceptHelper.formatLinkTag(nodeNote.getLexicalvalue());
                    skosVote.setValueNote(str.replaceAll(htmlTagsRegEx, ""));
                }
            }
            resource.addVote(skosVote);
        }
    }

    private void addGPSGiven(List<NodeGps> gps, SKOSResource resource) {
        if (gps == null) {
            return;
        }
        if (CollectionUtils.isNotEmpty(gps)) {
            List<SKOSGPSCoordinates> elements = new ArrayList<>();
            for (NodeGps element : gps) {
                elements.add(new SKOSGPSCoordinates(element.getLatitude(), element.getLongitude()));
            }
            resource.setGpsCoordinates(elements);
        }
    }


    private void addAlignementGiven(ArrayList<NodeAlignmentSmall> nodeAlignments, SKOSResource resource) {
        for (NodeAlignmentSmall alignment : nodeAlignments) {

            int prop = -1;
            switch (alignment.getAlignement_id_type()) {

                case 1:
                    prop = SKOSProperty.EXACT_MATCH;
                    break;
                case 2:
                    prop = SKOSProperty.CLOSE_MATCH;
                    break;
                case 3:
                    prop = SKOSProperty.BROAD_MATCH;
                    break;
                case 4:
                    prop = SKOSProperty.RELATED_MATCH;
                    break;
                case 5:
                    prop = SKOSProperty.NARROWER_MATCH;
                    break;
            }
            resource.addMatch(alignment.getUri_target(), prop);
        }
    }
    private void addRelationGiven(ArrayList<NodeHieraRelation> btList, ArrayList<NodeHieraRelation> ntList,
            ArrayList<NodeHieraRelation> rtList, SKOSResource resource, String idTheso) {
        for (NodeHieraRelation rt : rtList) {
            int prop;

            switch (rt.getRole()) {
                case "RHP":
                    prop = SKOSProperty.RELATED_HAS_PART;
                    break;
                case "RPO":
                    prop = SKOSProperty.RELATED_PART_OF;
                    break;
                default:
                    prop = SKOSProperty.RELATED;
            }
            resource.addRelation(rt.getUri().getIdConcept(), getUriFromNodeUri(rt.getUri(), idTheso), prop);
        }
        for (NodeHieraRelation nt : ntList) {
            int prop;
            switch (nt.getRole()) {
                case "NTG":
                    prop = SKOSProperty.NARROWER_GENERIC;
                    break;
                case "NTP":
                    prop = SKOSProperty.NARROWER_PARTITIVE;
                    break;
                case "NTI":
                    prop = SKOSProperty.NARROWER_INSTANTIAL;
                    break;
                default:
                    prop = SKOSProperty.NARROWER;
            }
            resource.addRelation(nt.getUri().getIdConcept(), getUriFromNodeUri(nt.getUri(), idTheso), prop);
        }
        for (NodeHieraRelation bt : btList) {

            int prop;
            switch (bt.getRole()) {
                case "BTG":
                    prop = SKOSProperty.BROADER_GENERIC;
                    break;
                case "BTP":
                    prop = SKOSProperty.BROADER_PARTITIVE;
                    break;
                case "BTI":
                    prop = SKOSProperty.BROADER_INSTANTIAL;
                    break;
                default:
                    prop = SKOSProperty.BROADER;
            }
            resource.addRelation(bt.getUri().getIdConcept(), getUriFromNodeUri(bt.getUri(), idTheso), prop);
        }
    }    
    
    // ajoute les données de remplacement des concepts dépréciés dans les 2 sens (Replaced et Replace)
    private void addReplaces(ArrayList<NodeHieraRelation> replaces,
             SKOSResource resource, String idTheso) {
        for (NodeHieraRelation uriReplace : replaces) {
            int prop;

            switch (uriReplace.getRole()) {
                case "replacedBy":
                    prop = SKOSProperty.IS_REPLACED_BY;
                    break;
                case "replace":
                    prop = SKOSProperty.REPLACES;
                    break;
                default:
                    prop = -1;
                    break;
            }
            resource.addReplaces(getUriFromNodeUri(uriReplace.getUri(), idTheso), prop);
        }
    }    

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    /////////////////////Fin des nouvelles fonctions ///////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////// 
    public String getUriFromId(String id) {
        if(nodePreference.isOriginalUriIsArk()){
            if(!StringUtils.isEmpty((nodePreference.getOriginalUri()))){
                return nodePreference.getOriginalUri()+ "/" + nodePreference.getIdNaan() + "/" + id;
            } else {
                return getPath() + "/?idt=" + id;
            }
        } else {
            return getPath() + "/?idt=" + id;
        }
      /*  if(nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty() 
            && !"null".equals(nodePreference.getOriginalUri())) {
            if(nodePreference.isOriginalUriIsArk()) {
                return nodePreference.getOriginalUri()+ "/" + nodePreference.getIdNaan() + "/" + id;
            }            
            return nodePreference.getOriginalUri() + "/" + nodePreference.getIdNaan() + "/" + id;
        } else {            
            return getPath() + "/?idt=" + id;
        }*/
    }

    /**
     * Cette fonction permet de retourner l'URI du concept avec identifiant Ark
     * : si renseigné sinon l'URL du Site
     *
     * @param nodeConceptExport
     * @return
     */
    private String getUri(NodeConceptExport nodeConceptExport) {
        String uri = "";
        if (nodeConceptExport == null) {
            return uri;
        }
        if (nodeConceptExport.getConcept() == null) {
            return uri;
        }

        // Choix de l'URI pour l'export : 
        // Si Handle est actif, on le prend en premier 
        // sinon,  on vérifie si Ark est actif, 
        // en dernier, on prend l'URL basique d'Opentheso
        // 1 seule URI est possible pour l'export par concept
        // URI de type Ark
        if(nodePreference.isOriginalUriIsArk()) {
            if (nodeConceptExport.getConcept().getIdArk() != null && !nodeConceptExport.getConcept().getIdArk().isEmpty()) {
                uri = nodePreference.getOriginalUri()+ "/" +nodeConceptExport.getConcept().getIdArk();
                return uri;
            } else {
                uri = getPath()+ "/?idc=" + nodeConceptExport.getConcept().getIdConcept().trim()
                            + "&idt=" + nodeConceptExport.getConcept().getIdThesaurus();                
                return uri;
            }
        }
        
        if(nodePreference.isOriginalUriIsHandle()) {
            // URI de type Handle
            if (nodeConceptExport.getConcept().getIdHandle() != null && !nodeConceptExport.getConcept().getIdHandle().isEmpty()) {
                if (!nodeConceptExport.getConcept().getIdHandle().trim().isEmpty()) {
                    uri = "https://hdl.handle.net/" + nodeConceptExport.getConcept().getIdHandle();
                    return uri;
                }
            }
        }
        if(nodePreference.isOriginalUriIsDoi()) {
            // URI de type Doi
            if (nodeConceptExport.getConcept().getIdDoi() != null) {
                if (!nodeConceptExport.getConcept().getIdDoi().trim().isEmpty()) {
                    uri = "https://doi.org/" + nodeConceptExport.getConcept().getIdDoi();
                    return uri;
                }
            }
        }        
        // si on ne trouve pas ni Handle, ni Ark
        //    uri = nodePreference.getCheminSite() + nodeConceptExport.getConcept().getIdConcept();
        if(nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty()) {
            uri = nodePreference.getOriginalUri()+ "/?idc=" + nodeConceptExport.getConcept().getIdConcept().trim()
                        + "&idt=" + nodeConceptExport.getConcept().getIdThesaurus();
        } else {
            uri = getPath()+ "/?idc=" + nodeConceptExport.getConcept().getIdConcept().trim()
                        + "&idt=" + nodeConceptExport.getConcept().getIdThesaurus();
        }

        return uri;
    }
    
    private String getLocalUri(NodeConceptExport nodeConceptExport){

        String uri = "";
        if(getPath() == null) return uri;        
        if (nodeConceptExport == null) {
            return uri;
        }
        if (nodeConceptExport.getConcept() == null) {
            return uri;
        }
        uri = getPath()+ "/?idc=" + nodeConceptExport.getConcept().getIdConcept().trim()
                    + "&idt=" + nodeConceptExport.getConcept().getIdThesaurus();
        return uri;
    }

    /**
     * Cette fonction permet de retourner l'URI du concept avec identifiant Ark : si renseigné sinon l'URL du Site
    * @param nodeGroupLabel
    * @return 
    */
    public String getUriFromGroup(NodeGroupLabel nodeGroupLabel) {
        String uri = "";
        if (nodeGroupLabel == null) {
            return uri;
        }
        if (nodeGroupLabel.getIdGroup() == null) {
            return uri;
        }

        // Choix de l'URI pour l'export : 
        // Si Handle est actif, on le prend en premier 
        // sinon,  on vérifie si Ark est actif, 
        // en dernier, on prend l'URL basique d'Opentheso
        // 1 seule URI est possible pour l'export par concept
        // URI de type Ark
        if(nodePreference.isOriginalUriIsArk()) {
            if (nodeGroupLabel.getIdArk() != null && !nodeGroupLabel.getIdArk().trim().isEmpty()) {
                uri = nodePreference.getUriArk() + nodeGroupLabel.getIdArk();
                return uri;
            } else {
                uri = getPath() + "/?idg=" + nodeGroupLabel.getIdGroup()
                            + "&idt=" + nodeGroupLabel.getIdThesaurus();
                return uri;
            }
            
        }
        if(nodePreference.isOriginalUriIsHandle()) {        
            // URI de type Handle
            if (nodeGroupLabel.getIdHandle() != null) {
                if (!nodeGroupLabel.getIdHandle().trim().isEmpty()) {
                    uri = "https://hdl.handle.net/" + nodeGroupLabel.getIdHandle();
                    return uri;
                }
            }
        }
        if(nodePreference.isOriginalUriIsDoi()) {        
            // URI de type Doi
            if (nodeGroupLabel.getIdDoi() != null) {
                if (!nodeGroupLabel.getIdDoi().trim().isEmpty()) {
                    uri = "https://doi.org/" + nodeGroupLabel.getIdDoi();
                    return uri;
                }
            }
        }        
        // si on ne trouve pas ni Handle, ni Ark
//        uri = nodePreference.getCheminSite() + nodeGroupLabel.getIdGroup();
        if(nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty()) {
            uri = nodePreference.getOriginalUri() + "/?idg=" + nodeGroupLabel.getIdGroup()
                        + "&idt=" + nodeGroupLabel.getIdThesaurus();
        } else {
            uri = getPath() + "/?idg=" + nodeGroupLabel.getIdGroup()
                        + "&idt=" + nodeGroupLabel.getIdThesaurus();
        }

    //    uri = nodePreference.getCheminSite() + nodeGroupLabel.getIdGroup();
        return uri;
    }

    /**
     * Cette fonction permet de retourner l'URI du concept avec identifiant Ark
     * : si renseigné sinon l'URL du Site
     *
     * @return
     */
    private String getUriGroupFromNodeUri(NodeUri nodeUri, String idTheso) {
        String uri = "";
        if (nodeUri == null) {
            return uri;
        }

        // Choix de l'URI pour l'export : 
        // Si Handle est actif, on le prend en premier 
        // sinon,  on vérifie si Ark est actif, 
        // en dernier, on prend l'URL basique d'Opentheso
        // 1 seule URI est possible pour l'export par concept
        // URI de type Ark
        if(nodePreference.isOriginalUriIsArk()) { 
            if (nodeUri.getIdArk() != null && !nodeUri.getIdArk().isEmpty()) {
                uri = nodePreference.getOriginalUri()+ "/" + nodeUri.getIdArk();
                return uri;
            } else {
                uri = getPath() + "/?idg=" + nodeUri.getIdConcept()
                                + "&idt=" + idTheso;              
                return uri;
            }
        }
        // URI de type Handle
        if (nodeUri.getIdHandle() != null) {
            if (!nodeUri.getIdHandle().trim().isEmpty()) {
                uri = "https://hdl.handle.net/" + nodeUri.getIdHandle();
                return uri;
            }
        }

        // si on ne trouve pas ni Handle, ni Ark
        //    uri = nodePreference.getCheminSite() + nodeUri.getIdConcept();
//        uri = nodePreference.getCheminSite() + "?idg=" + nodeUri.getIdConcept()
//                        + "&idt=" + idTheso;
        if(nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty()) {
            uri = nodePreference.getOriginalUri() + "/?idg=" + nodeUri.getIdConcept()
                            + "&idt=" + idTheso;
        } else {
            uri = getPath() + "/?idg=" + nodeUri.getIdConcept()
                            + "&idt=" + idTheso;
        }

        return uri;
    }

    /**
     * Cette fonction permet de retourner l'URI du concept avec identifiant Ark
     * : si renseigné sinon l'URL du Site
     *
     * @return
     */
    private String getUriFromNodeUri(NodeUri nodeUri, String idTheso) {
        String uri = "";
        if (nodeUri == null) {
            return uri;
        }

        // Choix de l'URI pour l'export : 
        // Si Handle est actif, on le prend en premier 
        // sinon,  on vérifie si Ark est actif, 
        // en dernier, on prend l'URL basique d'Opentheso
        // 1 seule URI est possible pour l'export par concept
        // URI de type Ark

        if(nodePreference.isOriginalUriIsArk()) {
            if (nodeUri.getIdArk() != null && !nodeUri.getIdArk().trim().isEmpty()) {
                uri = nodePreference.getOriginalUri()+ "/" + nodeUri.getIdArk();
                return uri;
            } else {
                uri = getPath() + "/?idc=" + nodeUri.getIdConcept().trim()
                        + "&idt=" + idTheso;
                return uri;
            }
        }
        if(nodePreference.isOriginalUriIsHandle()) {
            // URI de type Handle
            if (nodeUri.getIdHandle() != null) {
                if (!nodeUri.getIdHandle().trim().isEmpty()) {
                    uri = "https://hdl.handle.net/" + nodeUri.getIdHandle();
                    return uri;
                }
            }
        }
        if(nodePreference.isOriginalUriIsDoi()) {
            // URI de type Doi
            if (nodeUri.getIdDoi() != null) {
                if (!nodeUri.getIdDoi().trim().isEmpty()) {
                    uri = "https://doi.org/" + nodeUri.getIdDoi();
                    return uri;
                }
            }
        }        

        // si on ne trouve pas ni Handle, ni Ark
        if(nodePreference.getOriginalUri() != null && !nodePreference.getOriginalUri().isEmpty()) {
            uri = nodePreference.getOriginalUri() + "/?idc=" + nodeUri.getIdConcept().trim()
                        + "&idt=" + idTheso;
        } else {
            uri = getPath() + "/?idc=" + nodeUri.getIdConcept().trim()
                        + "&idt=" + idTheso;
        }

                        //+ "&amp;idt=" + idTheso;
    //    uri = nodePreference.getCheminSite() + nodeUri.getIdConcept();
        return uri;
    }

    
    private String getUriForFacette(String idFacet, String idTheso){
        String uri = "";
        if (idFacet == null) {
            return uri;
        }
        uri = getPath()+ "/?idf=" + idFacet + "&idt=" +idTheso;
        return uri;
    }    
    
    /**
     * permet de retourner le Path de l'application
     * exp:  //http://localhost:8082/opentheso2
     * @return
     */
    private String getPath(){
        if(FacesContext.getCurrentInstance() == null) {
            return nodePreference.getOriginalUri();
        }
        String path = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap().get("origin");
        if(path == null)
            return nodePreference.getOriginalUri();
        return path + FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
    }

    public SKOSXmlDocument getSkosXmlDocument() {
        return skosXmlDocument;
    }

    public void getMessages() {
        if(messages != null) {
            if (!messages.isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Info :", messages));
            }
        }
    }
  
}
