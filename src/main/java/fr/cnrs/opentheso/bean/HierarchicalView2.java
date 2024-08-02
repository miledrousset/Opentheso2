
package fr.cnrs.opentheso.bean;

import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.FlowChartConnector;
import org.primefaces.model.diagram.endpoint.BlankEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.overlay.ArrowOverlay;
import org.primefaces.model.diagram.overlay.LabelOverlay;


@Named("diagramFlowChartView")
@ViewScoped
public class HierarchicalView2 implements Serializable {

    private DefaultDiagramModel model;

    @PostConstruct
    public void init() {
        model = new DefaultDiagramModel();
        model.setMaxConnections(-1);

        FlowChartConnector connector = new FlowChartConnector();
        connector.setPaintStyle("{stroke:'#C7B097',strokeWidth:3}");
        model.setDefaultConnector(connector);

/*        Element start = new Element("Fight for your dream", "20em", "6em");
        start.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        start.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));

        Element trouble = new Element("Do you meet some trouble?", "20em", "18em");
        trouble.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        trouble.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        trouble.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));

        Element giveup = new Element("Do you give up?", "20em", "30em");
        giveup.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        giveup.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        giveup.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));

        Element succeed = new Element("Succeed", "50em", "18em");
        succeed.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        succeed.setStyleClass("ui-diagram-success");

        Element fail = new Element("Fail", "50em", "30em");
        fail.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        fail.setStyleClass("ui-diagram-fail");

        model.addElement(start);
        model.addElement(trouble);
        model.addElement(giveup);
        model.addElement(succeed);
        model.addElement(fail);

        model.connect(createConnection(start.getEndPoints().get(0), trouble.getEndPoints().get(0), null));
        model.connect(createConnection(trouble.getEndPoints().get(1), giveup.getEndPoints().get(0), "Yes"));
        model.connect(createConnection(giveup.getEndPoints().get(1), start.getEndPoints().get(1), "No"));
        model.connect(createConnection(trouble.getEndPoints().get(2), succeed.getEndPoints().get(0), "No"));
        model.connect(createConnection(giveup.getEndPoints().get(2), fail.getEndPoints().get(0), "Yes"));
        */
        
        // les concepts       décalage   gauche  haut    // on décale de 20em de gauche et de 10em en hauteur et 
    /*    Element _4 = new Element("top", "21em", "1em");
        _4.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));         
        _4.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        _4.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));  
        _4.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));          
        
      */  
        // test 
        Element _4 = new Element("top", "21em", "1em");
        _4.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        model.addElement(_4);
        
        
        Element _5 = new Element("n1", "21em", "11em");  
        _5.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));         
        _5.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));   
    //    _5.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));  
    //    _5.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));  
        model.addElement(_5);
    
        model.connect(createConnection(_4.getEndPoints().get(0), _5.getEndPoints().get(0), "NT"));  
        
        Element _7 = new Element("n2", "1em", "21em"); 
        _7.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));         
        _7.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));   
        _7.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));  
        _7.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));            
        
        Element _8 = new Element("n3", "21em", "21em");   
        _8.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));         
        _8.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));   
        _8.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));  
        _8.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));          
        
        Element _19 = new Element("n21", "41em", "21em");
        _19.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));         
        _19.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));   
        _19.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));  
        _19.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));  
        
        Element _9 = new Element("n4", "21em", "31em");  
        _9.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));         
        _9.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));   
        _9.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));  
        _9.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));       

    //    model.addElement(_4);
    //    model.addElement(_5);
        model.addElement(_7);
        model.addElement(_8);
        model.addElement(_19);
        model.addElement(_9);
        
        
        // EndPointAnchor.TOP    = 0        
        // EndPointAnchor.BOTTOM = 1
        // EndPointAnchor.LEFT   = 2
        // EndPointAnchor.RIGHT  = 3    
        
 
        
        model.connect(createConnection(_5.getEndPoints().get(1), _7.getEndPoints().get(0), null));   
        model.connect(createConnection(_5.getEndPoints().get(1), _8.getEndPoints().get(0), null));         
        model.connect(createConnection(_5.getEndPoints().get(1), _19.getEndPoints().get(0), null));         
        
        model.connect(createConnection(_7.getEndPoints().get(3), _8.getEndPoints().get(2), null)); 
        
        model.connect(createConnection(_8.getEndPoints().get(1), _9.getEndPoints().get(0), null));        
        
        
    }

    public DiagramModel getModel() {
        return model;
    }

    private Connection createConnection(EndPoint from, EndPoint to, String label) {
        Connection conn = new Connection(from, to);
        conn.getOverlays().add(new ArrowOverlay(20, 20, 1, 1));

        if (label != null) {
            conn.getOverlays().add(new LabelOverlay(label, "flow-label", 0.5));
        }

        return conn;
    }
}