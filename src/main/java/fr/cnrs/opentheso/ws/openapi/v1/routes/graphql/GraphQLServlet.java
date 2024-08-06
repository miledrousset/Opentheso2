package fr.cnrs.opentheso.ws.openapi.v1.routes.graphql;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.Scalars;
import graphql.schema.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.dao.DaoResourceHelper;
import static fr.cnrs.opentheso.ws.openapi.helper.DataHelper.connect;

@WebServlet(name = "GraphQLServlet", urlPatterns = {"/graphql"})
public class GraphQLServlet extends HttpServlet {
    private final GraphQL graphQL;
    private final Gson gson;
    private HikariDataSource ds;


    // Classe pour avoir la connexion à la BD et exploiter les JSON
    public GraphQLServlet() {
        this.gson = new Gson();
        this.ds = connect();
        GraphQLSchema schema = buildSchema();
        this.graphQL = GraphQL.newGraphQL(schema).build();
    }

    private GraphQLSchema buildSchema() {

        // Type ConceptLabel
        GraphQLObjectType conceptLabelType = GraphQLObjectType.newObject()
                .name("ConceptLabel")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idTerm").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("id").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("label").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idLang").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("codeFlag").type(Scalars.GraphQLString))
                .build();

        // Type ConceptRelation
        GraphQLObjectType conceptRelationType = GraphQLObjectType.newObject()
                .name("ConceptRelation")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("Uri").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("label").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idConcept").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("role").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("status").type(Scalars.GraphQLString))
                .build();

        // Type ConceptNote
        GraphQLObjectType conceptNoteType = GraphQLObjectType.newObject()
                .name("ConceptNote")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idNote").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("idLang").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("label").type(Scalars.GraphQLString))
                .build();

        // Type RessourceGPS
        GraphQLObjectType resourceGPSType = GraphQLObjectType.newObject()
                .name("ResourceGPS")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("latitude").type(Scalars.GraphQLFloat))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("longitude").type(Scalars.GraphQLFloat))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("position").type(Scalars.GraphQLInt))
                .build();

        // Type ConceptImage
        GraphQLObjectType conceptImageType = GraphQLObjectType.newObject()
                .name("ConceptImage")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("id").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("imageName").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("copyRight").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("uri").type(Scalars.GraphQLString))
                .build();

        // Type ConceptIdLabel
        GraphQLObjectType conceptIdLabelType = GraphQLObjectType.newObject()
                .name("ConceptIdLabel")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("uri").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("identifier").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("label").type(Scalars.GraphQLString))
                .build();

        // Type ConceptCustomRelation
        GraphQLObjectType conceptCustomRelationType = GraphQLObjectType.newObject()
                .name("ConceptCustomRelation")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("targetConcept").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("targetLabel").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("relation").type(Scalars.GraphQLString))
                .build();

        // Type NodeFullConcept package : fr.cnrs.opentheso.bdd.helper.dao
        GraphQLObjectType nodeFullConceptType = GraphQLObjectType.newObject()
                .name("NodeFullConcept")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("uri").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("resourceType").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("identifier").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("permanentId").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("notation").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("resourceStatus").type(Scalars.GraphQLInt))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("created").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("modified").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("creatorName").type(Scalars.GraphQLString))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("contributorName").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("prefLabel").type(conceptLabelType))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("altLabels").type(new GraphQLList(conceptLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("hiddenLabels").type(new GraphQLList(conceptLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("prefLabelsTraduction").type(new GraphQLList(conceptLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("altLabelTraduction").type(new GraphQLList(conceptLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("hiddenLabelTraduction").type(new GraphQLList(conceptLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("narrowers").type(new GraphQLList(conceptRelationType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("broaders").type(new GraphQLList(conceptRelationType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("relateds").type(new GraphQLList(conceptRelationType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("notes").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("definitions").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("examples").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("editorialNotes").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("changeNotes").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("scopeNotes").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("historyNotes").type(new GraphQLList(conceptNoteType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("exactMatchs").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("closeMatchs").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("broadMatchs").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("relatedMatchs").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("narrowMatchs").type(new GraphQLList(Scalars.GraphQLString)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("gps").type(new GraphQLList(resourceGPSType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("images").type(new GraphQLList(conceptImageType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("membres").type(new GraphQLList(conceptIdLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("replacedBy").type(new GraphQLList(conceptIdLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("replaces").type(new GraphQLList(conceptIdLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("facets").type(new GraphQLList(conceptIdLabelType)))
                .field(GraphQLFieldDefinition.newFieldDefinition().name("nodeCustomRelations").type(new GraphQLList(conceptCustomRelationType)))
                .build();

        // Type SearchResults pour la recherche de termes
        GraphQLObjectType searchResultsType = GraphQLObjectType.newObject()
                .name("SearchResults")
                .field(GraphQLFieldDefinition.newFieldDefinition().name("results").type(new GraphQLList(nodeFullConceptType)))
                .build();


        // Déclaration du type Query pour les requêtes
        GraphQLObjectType queryType;
        queryType = GraphQLObjectType.newObject()
                .name("Query")
                .field(GraphQLFieldDefinition.newFieldDefinition() // Query pour récupérer un concept
                        .name("fullConcept")
                        .type(nodeFullConceptType)
                        .argument(GraphQLArgument.newArgument().name("idTheso").type(Scalars.GraphQLString))
                        .argument(GraphQLArgument.newArgument().name("idConcept").type(Scalars.GraphQLString))
                        .argument(GraphQLArgument.newArgument().name("idLang").type(Scalars.GraphQLString))
                        .dataFetcher(env -> {
                            String idTheso = env.getArgument("idTheso");
                            String idConcept = env.getArgument("idConcept");
                            String idLang = env.getArgument("idLang");
                            DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
                            return daoResourceHelper.getFullConcept(ds, idTheso, idConcept, idLang, -1, -1);
                        })
                )
                .field(GraphQLFieldDefinition.newFieldDefinition() // Query pour la recherche de termes
                        .name("searchConcepts")
                        .type(new GraphQLList(nodeFullConceptType))
                        .argument(GraphQLArgument.newArgument().name("value").type(Scalars.GraphQLString))
                        .argument(GraphQLArgument.newArgument().name("idLang").type(Scalars.GraphQLString))
                        .argument(GraphQLArgument.newArgument().name("idGroups").type(new GraphQLList(Scalars.GraphQLString)))
                        .argument(GraphQLArgument.newArgument().name("idTheso").type(Scalars.GraphQLString))
                        .dataFetcher(env -> {
                            String value = env.getArgument("value");
                            String idLang = env.getArgument("idLang");
                            List<String> idGroupsList = env.getArgument("idGroups");
                            String idTheso = env.getArgument("idTheso");
                            String[] idGroups = idGroupsList != null ? idGroupsList.toArray(new String[0]) : null;
                            SearchHelper searchHelper = new SearchHelper();
                            List<String> autoCompletions = searchHelper.searchAutoCompletionWSForWidget(ds, value, idLang, idGroups, idTheso);
                            DaoResourceHelper daoResourceHelper = new DaoResourceHelper();
                            List<Object> fullConcepts = new ArrayList<>();
                            for (String autoCompletion : autoCompletions) {
                                fullConcepts.add(daoResourceHelper.getFullConcept(ds, idTheso, autoCompletion, idLang, -1, -1));
                            }
                            return(fullConcepts);
                        })
                )
                .build();

        return GraphQLSchema.newSchema()
                .query(queryType)
                .build();
    }



    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Lecture et parsing du JSON pour récupérer la query
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }

        // On converti le JSON en Map pour exploiter comme il faut les données
        Map<String, Object> requestMap = gson.fromJson(stringBuilder.toString(), Map.class);
        String query = (String) requestMap.get("query");

        // On exécute la requête
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(query)
                .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        // On converti le résultat en JSON
        String jsonResponse = gson.toJson(executionResult.toSpecification());

        // On renvoie la réponse en JSON
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(jsonResponse);
    }

}