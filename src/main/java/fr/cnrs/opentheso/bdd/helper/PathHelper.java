/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.nodes.NodePath;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeUri;
import fr.cnrs.opentheso.bdd.helper.nodes.Path;
import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.ArrayDeque;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author miledrousset
 */
public class PathHelper {
    private String message;
    
    
    /**
     * methode pour retrouver tous les chemins vers la racine
     * exemple de retour 
     *  9##8 (9 a un BT 8)
        8##7 (8`a un BT 7)
        7##5
        5##4
        4  (défini le top term)
        5##6
        6##18
        18 (top term)
        8##5 
     * 
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @return 
     * #MR
     */   
    public List<String> getGraphOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {
        message = null; 
        List<String> path = new ArrayList<>();
        getGraph(ds, idConcept, idThesaurus, path);
        return path;
    }
    private void getGraph(HikariDataSource ds,
            String idConcept, String idThesaurus, List<String> path){
        RelationsHelper relationsHelper = new RelationsHelper();
        ArrayList<String> idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if(idBTs == null || idBTs.isEmpty()){
            if(!path.contains(idConcept)) {
                path.add(idConcept);
            }            
        } else {
            for (String idBT : idBTs) {
                if(!path.contains(idConcept + "##" + idBT) && !path.contains(idBT + "##" + idConcept)) {
                    path.add(idConcept + "##" + idBT);
                    getGraph(ds, idBT, idThesaurus, path);
                }
            }       
        }
    }
    
    public List<List<String>> getPathFromGraph(List<String> graph){
        List<String> topTerms = getTopTermsFromGraph(graph);
        
        List<List<String>> paths = new ArrayList<>();
        List<String> savedPath = new ArrayList<>();
        for (String topTerm : topTerms) {
            List<String> path = new ArrayList<>();
            getNT(topTerm, path, savedPath, paths, graph);
            if(!path.isEmpty()){
                paths.add(path);
            }
        }
        return paths;
    }
    private void getNT(String idConcept, List<String> path, List<String> savedPath, List<List<String>> paths, List<String> graph){

        List<String> idNTs = getListNT(idConcept, graph);
        path.add(idConcept);
        
        if(idNTs != null && idNTs.size() > 1){
            for (String idC : path) {
                savedPath.add(idC);
            }
        }
        
        if(idNTs == null || idNTs.isEmpty()){
            List<String> pathTemp = new ArrayList<>();
            for (String path1 : path) {
                pathTemp.add(path1);
            }
            paths.add(pathTemp);
            path.clear();
            if(!savedPath.isEmpty()){
                for (String idC : savedPath) {
                   path.add(idC);
                }
                savedPath.clear();
            }
        } else {
            for (String idNT : idNTs) {
                getNT(idNT, path, savedPath, paths, graph);
            }       
        }        
    }
    
    private List<String> getListNT(String idConcept, List<String> graph){
        List<String> listNTs = new ArrayList<>();
        for (String node : graph) {
            if(StringUtils.endsWith(node, "##"+idConcept)){
                listNTs.add(StringUtils.substringBefore(node, "##"));
            }
        }
        return listNTs;
    }
    
    
    private List<String> getTopTermsFromGraph(List<String> graph){
        List<String> topTerms = new ArrayList<>();
        for (String node : graph) {
            if(!node.contains("##")){
                topTerms.add(node);
            }
        }
        return topTerms;
    }
    
    public List<List<NodePath>> getPathWithLabel2(HikariDataSource ds, List<List<String>> paths,
            String idTheso, String idLang) {
        
        ArrayList<List<NodePath>> pathLabel1 = new ArrayList<>();
        boolean isStartNewPath;
        String label;
        
        for (List<String> path : paths) {
            isStartNewPath = true;
            List<NodePath> nodePaths = new ArrayList<>();
            for (String idConcept : path) {
                NodePath nodePath = new NodePath();
                nodePath.setIdConcept(idConcept);
                label = new ConceptHelper().getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
                if(label.isEmpty())
                    label = "("+ idConcept+")";
                nodePath.setTitle(label);
                nodePath.setIsStartOfPath(isStartNewPath);
                nodePaths.add(nodePath);
                isStartNewPath = false;
            }
            pathLabel1.add(nodePaths);
        }
        return pathLabel1;
    }    
    
    
    
    
    
    
    
    
    
