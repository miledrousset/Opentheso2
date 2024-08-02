package fr.cnrs.opentheso.bdd.datas;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Concept {

    private String idConcept;
    private String idThesaurus;
    private String idArk;
    private String idHandle;
    private String idDoi;    
    private Date created;
    private Date modified;
    private String status;
    private String notation;
    private boolean topConcept;
    private String idGroup;
    private String userName;
    private int idUser;
    private String lang;
    private boolean isDeprecated;
    
    // type de concept (concept, place, people, period ...)
    private String conceptType;
    
    private int creator;
    private int contributor;
    private String creatorName;
    private String contributorName;    
    

    public Concept(String idConcept, String status, String notation, String idThesaurus, String idGroup, boolean topConcept) {
        super();
        this.idConcept = idConcept;
        this.status = status;
        this.notation = notation;
        this.idThesaurus = idThesaurus;
        this.idGroup = idGroup;
        this.topConcept = topConcept;
        this.notation = "";
    }
    
}
