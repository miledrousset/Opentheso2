package fr.cnrs.opentheso.bean.proposition.dao;

import fr.cnrs.opentheso.bdd.helper.nodes.term.NodeTermTraduction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TraductionPropBean extends NodeTermTraduction {
    
    private String oldValue;
    private String idTerm;
    
    private boolean toAdd;
    private boolean toRemove;
    private boolean toUpdate;
    
}
