/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bdd.helper;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import jakarta.faces.model.SelectItem;
import fr.cnrs.opentheso.bdd.datas.Languages_iso639;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author miled.rousset
 */
public class LanguageHelper {

    private final Log log = LogFactory.getLog(ThesaurusHelper.class);
    
    
    public String normalizeIdLang(String idLang){
        switch (idLang) {
            case "en-GB":
                return "en";
            case "en-US":
                return "en";   
            case "pt-BR":
                return "pt";     
            case "pt-PT":
                return "pt";                  
        }
        return idLang;
    }    
    
    /**
     * Permet de retourner le code du drapeau d'un pays à partir du code de la langue
     *
     * @param ds le pool de connexion
     * @param idLang
     * @return Objet Class Thesaurus
     */
    public String getFlagFromIdLang(HikariDataSource ds, String idLang) {
        String flag = "";
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select code_pays from languages_iso639 where iso639_1 = '" + idLang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet.next()) {
                        flag = resultSet.getString("code_pays");
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting Flag : " + idLang, sqle);
        }
        return flag;
    }    
    
    /**
     * Permet de remplir un tableau de SelectItem avec l'intégralité des langues
     * ayant un id_1 non null
     *
     * @param ds le pool de connexion
     * @return le tableau de SelectItem plein
     */
    public SelectItem[] getSelectItemLanguages(HikariDataSource ds) {
        SelectItem[] langues;
        ArrayList<Languages_iso639> arrayLangue1 = (getAllLanguages(ds));
        ArrayList<Languages_iso639> arrayLangue2 = new ArrayList<>();
        for (Languages_iso639 l : arrayLangue1) {
            if (l.getId_iso639_1() != null) {
                arrayLangue2.add(l);
            }
        }
        langues = new SelectItem[arrayLangue2.size()];
        int i = 0;
        for (Languages_iso639 l : arrayLangue2) {
            langues[i] = new SelectItem(l.getId_iso639_1(), l.getFrench_name() + " (" + l.getId_iso639_1().trim() + ")");
            i++;
        }
        return langues;
    }

    /**
     * Permet de remplir un tableau de SelectItem avec les langues d'un
     * thesaurus
     *
     * @param ds le pool de connexion
     * @param idTheso l'identifiant du thesaurus
     * @param idLangue la langue du thesaurus
     * @return le tableau de SelectItem plein
     */
    public SelectItem[] getSelectItemLanguagesOneThesaurus(HikariDataSource ds, String idTheso, String idLangue) {
        SelectItem[] langues;
        ArrayList<Languages_iso639> arrayLangue1 = (getLanguagesOfThesaurus(ds, idTheso));
        if (idLangue.equals("")) {
            langues = new SelectItem[arrayLangue1.size()];
        } else {
            langues = new SelectItem[arrayLangue1.size() - 1];
        }
        int i = 0;
        for (Languages_iso639 l : arrayLangue1) {
            if (!(l.getId_iso639_1().trim()).equals(idLangue)) {
                langues[i] = new SelectItem(l.getId_iso639_1(), l.getFrench_name() + " (" + l.getId_iso639_1().trim() + ")");
                i++;
            }
        }
        return langues;
    }

    /**
     * Permet de retourner un objet Langauge par identifiant / ou null si rien
     *
     * @param ds le pool de connexion
     * @param idLang
     * @return Objet Class Thesaurus
     */
    public Languages_iso639 getThisLanguage(HikariDataSource ds, String idLang) {
        Languages_iso639 language = null;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select * from languages_iso639 where iso639_1 = '" + idLang + "'");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        resultSet.next();
                        language = new Languages_iso639();
                        language.setId_iso639_1(idLang);
                        language.setId_iso639_2(resultSet.getString("iso639_2"));
                        language.setFrench_name(resultSet.getString("french_name"));
                        language.setEnglish_name(resultSet.getString("english_name"));
                        language.setCodePays(resultSet.getString("code_pays"));
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while adding element : " + language, sqle);
        }
        return language;
    }

    public List<Languages_iso639> getLanguagesByProject(HikariDataSource ds, String idProject) {

        List<Languages_iso639> language = null;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select lang.* from languages_iso639 lang, project_description pro " +
                        "where lang.iso639_1 = pro.lang and pro.id_group = '"+idProject+"'");
                try ( ResultSet resultSet = stmt.getResultSet()) {

                    if (resultSet != null) {
                        language = new ArrayList<>();
                        while (resultSet.next()) {
                            Languages_iso639 languageTmp = new Languages_iso639();
                            languageTmp.setId_iso639_1(resultSet.getString("iso639_1").trim());
                            languageTmp.setId_iso639_2(resultSet.getString("iso639_2").trim());
                            languageTmp.setFrench_name(resultSet.getString("french_name"));
                            languageTmp.setEnglish_name(resultSet.getString("english_name"));
                            languageTmp.setCodePays(resultSet.getString("code_pays"));
                            language.add(languageTmp);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting languages of project : " + idProject, sqle);
        }
        return language ;
    }

    /**
     * Permet de retourner un ArrayList d'objets Language_iso639 c'est la liste
     * des langues utilisées par un thésaurus / ou null si rien
     *
     * @param ds le pool de connexion
     * @param idThesaurus
     * @return Objet Class Thesaurus
     */
    public ArrayList<Languages_iso639> getLanguagesOfThesaurus(HikariDataSource ds, String idThesaurus) {

        ArrayList<Languages_iso639> language = null;
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select distinct languages_iso639.iso639_1,"
                        + " languages_iso639.iso639_2, languages_iso639.english_name,"
                        + " languages_iso639.french_name, languages_iso639.code_pays"
                        + " from languages_iso639, thesaurus_label where thesaurus_label.lang = languages_iso639.iso639_1 "
                        + " and thesaurus_label.id_thesaurus ='" + idThesaurus + "';");
                try ( ResultSet resultSet = stmt.getResultSet()) {

                    if (resultSet != null) {
                        language = new ArrayList<>();
                        while (resultSet.next()) {
                            Languages_iso639 languageTmp = new Languages_iso639();
                            languageTmp.setId_iso639_1(resultSet.getString("iso639_1").trim());
                            languageTmp.setId_iso639_2(resultSet.getString("iso639_2").trim());
                            languageTmp.setFrench_name(resultSet.getString("french_name"));
                            languageTmp.setEnglish_name(resultSet.getString("english_name"));
                            languageTmp.setCodePays(resultSet.getString("code_pays"));
                            language.add(languageTmp);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while getting languages of thésaurus : " + idThesaurus, sqle);
    }
    return language ;
}

/**
 * Permet de retourner un ArrayList d'Objet Languages_iso639 de toute la table
 * Language_iso639 c'est la liste des langues ISO639 / ou null si rien
 *
 * @param ds le pool de connexion
 * @return Objet Class Thesaurus
 */
public ArrayList<Languages_iso639> getAllLanguages(HikariDataSource ds) {

        ArrayList<Languages_iso639> language = new ArrayList<>();
        try ( Connection conn = ds.getConnection()) {
            try ( Statement stmt = conn.createStatement()) {
                stmt.executeQuery("select distinct * from languages_iso639 ORDER BY iso639_1");
                try ( ResultSet resultSet = stmt.getResultSet()) {
                    if (resultSet != null) {
                        language = new ArrayList<>();
                        while (resultSet.next()) {
                            Languages_iso639 languageTmp = new Languages_iso639();
                            languageTmp.setId_iso639_1(resultSet.getString("iso639_1"));
                            languageTmp.setId_iso639_2(resultSet.getString("iso639_2"));
                            languageTmp.setFrench_name(resultSet.getString("french_name"));
                            languageTmp.setEnglish_name(resultSet.getString("english_name"));
                            languageTmp.setCodePays(resultSet.getString("code_pays"));
                            language.add(languageTmp);
                        }
                    }
                }
            }
        } catch (SQLException sqle) {
            log.error("Error while adding element : " + language, sqle);
        }
        return language;
    }

}
