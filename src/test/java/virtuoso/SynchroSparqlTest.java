package virtuoso;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.junit.Ignore;
import org.junit.Test;
/*import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;*/

@Ignore
public class SynchroSparqlTest {

    @Test
    public void virtuosoTest() {

        /*			STEP 1			*/
  /*      VirtGraph set = new VirtGraph ("jdbc:virtuoso://localhost:1111", "dba", "dba");

        /*			STEP 2			*/
    /*    System.out.println("\nexecute: CLEAR GRAPH <th1>");
        VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create("CLEAR GRAPH <th1>", set);
        vur.exec();
*/
        //set.clear();

        //System.out.println("\nexecute: INSERT INTO GRAPH <http://test1> { <aa> <bb> 'cc' . <aa1> <bb1> 123. }");
        //vur = VirtuosoUpdateFactory.create("INSERT INTO GRAPH <http://test1> { <aa> <bb> 'cc' . <aa1> <bb1> 123. }", set);
        //vur.exec();

        /*			STEP 3			*/
        /*		Select all data in virtuoso	*/
  /*      System.out.println("\nexecute: SELECT * FROM <th1> WHERE { ?s ?p ?o }");
        Query sparql = QueryFactory.create("SELECT * FROM <th1> WHERE { ?s ?p ?o }");

        /*			STEP 4			*/
    /*    QueryExecution vqe = VirtuosoQueryExecutionFactory.create (sparql, set);

        ResultSet results = vqe.execSelect();
        while (results.hasNext()) {
            QuerySolution rs = results.nextSolution();
            RDFNode s = rs.get("s");
            RDFNode p = rs.get("p");
            RDFNode o = rs.get("o");
            System.out.println(" { " + s + " " + p + " " + o + " . }");
        }
/*
        System.out.println("\nexecute: DELETE FROM GRAPH <http://test1> { <aa> <bb> 'cc' }");
        vur = VirtuosoUpdateFactory.create("DELETE FROM GRAPH <http://test1> { <aa> <bb> 'cc' }" , set);
        vur.exec();

        System.out.println("\nexecute: SELECT * FROM <http://test1> WHERE { ?s ?p ?o }");
        vqe = VirtuosoQueryExecutionFactory.create (sparql, set);
        results = vqe.execSelect();
        while (results.hasNext()) {
            QuerySolution rs = results.nextSolution();
            RDFNode s = rs.get("s");
            RDFNode p = rs.get("p");
            RDFNode o = rs.get("o");
            System.out.println(" { " + s + " " + p + " " + o + " . }");
        }
*/

    }

}