    public ArrayList<NodePath> getPathWithLabel(HikariDataSource ds, List<Path> paths,
            String idTheso, String idLang, String selectedIdConcept) {
        
        ArrayList<NodePath> pathLabel1 = new ArrayList<>();
        boolean isStartNewPath = true;
        String label;
        
        for (Path path1 : paths) {
            for (String idConcept : path1.getPath()) {
                if(!idConcept.equalsIgnoreCase(selectedIdConcept)) {
                    NodePath nodePath = new NodePath();
                    nodePath.setIdConcept(idConcept);
                    label = new ConceptHelper().getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
                    if(label.isEmpty())
                        label = "("+ idConcept+")";
                    nodePath.setTitle(label);
                    nodePath.setIsStartOfPath(isStartNewPath);
                    pathLabel1.add(nodePath);
                    isStartNewPath = false;
                }
            }
            isStartNewPath = true;
        }
        return pathLabel1;
    }  
    
    public void getPathWithLabelAsJson(HikariDataSource ds,
            List<Path> paths, JsonArrayBuilder jsonArrayBuilder,
            String idTheso, String idLang, String selectedIdConcept, String format) {
        String label;
        ConceptHelper conceptHelper = new ConceptHelper();
    //    GroupHelper groupHelper = new GroupHelper();
        TermHelper termHelper = new TermHelper();
        NoteHelper noteHelper = new NoteHelper();
       // ArrayList<NodeGroup> nodeGroup;
    //    ArrayList<NodeUri> nodeGroup;
        int i = 0;
         
        if(idLang == null || idLang.isEmpty()) {
            idLang = new PreferencesHelper().getWorkLanguageOfTheso(ds, idTheso);
        }
                
        for (Path path1 : paths) {
            JsonArrayBuilder jsonArrayBuilderPath = Json.createArrayBuilder();            
            for (String idConcept : path1.getPath()) {
                JsonObjectBuilder job = Json.createObjectBuilder();

                label = conceptHelper.getLexicalValueOfConcept(ds, idConcept, idTheso, idLang);
                if(label.isEmpty())
                    label = "("+ idConcept+")";
                job.add("id", idConcept);
                job.add("arkId", conceptHelper.getIdArkOfConcept(ds, idConcept, idTheso));
                job.add("label", label);
                
                
                if(format != null && format.equalsIgnoreCase("full")) {
                    if(i++ == path1.getPath().size() - 1){
                        // synonymes
                        ArrayList<String> altLabels = termHelper.getNonPreferredTermsLabel(ds, idConcept, idTheso, idLang);
                        
                        JsonArrayBuilder jsonArrayBuilderAltLabels = Json.createArrayBuilder();
                        for (String altLabel : altLabels) {
                            jsonArrayBuilderAltLabels.add(altLabel);
                        }
                        if(jsonArrayBuilderAltLabels != null && !altLabels.isEmpty())
                            job.add("altLabel", jsonArrayBuilderAltLabels.build());  
                        
                        // défintion
                        ArrayList<String> definitions = noteHelper.getDefinition(ds, idConcept, idTheso, idLang);
                        JsonArrayBuilder jsonArrayBuilderDefinitions = Json.createArrayBuilder();
                        for (String def : definitions) {
                            jsonArrayBuilderDefinitions.add(def);
                        }
                        if(jsonArrayBuilderDefinitions != null && !definitions.isEmpty())
                            job.add("definition", jsonArrayBuilderDefinitions.build());      
                        
                        // note d'application
                        ArrayList<String> scopeNotes = noteHelper.getScopeNote(ds, idConcept, idTheso, idLang);
                        JsonArrayBuilder jsonArrayBuilderScopeNotes = Json.createArrayBuilder();
                        for (String scope : scopeNotes) {
                            jsonArrayBuilderScopeNotes.add(scope);
                        }
                        if(jsonArrayBuilderScopeNotes != null && !scopeNotes.isEmpty())
                            job.add("scopeNote", jsonArrayBuilderScopeNotes.build());
                        
                        // traductions
                        ArrayList<NodeTermTraduction> traductions = termHelper.getTraductionsOfConcept(ds, idConcept, idTheso, idLang);

                        JsonObjectBuilder jobTrad = Json.createObjectBuilder();
                        for (NodeTermTraduction trad : traductions) {
                            jobTrad.add(trad.getLang(), trad.getLexicalValue());                            
                        }
                        if(jobTrad != null){
                            if(!traductions.isEmpty()) {
                                job.add("labels", jobTrad.build());
                            }
                        }
                    }
                }
                
                // ajout des groups
                // temps 2 secondes
            /*    nodeGroup = groupHelper.getListGroupOfConceptArk(ds, idTheso, idConcept);//ListGroupOfConcept(ds, idTheso, idConcept, idLang);
                JsonArrayBuilder jsonArrayBuilderGroup = Json.createArrayBuilder();
                for (NodeUri nodeUri : nodeGroup) {
                    JsonObjectBuilder jobGroup = Json.createObjectBuilder();
                    jobGroup.add("id", nodeUri.getIdConcept());
                    jobGroup.add("arkId", nodeUri.getIdArk());
                    jobGroup.add("label", groupHelper.getLexicalValueOfGroup(ds, nodeUri.getIdConcept(), idTheso, idLang));
                    jsonArrayBuilderGroup.add(jobGroup.build());
                }       */         
                
                
                // temps 6 secondes
                /*
                nodeGroup = groupHelper.getListGroupOfConcept(ds, idTheso, idConcept, idLang);
                for (NodeGroup nodeGroup1 : nodeGroup) {
                    job.add("group", nodeGroup1.getConceptGroup().getIdARk());
                    job.add("group", nodeGroup1.getLexicalValue());
                }
                */
             //   job.add("group", jsonArrayBuilderGroup.build());
                jsonArrayBuilderPath.add(job.build());
            }
            jsonArrayBuilder.add(jsonArrayBuilderPath.build());//.toString());
            i = 0;
        }
    }      
    
