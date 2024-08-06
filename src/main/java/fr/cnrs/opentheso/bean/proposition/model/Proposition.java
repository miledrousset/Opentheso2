package fr.cnrs.opentheso.bean.proposition.model;

import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bean.proposition.NotePropBean;
import fr.cnrs.opentheso.bean.proposition.SynonymPropBean;
import fr.cnrs.opentheso.bean.proposition.TraductionPropBean;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Data
public class Proposition implements Serializable {

    private String conceptID;

    private boolean updateNomConcept;
    private String nomConceptProp;
    private Term nomConcept;

    private List<SynonymPropBean> synonymsProp;
    private List<TraductionPropBean> traductionsProp;

    private List<NotePropBean> note, scopeNotes, historyNotes, examples,
            editorialNotes, definitions, changeNotes;

    
    public Proposition() {
        synonymsProp = new ArrayList<>();
        traductionsProp = new ArrayList<>();
        note = new ArrayList<>();
        scopeNotes = new ArrayList<>();
        historyNotes = new ArrayList<>();
        examples = new ArrayList<>();
        editorialNotes = new ArrayList<>();
        definitions = new ArrayList<>();
        changeNotes = new ArrayList<>();
    }
}
