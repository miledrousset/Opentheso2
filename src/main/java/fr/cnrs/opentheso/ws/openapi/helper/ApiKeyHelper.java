/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.cnrs.opentheso.ws.openapi.helper;

import fr.cnrs.opentheso.bdd.helper.ToolsHelper;
import fr.cnrs.opentheso.bdd.tools.MD5Password;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Helper permettant de vérifier l'existence d'une clé API dans la table useres
 */
public class ApiKeyHelper {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ApiKeyHelper.class);
    private static Connection connection = null;
    
    public ApiKeyHelper() {
        if (connection == null) {
            try {
                connection = DataHelper.connect().getConnection();
            } catch (SQLException ex) {
                Logger.getLogger(ApiKeyHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    /**
     * Génère une clé API d'une longueur choisie avec le header voulu
     * @param header le header de la clé API
     * @param keyLength la longueur de la clé API
     * @return la clé API
     */
    public String generateApiKey(String header, int keyLength) {
        final String timestamp = String.valueOf(System.currentTimeMillis());
        final String randomKey = header.length() + timestamp.length() < keyLength ? new ToolsHelper().getNewId(keyLength-header.length()-timestamp.length(), false, false) : "";
        String apiKey= header + timestamp + randomKey;
        if (apiKey.length() > keyLength) {
            apiKey = apiKey.substring(0, keyLength);
        }
        return apiKey;
    }

    /**
     * @param apiKey Clé d'API à sauvegarder
     * @param idUser id de l'utilisateur propriétaire de la clé
     * @return True si la clé a bien été sauvegardé, False sinon
     * @throws SQLException Appelé quand il y a eu une erreur dans l'exécution de la commande SQL
     */
    public boolean saveApiKey(String apiKey, int idUser) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE users SET apikey = ? WHERE id_user = ?")){
            stmt.setString(1, apiKey);
            stmt.setInt(2, idUser);
            int result = stmt.executeUpdate();
            return result > 0;
        }
    }

    /**
     * Renvoie une réponse adaptée si l'état de la clé n'est pas VALID
     * @param state Etat de la clé API
     * @return Response avec le message erreur correspondant
     */
    public Response errorResponse(ApiKeyState state) {
        Status code = null;
        String msg = null;
        switch (state) {
            case EMPTY:
                code = Response.Status.UNAUTHORIZED;
                msg = "No API key given";
                break;
            case DATABASE_UNAVAILABLE:
                code = Response.Status.SERVICE_UNAVAILABLE;
                msg = "Database unavailable";
                break;
            case INVALID:
                code = Response.Status.FORBIDDEN;
                msg = "API key is invalid";
                break;
            case SQL_ERROR:
                code = Response.Status.INTERNAL_SERVER_ERROR;
                msg = "Server internal error";
                break;
            case EXPIRED:
                code = Response.Status.UNAUTHORIZED;
                msg = "API key is expired";
                break;
        }
        
        return ResponseHelper.errorResponse(code, msg, CustomMediaType.APPLICATION_JSON_UTF_8);
    }

    /**
     * Retourne l'id de l'utilisateur propriétaire de la clé
     * @param apiKey
     * @return Optional<Integer> Id utilisateur trouvé ou null sinon
     * @throws SQLException
     */
    public int getIdUser(String apiKey) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id_user, key_expires_at FROM users WHERE apikey =?")) {
            stmt.setString(1, MD5Password.getEncodedPassword(apiKey));
            ResultSet result = stmt.executeQuery();
            if (result.next()) {
                return result.getInt("id_user");
            }

        }
        return -1;
    }

    /**
     * Vérifie si la clé est valide, existe et n'est pas expirée
     * @param apiKey
     * @return ApiKeyState Etat de validation de la clé
     */
    public ApiKeyState checkApiKey(String apiKey){
        if (apiKey == null || apiKey.isEmpty()) return ApiKeyState.EMPTY;
        if (connection == null) return ApiKeyState.DATABASE_UNAVAILABLE;
        try(PreparedStatement stmt = connection.prepareStatement("SELECT key_expires_at, key_never_expire  FROM users WHERE apikey =?")){
            stmt.setString(1, MD5Password.getEncodedPassword(apiKey));
            ResultSet result = stmt.executeQuery();
            if (!result.next()) {return ApiKeyState.INVALID;}
            if (!result.getBoolean("key_never_expire")) {
                if (result.getDate("key_expires_at")==null){return ApiKeyState.INVALID;}
                if (LocalDate.now().isAfter(result.getDate("key_expires_at").toLocalDate())){return ApiKeyState.EXPIRED;}
            }
            return ApiKeyState.VALID;

        } catch (SQLException e){
            Logger.getLogger(ApiKeyHelper.class.getName()).log(Level.SEVERE, null, e);
            return ApiKeyState.SQL_ERROR;
        }
    }

    /**
     * Retourne la date d'expiration de la clé ou lève une exception
     * @param apiKey
     * @return LocalDate Date d'expiration de la clé
     * @throws SQLException
     */
    private LocalDate getApiKeyExpireDate(String apiKey) throws SQLException{
            try(PreparedStatement stmt = connection.prepareStatement("SELECT key_expires_at FROM users WHERE apikey = ?")){
                stmt.setString(1, apiKey);
                ResultSet result = stmt.executeQuery();
                return result.getDate("key_expires_at").toLocalDate();
            }
    }




    
}