    public void getPathWithLabelAsJsonV2(HikariDataSource ds,
            ArrayList<Path> paths, JsonArrayBuilder jsonArrayBuilder,
            String idTheso, String idLang, String selectedIdConcept, String format) {
        String label;
        ConceptHelper conceptHelper = new ConceptHelper();
        GroupHelper groupHelper = new GroupHelper();
        TermHelper termHelper = new TermHelper();
       // ArrayList<NodeGroup> nodeGroup;
        ArrayList<NodeUri> nodeGroup;
        int i = 0;
        
        ArrayList<String> langs;
        if(idLang == null || idLang.isEmpty()) {
            langs = new ThesaurusHelper().getAllUsedLanguagesOfThesaurus(ds, idTheso);
        } else {
            langs = new ArrayList<>();
            langs.add(idLang);
        }
        
        for (Path path1 : paths) {
            JsonArrayBuilder jsonArrayBuilderPath = Json.createArrayBuilder();            
            for (String idConcept : path1.getPath()) {
                JsonObjectBuilder job = Json.createObjectBuilder();
                
                for (String lang : langs) {
                    label = conceptHelper.getLexicalValueOfConcept(ds, idConcept, idTheso, lang);
                    if(label.isEmpty())
                        label = "("+ idConcept+")";
                    job.add("id", idConcept);
                    job.add("arkId", conceptHelper.getIdArkOfConcept(ds, idConcept, idTheso));
                    job.add("label", label);                    
                }

                
                // synonymes
                if(format != null && format.equalsIgnoreCase("full")) {
                    if(i++ == path1.getPath().size() - 1){
                        ArrayList<String> altLabels = termHelper.getNonPreferredTermsLabel(ds, idConcept, idTheso, idLang);
                        JsonArrayBuilder jsonArrayBuilderAltLabels = Json.createArrayBuilder();
                        for (String altLabel : altLabels) {
                            jsonArrayBuilderAltLabels.add(altLabel);
                        }
                        if(jsonArrayBuilderAltLabels != null && !altLabels.isEmpty())
                            job.add("altLabel", jsonArrayBuilderAltLabels.build());  
                    }
                }
                
                // ajout des groups
                // temps 2 secondes
                nodeGroup = groupHelper.getListGroupOfConceptArk(ds, idTheso, idConcept);//ListGroupOfConcept(ds, idTheso, idConcept, idLang);
                JsonArrayBuilder jsonArrayBuilderGroup = Json.createArrayBuilder();
                for (NodeUri nodeUri : nodeGroup) {
                    JsonObjectBuilder jobGroup = Json.createObjectBuilder();
                    jobGroup.add("id", nodeUri.getIdConcept());
                    jobGroup.add("arkId", nodeUri.getIdArk());
                    jobGroup.add("label", groupHelper.getLexicalValueOfGroup(ds, nodeUri.getIdConcept(), idTheso, idLang));
                    jsonArrayBuilderGroup.add(jobGroup.build());
                }                
                
                
                // temps 6 secondes
                /*
                nodeGroup = groupHelper.getListGroupOfConcept(ds, idTheso, idConcept, idLang);
                for (NodeGroup nodeGroup1 : nodeGroup) {
                    job.add("group", nodeGroup1.getConceptGroup().getIdARk());
                    job.add("group", nodeGroup1.getLexicalValue());
                }
                */
                job.add("group", jsonArrayBuilderGroup.build());
                jsonArrayBuilderPath.add(job.build());
            }
            jsonArrayBuilder.add(jsonArrayBuilderPath.build());//.toString());
            i = 0;
        }
    }     
    
