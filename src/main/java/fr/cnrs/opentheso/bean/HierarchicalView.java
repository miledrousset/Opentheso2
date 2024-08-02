
package fr.cnrs.opentheso.bean;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.StraightConnector;
import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;

@Named("diagramHierarchicalView")
@RequestScoped
public class HierarchicalView {

    private DefaultDiagramModel model;

    @PostConstruct
    public void init() {
        model = new DefaultDiagramModel();
        model.setMaxConnections(-1);
        model.setConnectionsDetachable(false);

        Element ceo = new Element("top de départ pour la branche", "25em", "6em");
        ceo.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));
        model.addElement(ceo);

        //CFO
        Element cfo = new Element("CFO", "10em", "18em");
        cfo.addEndPoint(createEndPoint(EndPointAnchor.TOP));
        cfo.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));

        Element fin = new Element("FIN", "5em", "30em");
        fin.addEndPoint(createEndPoint(EndPointAnchor.TOP));

        Element pur = new Element("PUR", "20em", "30em");
        pur.addEndPoint(createEndPoint(EndPointAnchor.TOP));

        model.addElement(cfo);
        model.addElement(fin);
        model.addElement(pur);

        //CTO
        Element cto = new Element("CTO", "40em", "18em");
        cto.addEndPoint(createEndPoint(EndPointAnchor.TOP));
        cto.addEndPoint(createEndPoint(EndPointAnchor.BOTTOM));

        Element dev = new Element("DEV", "35em", "30em");
        dev.addEndPoint(createEndPoint(EndPointAnchor.TOP));

        Element tst = new Element("TST", "50em", "30em");
        tst.addEndPoint(createEndPoint(EndPointAnchor.TOP));

        model.addElement(cto);
        model.addElement(dev);
        model.addElement(tst);

        StraightConnector connector = new StraightConnector();
        connector.setPaintStyle("{stroke:'#404a4e', strokeWidth:3}");
        connector.setHoverPaintStyle("{stroke:'#20282b'}");

        //connections
        model.connect(new Connection(ceo.getEndPoints().get(0), cfo.getEndPoints().get(0), connector));
        model.connect(new Connection(ceo.getEndPoints().get(0), cto.getEndPoints().get(0), connector));
        model.connect(new Connection(cfo.getEndPoints().get(1), fin.getEndPoints().get(0), connector));
        model.connect(new Connection(cfo.getEndPoints().get(1), pur.getEndPoints().get(0), connector));
        model.connect(new Connection(cto.getEndPoints().get(1), dev.getEndPoints().get(0), connector));
        model.connect(new Connection(cto.getEndPoints().get(1), tst.getEndPoints().get(0), connector));
    }

    private EndPoint createEndPoint(EndPointAnchor anchor) {
        DotEndPoint endPoint = new DotEndPoint(anchor);
        endPoint.setStyle("{fill:'#404a4e'}");
        endPoint.setHoverStyle("{fill:'#20282b'}");

        return endPoint;
    }

    public DiagramModel getModel() {
        return model;
    }
}