package fr.cnrs.opentheso.ws.openapi.helper;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.ws.api.RestRDFHelper;

import jakarta.ws.rs.core.Response;

import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;

/**
 * Helper permettant de récupérer un concept uniquement avec son ID Ark
 */
public class ConceptHelper {

    public static Response directFetchConcept(String id, String format) {
        RestRDFHelper restRDFHelper = new RestRDFHelper();
        String datas;

        try(HikariDataSource ds = connect()) {
            if (ds == null) return ResponseHelper.errorResponse(Response.Status.SERVICE_UNAVAILABLE, "No connection to server", format);
            datas = restRDFHelper.exportConcept(ds, id, HeaderHelper.removeCharset(format));
            if (datas == null) return ResponseHelper.errorResponse(Response.Status.NOT_FOUND, "Concept not found", format);
            return ResponseHelper.response(Response.Status.OK, datas, format);
        }


    }

}