    public ArrayList<Path> getPathOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus) {
        message = null;
        ArrayList<Path> allPaths = new ArrayList<>();
        ArrayList<String> path = new ArrayList<>();
        path.add(idConcept);
        
        ArrayList<String> firstPath = new ArrayList<>();
        allPaths = getPathOfConcept(ds, idConcept,
                idThesaurus,
                firstPath,
                path, allPaths);

        return allPaths;
    }
    
    /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param firstPath
     * @param path
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<Path> getPathOfConcept(HikariDataSource ds,
            String idConcept, String idThesaurus,
            ArrayList<String> firstPath,
            ArrayList<String> path,
            ArrayList<Path> allPaths) {

        RelationsHelper relationsHelper = new RelationsHelper();

        ArrayList<String> idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if(idBTs == null) return null;
        if (idBTs.size() > 1) {
          //  Collections.reverse(path);
            if(path.equals(firstPath)){
                message = "!! Attention, erreur de boucle détectée :" + firstPath.toString();
                return allPaths;
            }
            for (String idBT1 : path) {
                if(!firstPath.contains(idBT1))
                    firstPath.add(idBT1);
            }
        }
        if (idBTs.isEmpty()) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (String id2 : firstPath) {
                pathTemp.add(id2);
            }
            for (String id1 : path) {
                if (pathTemp.indexOf(id1) == -1) {
                    pathTemp.add(id1);//(0,id1);
                }
            }
            
            Path path1 = new Path();
            Collections.reverse(pathTemp);
            path1.setPath(pathTemp);
            allPaths.add(path1);
    //        System.out.println("chemin = "  + path.toString());
            path.clear();
        }

        for (String idBT : idBTs) {
            if(!path.contains(idBT)){
                path.add(idBT);
                getPathOfConcept(ds, idBT, idThesaurus, firstPath, path, allPaths);
            } else {
                message = "!! Attention, erreur de boucle détectée, vérifier le concept  :" + idBT;
                message = message + "  ___ et le chemin " + path;
            }
        }
        
        return allPaths;

    }
    
  public List<Path> getPathOfConcept2(HikariDataSource ds, String idConcept, String idTheso) {
        List<ArrayList<String>> allPaths = new ArrayList<>();
        Map<String, List<Link>> links = new HashMap<>();
        List<String> tails = new ArrayList<>();
        depthSearch(ds, idConcept, idTheso, links, tails);
        Queue<String> toProcess = new ArrayDeque<>();

        for (String tail : tails) {
            ArrayList<String> current = new ArrayList<>();
            current.add(tail);
           // if(!allPaths.contains(current)){
                allPaths.add(current);
           // }
            
            if (!toProcess.contains(tail)) {
                toProcess.add(tail);
            }
        }

        while (!toProcess.isEmpty()) {
            String current = toProcess.poll();
            List<String> pattern = new ArrayList<>();
            for (Link link : links.get(current)) {
                pattern.add(link.destination);
                toProcess.add(link.destination);
            }

            if (!pattern.isEmpty()) {
                int currentIndex = 0;
                for (List<String> path : allPaths) {
                    if (path.get(path.size() - 1).equals(current)) {
                        path.add(pattern.get(currentIndex % pattern.size()));
                        currentIndex++;
                    }
                }
            }
        }

        List<Path> paths = new ArrayList<>();
        for (ArrayList<String> currentPath : allPaths) {
            Path current = new Path();
            current.setPath(currentPath);
            if(paths.isEmpty())
                paths.add(current);
            else {
                if(!containThisPath(paths, current)){
                    paths.add(current);
                }
                /*for (Path path : paths) {
                    if(!current.getPath().equals(path.getPath())){
                        paths.add(current);
                    }
                }*/
            }
          /*  if(!paths.contains(current.getPath())){
                paths.add(current);
            }*/
            
        }

        return paths;
    }    
  
    private boolean containThisPath(List<Path> paths, Path currentPath){
        for (Path pathTemp : paths) {
            if(currentPath.getPath().equals(pathTemp.getPath())){
                return true;
            }
        }        
        return false;
    }
    
      private void addLink(String origin, String destination, Map<String, List<Link>> links) {
        if (!links.containsKey(origin)) {
            links.put(origin, new ArrayList<>());
            
        }
        boolean found = false;
            for (Link link : links.get(origin)) {
                if (link.getDestination().equals(destination)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) links.get(origin).add(new Link(origin, destination));
    }

    private void depthSearch(HikariDataSource ds, String idConcept, String idTheso, Map<String, List<Link>> links, List<String> tails) {

        Queue<String> queue = new ArrayDeque<>();

        RelationsHelper relationsHelper = new RelationsHelper();
        queue.add(idConcept);
        links.put(idConcept, new ArrayList<>());

        while (!queue.isEmpty()) {
            String elt = queue.poll();

            List<String> neighbors = relationsHelper.getListIdBT(ds, elt, idTheso);

            if (neighbors.isEmpty()) {
                tails.add(elt);
            } else {
                for (String neighbor : neighbors) {
                    queue.add(neighbor);
                    addLink(neighbor, elt, links);
                }
            }
        }

    }
    
    private class Link {

        public Link(String origin, String destination) {
            this.destination = destination;
            this.origin = origin;
        }
        
        private final String origin;
        private final String destination;

        public String getOrigin() {
            return origin;
        }

        public String getDestination() {
            return destination;
        }

        @Override
        public String toString() {
            return origin + " --> " + destination;
        }
        
    }
  
  
  
  
    
    public ArrayList<Path> getPathOfFacet(HikariDataSource ds,
            String idFacet, String idThesaurus) {

        ArrayList<Path> allPaths = new ArrayList<>();
        ArrayList<String> path = new ArrayList<>();
        path.add(idFacet);
        
        FacetHelper facetHelper = new FacetHelper();
        String idConceptParentOfFacet = facetHelper.getIdConceptParentOfFacet(ds, idFacet, idThesaurus);        
        path.add(0,idConceptParentOfFacet);
        ArrayList<String> firstPath = new ArrayList<>();
        allPaths = getPathOfConcept(ds, idConceptParentOfFacet,
                idThesaurus,
                firstPath,
                path, allPaths);

        return allPaths;
    }    
    
    /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param firstPath
     * @param path
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
  /*  private ArrayList<Path> getPathOfFacet(HikariDataSource ds,
            String idFacet, String idThesaurus,
            ArrayList<String> firstPath,
            ArrayList<String> path,
            ArrayList<Path> allPaths) {

        RelationsHelper relationsHelper = new RelationsHelper();

        FacetHelper facetHelper = new FacetHelper();
        String idFacetParent = facetHelper.getIdParentOfFacet(ds, idFacet, idThesaurus);
       
        ArrayList<String> idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if(idBTs == null) return null;
        if (idBTs.size() > 1) {
            for (String idBT1 : path) {
                firstPath.add(0,idBT1);
            }
        }
        if (idBTs.isEmpty()) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (String id2 : firstPath) {
                pathTemp.add(id2);
            }
            for (String id1 : path) {
                if (pathTemp.indexOf(id1) == -1) {
                    pathTemp.add(0,id1);
                }
            }
            
            Path path1 = new Path();
            path1.setPath(pathTemp);
            allPaths.add(path1);
            path.clear();
        }

        for (String idBT : idBTs) {
            path.add(idBT);
            getPathOfConcept(ds, idBT, idThesaurus, firstPath, path, allPaths);
        }
        
        return allPaths;

    }    */

    /**
     * permet de rtourner le chemin vers le groupe sélectionné
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @return 
     */
    public ArrayList<String> getPathOfGroup(HikariDataSource ds,
            String idGroup, String idThesaurus) {

        ArrayList<String> path = new ArrayList<>();
        path.add(idGroup);
        

        path = getPathOfGroup(ds, idGroup,
                idThesaurus, path);

        return path;
    }    

        /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param ds
     * @param idGroup
     * @param idThesaurus
     * @param path
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<String> getPathOfGroup(HikariDataSource ds,
            String idGroup, String idThesaurus,
            ArrayList<String> path) {

        GroupHelper groupHelper = new GroupHelper();

        String idGroupParent = groupHelper.getIdFather(ds, idGroup, idThesaurus);
        if(idGroupParent == null) return path;

        path.add(0,idGroupParent);
        getPathOfGroup(ds, idGroupParent, idThesaurus, path);
        
        return path;
    }
    
    
    /**
     * Pour tester 
     * #MR
     * 
     */
    /**
     * Fonction récursive pour trouver le chemin complet d'un concept en partant
     * du Concept lui même pour arriver à la tête TT on peut rencontrer
     * plusieurs têtes en remontant, alors on construit à chaque fois un chemin
     * complet.
     *
     * @param ds
     * @param idConcept
     * @param idThesaurus
     * @param firstPath
     * @param path
     * @return Vector Ce vecteur contient tous les Path des BT d'un id_terme
     * exemple (327,368,100,#,2251,5555,54544,8789,#) ici deux path disponible
     * il faut trouver le path qui correspond au microthesaurus en cours pour
     * l'afficher en premier
     */
    private ArrayList<Path> getInvertPathOfConcept2(HikariDataSource ds,
            String idConcept, String idThesaurus,
            ArrayList<String> firstPath,
            ArrayList<String> path,
            ArrayList<Path> allPaths) {

        RelationsHelper relationsHelper = new RelationsHelper();

        ArrayList<String> idBTs = relationsHelper.getListIdBT(ds, idConcept, idThesaurus);
        if(idBTs == null) return null;
        if (idBTs.size() > 1) {
            for (String idBT1 : path) {
                if(!firstPath.contains(idBT1))
                    firstPath.add(idBT1);
            }
        }

        if (idBTs.isEmpty()) {
            ArrayList<String> pathTemp = new ArrayList<>();
            for (String id2 : firstPath) {
                pathTemp.add(id2);
            }
            for (String id1 : path) {
                pathTemp.add(id1);
            }
            
            Path path1 = new Path();
            path1.setPath(pathTemp);
            allPaths.add(path1);
            path.clear();
        }

        for (String idBT : idBTs) {
            path.add(0,idBT);
            getInvertPathOfConcept2(ds, idBT, idThesaurus, firstPath, path, allPaths);
        }
        
        return allPaths;

    }    

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
}
