
package notes;

import com.zaxxer.hikari.HikariDataSource;
import connexion.ConnexionTest;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

/**
 *
 * @author miledrousset
 */
public class extractValues {
    
    public extractValues() {
    }
    

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() {
        List <NodeIdValue> notes = new ArrayList();
        ConnexionTest connexionTest = new ConnexionTest();
        HikariDataSource ds = connexionTest.getConnexionPool();       
        
        try (Connection conn = ds.getConnection()){
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select identifier, lexicalvalue from note where id_thesaurus = 'th17' and lang = 'fr' and notetypecode = 'editorialNote' and (lexicalvalue iLIKE 'GD%');");
                try(ResultSet resultSet = stmt.getResultSet()){
                    while(resultSet.next()){
                        NodeIdValue nodeIdValue = new NodeIdValue();
                        nodeIdValue.setId(resultSet.getString("identifier"));
                        nodeIdValue.setValue(resultSet.getString("lexicalvalue"));
                        notes.add(nodeIdValue);
                //        System.out.println("note : " + resultSet.getString("lexicalvalue"));
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(extractValues.class.getName()).log(Level.SEVERE, null, ex);
        } 
 //       String phrase = "GD 49 dans la nomenclature numérique des monuments du Guide de Délos édité par l'École française d'Athènes.";

        // Le motif regex pour trouver le premier mot de la forme "GD" suivi d'un espace et d'un ou plusieurs chiffres ou lettres
        String regex = "\\bGD\\s*[A-Za-z0-9.]+\\b";
        
        // Compiler le motif regex
        Pattern pattern = Pattern.compile(regex);
        
        List <NodeIdValue> notes_result = new ArrayList();
        for (NodeIdValue nodeIdValue : notes) {
        // Créer un matcher pour trouver le motif dans la phrase
            Matcher matcher = pattern.matcher(nodeIdValue.getValue());

            // Vérifier si le motif est trouvé
            if (matcher.find()) {
                // Extraire et imprimer le premier mot trouvé
                String foundWord = matcher.group();
              //  notes_result.add(nodeIdValue.getValue());
                System.out.println(nodeIdValue.getId() + ";" + foundWord);
            } else {
                System.out.println(nodeIdValue.getValue());
                System.out.println("Aucun mot correspondant trouvé.");
            }            
        }
        
        

    }
}
