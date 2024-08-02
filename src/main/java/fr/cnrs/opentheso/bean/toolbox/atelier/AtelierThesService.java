package fr.cnrs.opentheso.bean.toolbox.atelier;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.PreferencesHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.ThesaurusHelper;
import fr.cnrs.opentheso.bdd.helper.UserHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeBT;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeEM;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePreference;
import fr.cnrs.opentheso.bdd.helper.nodes.concept.NodeConcept;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.users.CurrentUser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.event.FileUploadEvent;


@Named
@ViewScoped
public class AtelierThesService implements Serializable {
    @Inject private Connect connect;
    @Inject private LanguageBean languageBean;
    @Inject private CurrentUser currentUser;
    
    
    public ArrayList<ConceptResultNode> comparer(List<List<String>> datas, int position, NodeIdValue thesoSelected) {
        
        PreferencesHelper preferencesHelper = new PreferencesHelper();
        NodePreference nodePreference = preferencesHelper.getThesaurusPreferences(connect.getPoolConnexion(), thesoSelected.getId());
        
        ArrayList<ConceptResultNode> list = new ArrayList<>();
        ConceptHelper conceptHelper = new ConceptHelper();
        int limit = 5;
        for (List<String> data : datas) {
            if(data.get(position) == null || data.get(position).isEmpty()) continue;
            ArrayList<NodeSearchMini> temp = new SearchHelper().searchFullText(connect.getPoolConnexion(), 
                    data.get(position), languageBean.getIdLangue(), thesoSelected.getId(), limit);
            
            if (!CollectionUtils.isEmpty(temp)) {
                temp.forEach(nodeSearchMini -> {
                    NodeConcept concept = conceptHelper.getConcept(connect.getPoolConnexion(), nodeSearchMini.getIdConcept(), 
                            thesoSelected.getId(), languageBean.getIdLangue(), -1, -1);

                    ConceptResultNode conceptResultNode = new ConceptResultNode();
                    if(data.size() == 1) 
                        conceptResultNode.setIdOrigine("");
                    else
                        conceptResultNode.setIdOrigine(data.get(0));
                    conceptResultNode.setPrefLabelOrigine(data.get(position));
                    conceptResultNode.setIdConcept(concept.getConcept().getIdConcept());
                    conceptResultNode.setPrefLabelConcept(concept.getTerm().getLexical_value());

                    if(concept.getNodeEM() == null || concept.getNodeEM().isEmpty())
                        conceptResultNode.setAltLabelConcept("");
                    else {
                        for (NodeEM nodeEM : concept.getNodeEM()) {
                            if(conceptResultNode.getAltLabelConcept() == null)
                                conceptResultNode.setAltLabelConcept(nodeEM.getLexical_value());
                            else 
                                conceptResultNode.setAltLabelConcept(conceptResultNode.getAltLabelConcept() + " ## " + nodeEM.getLexical_value());
                        }                        
                    }
                    for (NodeBT nodeBT : concept.getNodeBT()) {
                        if(conceptResultNode.getTermGenerique() == null)
                            conceptResultNode.setTermGenerique(nodeBT.getTitle());
                        else 
                            conceptResultNode.setTermGenerique(conceptResultNode.getTermGenerique() + " ## " + nodeBT.getTitle());
                    }                    
                    
                    conceptResultNode.setDefinition(getDefinition(concept.getNodeNotesTerm()));
                    if(concept.getConcept().getIdArk()!= null && !concept.getConcept().getIdArk().isEmpty()) {
                        conceptResultNode.setUriArk(nodePreference.getUriArk() + concept.getConcept().getIdArk());
                    } else
                        conceptResultNode.setUriArk("");
                    list.add(conceptResultNode);

                });
            } else {
                ConceptResultNode conceptResultNode = new ConceptResultNode();
                conceptResultNode.setIdOrigine(data.get(0));
                conceptResultNode.setPrefLabelOrigine(data.get(position));
                list.add(conceptResultNode);    
            }
            
        }
        
        return list;
    }
    
    private String getDefinition(ArrayList<NodeNote> notes) {
        String definition = "";
        for (NodeNote note : notes) {
            if ("definition".equals(note.getNotetypecode())) {
                definition = note.getLexicalvalue();
            }
        }
        return definition;
    }
    
    public List<List<String>> loadCsvFile(FileUploadEvent event, char delimiterCsv) {
        List<List<String>> values = new ArrayList<>();
        try {
            String line;
            Reader reader = new InputStreamReader(event.getFile().getInputStream());
            BufferedReader br = new BufferedReader(reader);
            while ((line = br.readLine()) != null) {
                List<String> list = Arrays.asList(line.split(""+delimiterCsv));
                values.add(list);
            }
            br.close();
            reader.close();
        } catch (IOException e) {

        }
        return values;
    }
    
    public ArrayList<NodeIdValue> searchAllThesaurus() {

        if(currentUser.getNodeUser() == null)
            return new ArrayList<>();

        List<String> authorizedTheso;
        if (currentUser.getNodeUser().isSuperAdmin()) {
            authorizedTheso = new ThesaurusHelper().getAllIdOfThesaurus(connect.getPoolConnexion(), true);
        } else {
            authorizedTheso = new UserHelper().getThesaurusOfUser(connect.getPoolConnexion(), currentUser.getNodeUser().getIdUser());
        }
        
        if (authorizedTheso == null) {
            return new ArrayList<>();
        }
        
        ArrayList<NodeIdValue> nodeListTheso = new ArrayList<>();
        ThesaurusHelper thesaurusHelper = new ThesaurusHelper();

        String preferredIdLangOfTheso;
        for (String idTheso1 : authorizedTheso) {
            
            preferredIdLangOfTheso = new PreferencesHelper()
                    .getWorkLanguageOfTheso(connect.getPoolConnexion(), idTheso1);
            if (preferredIdLangOfTheso == null) {
                preferredIdLangOfTheso = connect.getWorkLanguage().toLowerCase();
            }

            NodeIdValue nodeIdValue = new NodeIdValue();
            nodeIdValue.setId(idTheso1);
            nodeIdValue.setValue(thesaurusHelper.getTitleOfThesaurus(connect.getPoolConnexion(), 
                    idTheso1, preferredIdLangOfTheso));
            nodeIdValue.setStatus(thesaurusHelper.isThesoPrivate(connect.getPoolConnexion(), idTheso1));
            nodeListTheso.add(nodeIdValue);
            
        }
        
        return nodeListTheso;
    }
    
}
