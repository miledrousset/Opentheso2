/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.search;

import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import jakarta.inject.Named;
//import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import jakarta.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "testMemory")
//@SessionScoped
public class TestMemory implements Serializable {
    @Inject private SearchBean searchBean;
    @Inject private ConceptView conceptView;    
    
    public TestMemory() {
    }
    public void testSearch(){
        for (int i = 0; i < 1000; i++) {
     //       searchBean.testMemory();
        }
        System.out.println("traitement terminé");
    }

    public void testGetConcept(){
        for (int i = 0; i < 100; i++) {
            conceptView.getConcept("TH_1", "300", "fr");
        }
        System.out.println("traitement terminé");
    }    
}
