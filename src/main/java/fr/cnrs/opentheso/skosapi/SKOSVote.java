package fr.cnrs.opentheso.skosapi;

public class SKOSVote {

    String idConcept;
    String idThesaurus;
    String idNote;
    String valueNote;
    int idUser;
    String typeVote;

    public String getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(String idConcept) {
        this.idConcept = idConcept;
    }

    public String getIdThesaurus() {
        return idThesaurus;
    }

    public void setIdThesaurus(String idThesaurus) {
        this.idThesaurus = idThesaurus;
    }

    public String getIdNote() {
        return idNote;
    }

    public void setIdNote(String idNote) {
        this.idNote = idNote;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getTypeVote() {
        return typeVote;
    }

    public void setTypeVote(String typeVote) {
        this.typeVote = typeVote;
    }

    public String getValueNote() {
        return valueNote;
    }

    public void setValueNote(String valueNote) {
        this.valueNote = valueNote;
    }
}