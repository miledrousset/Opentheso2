DROP FUNCTION IF EXISTS opentheso_get_gps;
DROP FUNCTION IF EXISTS opentheso_get_concepts;
DROP FUNCTION IF EXISTS opentheso_get_concepts_by_group;
DROP FUNCTION IF EXISTS opentheso_add_new_concept;
DROP FUNCTION IF EXISTS opentheso_add_gps;

-- FUNCTION: public.opentheso_get_gps(character varying, character varying)

-- DROP FUNCTION public.opentheso_get_gps(character varying, character varying);

CREATE OR REPLACE FUNCTION public.opentheso_get_gps(
    id_thesorus character varying,
    id_con character varying)
    RETURNS TABLE(gps_latitude double precision, gps_longitude double precision)
    LANGUAGE 'plpgsql'
    COST 100
    VOLATILE PARALLEL UNSAFE
    ROWS 1000

AS $BODY$
begin
    return query
        SELECT latitude, longitude
        FROM gps
        WHERE id_theso = id_thesorus
          AND id_concept = id_con;

end;
$BODY$;

CREATE OR REPLACE FUNCTION opentheso_get_concepts_by_group (id_theso VARCHAR, path VARCHAR, id_group VARCHAR)
	RETURNS SETOF RECORD
	LANGUAGE plpgsql
AS $$
DECLARE

seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';

	rec record;
	con record;
	theso_rec record;
	traduction_rec record;
	altLab_rec record;
	altLab_hiden_rec record;
	geo_rec record;
	group_rec record;
	note_concept_rec record;
	note_term_rec record;
	relation_rec record;
	alignement_rec record;
	img_rec record;
	vote_record record;
	message_record record;
	cadidat_record record;
	replace_rec record;
	replacedBy_rec record;
	facet_rec record;
    externalResource_rec record;

	tmp text;
	uri text;
	local_URI text;
	prefLab VARCHAR;
	altLab VARCHAR;
	altLab_hiden VARCHAR;
	membre text;
	definition text;
	secopeNote text;
	note text;
	historyNote text;
	example text;
	changeNote text;
	editorialNote text;
	narrower text;
	broader text;
	related text;
	exactMatch text;
	closeMatch text;
	broadMatch text;
	relatedMatch text;
	narrowMatch text;
	img text;
	creator text;
	contributor text;
	replacedBy text;
	replaces text;
	facets text;
    externalResources text;
    gpsData text;

BEGIN

    SELECT * INTO theso_rec FROM preferences where id_thesaurus = id_theso;


    FOR con IN SELECT concept.* FROM concept, concept_group_concept WHERE concept.id_concept = concept_group_concept.idconcept
                                                                  AND concept.id_thesaurus = concept_group_concept.idthesaurus AND concept.id_thesaurus = id_theso
                                                                  AND concept_group_concept.idgroup = id_group AND concept.status != 'CA'
        LOOP
		-- URI
		uri = opentheso_get_uri(theso_rec.original_uri_is_ark, con.id_ark, theso_rec.original_uri, theso_rec.original_uri_is_handle,
					 con.id_handle, theso_rec.original_uri_is_doi, con.id_doi, con.id_concept, id_theso, path);

        -- LocalUri
        local_URI = path || '/?idc=' || con.id_concept || '&idt=' || id_theso;
                prefLab = '';

		-- PrefLab
        FOR traduction_rec IN SELECT * FROM opentheso_get_traductions(id_theso, con.id_concept)
        LOOP
            prefLab = prefLab || traduction_rec.term_lexical_value || sous_seperateur || traduction_rec.term_lang || seperateur;
        END LOOP;

		-- altLab
		altLab = '';
        FOR altLab_rec IN SELECT * FROM opentheso_get_alter_term(id_theso, con.id_concept, false)
        LOOP
            altLab = altLab || altLab_rec.alter_term_lexical_value || sous_seperateur || altLab_rec.alter_term_lang || seperateur;
        END LOOP;

		-- altLab hiden
		altLab_hiden = '';
        FOR altLab_hiden_rec IN SELECT * FROM opentheso_get_alter_term(id_theso, con.id_concept, true)
        LOOP
            altLab_hiden = altLab_hiden || altLab_hiden_rec.alter_term_lexical_value || sous_seperateur || altLab_hiden_rec.alter_term_lang || seperateur;
        END LOOP;

		-- Notes
		note = '';
		example = '';
		changeNote = '';
		secopeNote = '';
		definition = '';
		historyNote = '';
		editorialNote = '';
        FOR note_concept_rec IN SELECT * FROM opentheso_get_note_concept(id_theso, con.id_concept)
        LOOP
            IF (note_concept_rec.note_notetypecode = 'note') THEN
				note = note || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
            ELSIF (note_concept_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'example') THEN
				example = example || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
            END IF;
        END LOOP;

        FOR note_term_rec IN SELECT * FROM opentheso_get_note_term(id_theso, con.id_concept)
        LOOP
            IF (note_term_rec.note_notetypecode = 'note') THEN
				note = note || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
            ELSIF (note_term_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'example') THEN
				example = example || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
            END IF;
        END LOOP;

		-- Narrower
		narrower = '';
		broader = '';
		related = '';
        FOR relation_rec IN SELECT * FROM opentheso_get_relations(id_theso, con.id_concept)
        LOOP
            tmp = opentheso_get_uri(theso_rec.original_uri_is_ark, relation_rec.relationship_id_ark, theso_rec.original_uri,
                            theso_rec.original_uri_is_handle, relation_rec.relationship_id_handle, theso_rec.original_uri_is_doi,
                            relation_rec.relationship_id_doi, relation_rec.relationship_id_concept, id_theso, path)
                            || sous_seperateur || relation_rec.relationship_role || sous_seperateur || relation_rec.relationship_id_concept || sous_seperateur ;

            IF (relation_rec.relationship_role = 'NT' OR relation_rec.relationship_role = 'NTP' OR relation_rec.relationship_role = 'NTI'
                    OR relation_rec.relationship_role = 'NTG') THEN
                narrower = narrower || tmp || seperateur;
            ELSIF (relation_rec.relationship_role = 'BT' OR relation_rec.relationship_role = 'BTP' OR relation_rec.relationship_role = 'BTI'
                    OR relation_rec.relationship_role = 'BTG') THEN
                broader = broader || tmp || seperateur;
            ELSIF (relation_rec.relationship_role = 'RT' OR relation_rec.relationship_role = 'RHP' OR relation_rec.relationship_role = 'RPO') THEN
                related = related || tmp || seperateur;
            END IF;
        END LOOP;

		-- Alignement
		exactMatch = '';
		closeMatch = '';
		broadMatch = '';
		relatedMatch = '';
		narrowMatch = '';
        FOR alignement_rec IN SELECT * FROM opentheso_get_alignements(id_theso, con.id_concept)
        LOOP
            if (alignement_rec.alig_id_type = 1) THEN
                        exactMatch = exactMatch || alignement_rec.alig_uri_target || seperateur;
            ELSEIF (alignement_rec.alig_id_type = 2) THEN
                        closeMatch = closeMatch || alignement_rec.alig_uri_target || seperateur;
            ELSEIF (alignement_rec.alig_id_type = 3) THEN
                        broadMatch = broadMatch || alignement_rec.alig_uri_target || seperateur;
            ELSEIF (alignement_rec.alig_id_type = 4) THEN
                        relatedMatch = relatedMatch || alignement_rec.alig_uri_target || seperateur;
            ELSEIF (alignement_rec.alig_id_type = 5) THEN
                        narrowMatch = narrowMatch || alignement_rec.alig_uri_target || seperateur;
            END IF;
        END LOOP;

		-- geo:alt && geo:long
        gpsData = '';
        FOR geo_rec IN SELECT * FROM opentheso_get_gps(id_theso, con.id_concept)
        LOOP
            gpsData = gpsData || geo_rec.gps_latitude || sous_seperateur || geo_rec.gps_longitude || seperateur;
        END LOOP;

        -- membre
        membre = '';
        FOR group_rec IN SELECT * FROM opentheso_get_groups(id_theso, con.id_concept)
        LOOP
            IF (theso_rec.original_uri_is_ark = true AND group_rec.group_id_ark IS NOT NULL  AND group_rec.group_id_ark != '') THEN
                        membre = membre || theso_rec.original_uri || '/' || group_rec.group_id_ark || seperateur;
            ELSIF (theso_rec.original_uri_is_ark = true AND (group_rec.group_id_ark IS NULL OR group_rec.group_id_ark = '')) THEN
                        membre = membre || path || '/?idg=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
            ELSIF (group_rec.group_id_handle IS NOT NULL) THEN
                        membre = membre || 'https://hdl.handle.net/' || group_rec.group_id_handle || seperateur;
            ELSIF (theso_rec.original_uri IS NOT NULL) THEN
                        membre = membre || theso_rec.original_uri || '/?idg=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
            ELSE
                        membre = membre || path || '/?idg=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
            END IF;
        END LOOP;

		-- Images
		img = '';
        FOR img_rec IN SELECT * FROM opentheso_get_images(id_theso, con.id_concept)
        LOOP
            img = img || img_rec.name || sous_seperateur || img_rec.copyright || sous_seperateur || img_rec.url || seperateur;
        END LOOP;

        SELECT username INTO creator FROM users WHERE id_user = con.creator;
        SELECT username INTO contributor FROM users WHERE id_user = con.contributor;

        replaces = '';
        FOR replace_rec IN SELECT id_concept1, id_ark, id_handle, id_doi
            FROM concept_replacedby, concept
            WHERE concept.id_concept = concept_replacedby.id_concept1
            AND concept.id_thesaurus = concept_replacedby.id_thesaurus
            AND concept_replacedby.id_concept1 = con.id_concept
            AND concept_replacedby.id_thesaurus = id_theso
        LOOP
			replaces = replaces || opentheso_get_uri(theso_rec.original_uri_is_ark, replace_rec.id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, replace_rec.id_handle, theso_rec.original_uri_is_doi,
					replace_rec.id_doi, replace_rec.id_concept1, id_theso, path) || seperateur;
        END LOOP;

		replacedBy = '';
        FOR replacedBy_rec IN SELECT id_concept2, id_ark, id_handle, id_doi
            FROM concept_replacedby, concept
            WHERE concept.id_concept = concept_replacedby.id_concept2
            AND concept.id_thesaurus = concept_replacedby.id_thesaurus
            AND concept_replacedby.id_concept2 = con.id_concept
            AND concept_replacedby.id_thesaurus = id_theso
        LOOP
			replacedBy = replacedBy || opentheso_get_uri(theso_rec.original_uri_is_ark, replacedBy_rec.id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, replacedBy_rec.id_handle, theso_rec.original_uri_is_doi,
					replacedBy_rec.id_doi, replacedBy_rec.id_concept2, id_theso, path) || seperateur;
        END LOOP;

		facets = '';
        FOR facet_rec IN SELECT thesaurus_array.id_facet
            FROM thesaurus_array
            WHERE thesaurus_array.id_thesaurus = id_theso
                   AND thesaurus_array.id_concept_parent = con.id_concept
                     LOOP
			facets = facets || facet_rec.id_facet || seperateur;
        END LOOP;

		externalResources = '';
        FOR externalResource_rec IN SELECT external_resources.external_uri
            FROM external_resources
            WHERE external_resources.id_thesaurus = id_theso
            AND external_resources.id_concept = con.id_concept
        LOOP
			externalResources = externalResources || externalResource_rec.external_uri || seperateur;
        END LOOP;

        SELECT 	uri, con.status, local_URI, con.id_concept, con.id_ark, prefLab, altLab, altLab_hiden, definition, example,
          editorialNote, changeNote, secopeNote, note, historyNote, con.notation, narrower, broader, related, exactMatch, closeMatch,
          broadMatch, relatedMatch, narrowMatch, gpsData, membre, con.created, con.modified, img,
          creator, contributor, replaces, replacedBy, facets, externalResources INTO rec;

        RETURN NEXT rec;
    END LOOP;
END;
$$;


CREATE OR REPLACE FUNCTION opentheso_get_concepts (id_theso VARCHAR, path VARCHAR)
	RETURNS SETOF RECORD
	LANGUAGE plpgsql
AS $$
DECLARE

    seperateur constant varchar := '##';
	sous_seperateur constant varchar := '@@';

	rec record;
	con record;
	theso_rec record;
	traduction_rec record;
	altLab_rec record;
	altLab_hiden_rec record;
	geo_rec record;
	group_rec record;
	note_concept_rec record;
	note_term_rec record;
	relation_rec record;
	alignement_rec record;
	img_rec record;
	vote_record record;
	message_record record;
	cadidat_record record;
	replace_rec record;
	replacedBy_rec record;
	facet_rec record;
    externalResource_rec record;

	tmp text;
	uri text;
	local_URI text;
	prefLab VARCHAR;
	altLab VARCHAR;
	altLab_hiden VARCHAR;
	membre text;
	definition text;
	secopeNote text;
	note text;
	historyNote text;
	example text;
	changeNote text;
	editorialNote text;
	narrower text;
	broader text;
	related text;
	exactMatch text;
	closeMatch text;
	broadMatch text;
	relatedMatch text;
	narrowMatch text;
	img text;
	creator text;
	contributor text;
	replaces text;
	replacedBy text;
	facets text;
    externalResources text;
    gpsData text;
BEGIN

    SELECT * INTO theso_rec FROM preferences where id_thesaurus = id_theso;

    FOR con IN SELECT * FROM concept WHERE id_thesaurus = id_theso AND status != 'CA'
    LOOP
		-- URI
		uri = opentheso_get_uri(theso_rec.original_uri_is_ark, con.id_ark, theso_rec.original_uri, theso_rec.original_uri_is_handle,
					 con.id_handle, theso_rec.original_uri_is_doi, con.id_doi, con.id_concept, id_theso, path);

        -- LocalUri
        local_URI = path || '/?idc=' || con.id_concept || '&idt=' || id_theso;

        -- PrefLab
        prefLab = '';
        FOR traduction_rec IN SELECT * FROM opentheso_get_traductions(id_theso, con.id_concept)
        LOOP
            prefLab = prefLab || traduction_rec.term_lexical_value || sous_seperateur || traduction_rec.term_lang || seperateur;
        END LOOP;

		-- altLab
		altLab = '';
        FOR altLab_rec IN SELECT * FROM opentheso_get_alter_term(id_theso, con.id_concept, false)
                                            LOOP
            altLab = altLab || altLab_rec.alter_term_lexical_value || sous_seperateur || altLab_rec.alter_term_lang || seperateur;
        END LOOP;

		-- altLab hiden
		altLab_hiden = '';
        FOR altLab_hiden_rec IN SELECT * FROM opentheso_get_alter_term(id_theso, con.id_concept, true)
                                                  LOOP
            altLab_hiden = altLab_hiden || altLab_hiden_rec.alter_term_lexical_value || sous_seperateur || altLab_hiden_rec.alter_term_lang || seperateur;
        END LOOP;

		-- Notes
		note = '';
		example = '';
		changeNote = '';
		secopeNote = '';
		definition = '';
		historyNote = '';
		editorialNote = '';
        FOR note_concept_rec IN SELECT * FROM opentheso_get_note_concept(id_theso, con.id_concept)
        LOOP
            IF (note_concept_rec.note_notetypecode = 'note') THEN
				note = note || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
            ELSIF (note_concept_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'example') THEN
				example = example || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
			ELSIF (note_concept_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_concept_rec.note_lexicalvalue || sous_seperateur || note_concept_rec.note_lang || seperateur;
            END IF;
        END LOOP;

        FOR note_term_rec IN SELECT * FROM opentheso_get_note_term(id_theso, con.id_concept)
        LOOP
            IF (note_term_rec.note_notetypecode = 'note') THEN
				note = note || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
            ELSIF (note_term_rec.note_notetypecode = 'scopeNote') THEN
				secopeNote = secopeNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'historyNote') THEN
				historyNote = historyNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'definition') THEN
				definition = definition || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'example') THEN
				example = example || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'changeNote') THEN
				changeNote = changeNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
			ELSIF (note_term_rec.note_notetypecode = 'editorialNote') THEN
				editorialNote = editorialNote || note_term_rec.note_lexicalvalue || sous_seperateur || note_term_rec.note_lang || seperateur;
            END IF;
        END LOOP;

		-- Narrower
		narrower = '';
		broader = '';
		related = '';
        FOR relation_rec IN SELECT * FROM opentheso_get_relations(id_theso, con.id_concept)
        LOOP
            tmp = opentheso_get_uri(theso_rec.original_uri_is_ark, relation_rec.relationship_id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, relation_rec.relationship_id_handle, theso_rec.original_uri_is_doi,
					relation_rec.relationship_id_doi, relation_rec.relationship_id_concept, id_theso, path)
					|| sous_seperateur || relation_rec.relationship_role || sous_seperateur || relation_rec.relationship_id_concept || sous_seperateur ;

            IF (relation_rec.relationship_role = 'NT' OR relation_rec.relationship_role = 'NTP' OR relation_rec.relationship_role = 'NTI'
					OR relation_rec.relationship_role = 'NTG') THEN
				narrower = narrower || tmp || seperateur;
			ELSIF (relation_rec.relationship_role = 'BT' OR relation_rec.relationship_role = 'BTP' OR relation_rec.relationship_role = 'BTI'
					OR relation_rec.relationship_role = 'BTG') THEN
				broader = broader || tmp || seperateur;
			ELSIF (relation_rec.relationship_role = 'RT' OR relation_rec.relationship_role = 'RHP' OR relation_rec.relationship_role = 'RPO') THEN
				related = related || tmp || seperateur;
            END IF;
        END LOOP;

		-- Alignement
		exactMatch = '';
		closeMatch = '';
		broadMatch = '';
		relatedMatch = '';
		narrowMatch = '';
        FOR alignement_rec IN SELECT * FROM opentheso_get_alignements(id_theso, con.id_concept)
                                        LOOP
            if (alignement_rec.alig_id_type = 1) THEN
				exactMatch = exactMatch || alignement_rec.alig_uri_target || seperateur;
            ELSEIF (alignement_rec.alig_id_type = 2) THEN
				closeMatch = closeMatch || alignement_rec.alig_uri_target || seperateur;
		    ELSEIF (alignement_rec.alig_id_type = 3) THEN
				broadMatch = broadMatch || alignement_rec.alig_uri_target || seperateur;
			ELSEIF (alignement_rec.alig_id_type = 4) THEN
				relatedMatch = relatedMatch || alignement_rec.alig_uri_target || seperateur;
			ELSEIF (alignement_rec.alig_id_type = 5) THEN
				narrowMatch = narrowMatch || alignement_rec.alig_uri_target || seperateur;
            END IF;
        END LOOP;

		-- geo:alt && geo:long
        gpsData = '';
        FOR geo_rec IN SELECT * FROM opentheso_get_gps(id_theso, con.id_concept)
                                         LOOP
            gpsData = gpsData || geo_rec.gps_latitude || sous_seperateur || geo_rec.gps_longitude || seperateur;
        END LOOP;

        -- membre
        membre = '';
        FOR group_rec IN SELECT * FROM opentheso_get_groups(id_theso, con.id_concept)
        LOOP
            IF (theso_rec.original_uri_is_ark = true AND group_rec.group_id_ark IS NOT NULL  AND group_rec.group_id_ark != '') THEN
				membre = membre || theso_rec.original_uri || '/' || group_rec.group_id_ark || seperateur;
            ELSIF (theso_rec.original_uri_is_ark = true AND (group_rec.group_id_ark IS NULL OR group_rec.group_id_ark = '')) THEN
				membre = membre || path || '/?idg=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
			ELSIF (group_rec.group_id_handle IS NOT NULL AND group_rec.group_id_handle != '') THEN
				membre = membre || 'https://hdl.handle.net/' || group_rec.group_id_handle || seperateur;
			ELSIF (theso_rec.original_uri IS NOT NULL AND theso_rec.original_uri != '') THEN
				membre = membre || theso_rec.original_uri || '/?idg=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
            ELSE
				membre = membre || path || '/?idc=' || group_rec.group_id || '&idt=' || id_theso || seperateur;
            END IF;
        END LOOP;

		-- Images
		img = '';
        FOR img_rec IN SELECT * FROM opentheso_get_images(id_theso, con.id_concept)
        LOOP
            img = img || img_rec.name || sous_seperateur || img_rec.copyright || sous_seperateur || img_rec.url || seperateur;
        END LOOP;

        SELECT username INTO creator FROM users WHERE id_user = con.creator;
        SELECT username INTO contributor FROM users WHERE id_user = con.contributor;

        replaces = '';
        FOR replace_rec IN SELECT id_concept1, id_ark, id_handle, id_doi
            FROM concept_replacedby, concept
            WHERE concept.id_concept = concept_replacedby.id_concept1
                     AND concept.id_thesaurus = concept_replacedby.id_thesaurus
                     AND concept_replacedby.id_concept2 = con.id_concept
                     AND concept_replacedby.id_thesaurus = id_theso
                       LOOP
			replaces = replaces || opentheso_get_uri(theso_rec.original_uri_is_ark, replace_rec.id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, replace_rec.id_handle, theso_rec.original_uri_is_doi,
					replace_rec.id_doi, replace_rec.id_concept1, id_theso, path) || seperateur;
        END LOOP;

		replacedBy = '';
        FOR replacedBy_rec IN SELECT id_concept2, id_ark, id_handle, id_doi
            FROM concept_replacedby, concept
            WHERE concept.id_concept = concept_replacedby.id_concept2
            AND concept.id_thesaurus = concept_replacedby.id_thesaurus
            AND concept_replacedby.id_concept1 = con.id_concept
            AND concept_replacedby.id_thesaurus = id_theso
        LOOP
			replacedBy = replacedBy || opentheso_get_uri(theso_rec.original_uri_is_ark, replacedBy_rec.id_ark, theso_rec.original_uri,
					theso_rec.original_uri_is_handle, replacedBy_rec.id_handle, theso_rec.original_uri_is_doi,
					replacedBy_rec.id_doi, replacedBy_rec.id_concept2, id_theso, path) || seperateur;
        END LOOP;

		facets = '';
        FOR facet_rec IN SELECT thesaurus_array.id_facet
            FROM thesaurus_array
            WHERE thesaurus_array.id_thesaurus = id_theso
            AND thesaurus_array.id_concept_parent = con.id_concept
        LOOP
			facets = facets || facet_rec.id_facet || seperateur;
        END LOOP;

		externalResources = '';
        FOR externalResource_rec IN SELECT external_resources.external_uri
            FROM external_resources
            WHERE external_resources.id_thesaurus = id_theso
            AND external_resources.id_concept = con.id_concept
        LOOP
            externalResources = externalResources || externalResource_rec.external_uri || seperateur;
        END LOOP;

        SELECT 	uri, con.status, local_URI, con.id_concept, con.id_ark, prefLab, altLab, altLab_hiden, definition, example,
          editorialNote, changeNote, secopeNote, note, historyNote, con.notation, narrower, broader, related, exactMatch, closeMatch,
          broadMatch, relatedMatch, narrowMatch, gpsData, membre, con.created, con.modified,
          img, creator, contributor, replaces, replacedBy, facets, externalResources INTO rec;

        RETURN NEXT rec;
    END LOOP;
END;
$$;



CREATE OR REPLACE procedure opentheso_add_new_concept(
    id_thesaurus character varying,
    id_con character varying,
    id_user int,
    conceptStatus character varying,
    conceptType text,
    notationConcept character varying,
    id_ark character varying,
    isTopConcept Boolean,
    id_handle character varying,
    id_doi character varying,
    prefterms text,
    relation_hiarchique text,
    custom_relation text,
    notes text,
    non_pref_terms text,
    alignements text,
    images text,
    idsConceptsReplaceBy text,
    isGpsPresent Boolean,
    gps text,
    created Date,
    modified Date,
    concept_dcterms text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    id_new_concet character varying;
    seperateur constant varchar := '##';
    concept_Rep_rec record;
BEGIN

    Insert into concept (id_concept, id_thesaurus, id_ark, created, modified, status, concept_type, notation, top_concept, id_handle, id_doi, creator, contributor, gps)
    values (id_con, id_thesaurus, id_ark, created, modified, conceptStatus, conceptType, notationConcept, isTopConcept, id_handle, id_doi, id_user, id_user, isGpsPresent) ;

    SELECT concept.id_concept INTO id_new_concet FROM concept WHERE concept.id_concept = id_con;

    IF (id_new_concet IS NOT NULL) THEN

        IF (prefterms IS NOT NULL AND prefterms != 'null') THEN
            -- 'lexical_value@lang@source@status@createed@modified'
            CALL opentheso_add_terms(id_new_concet, id_thesaurus, id_new_concet, id_user, prefterms);
        END IF;

        IF (relation_hiarchique IS NOT NULL AND relation_hiarchique != 'null') THEN
            -- 'id_concept1@role@id_concept2'
            CALL opentheso_add_hierarchical_relations(id_thesaurus, relation_hiarchique);
        END IF;

        IF (custom_relation IS NOT NULL AND custom_relation != 'null') THEN
            -- 'id_concept1@role@id_concept2'
            CALL opentheso_add_custom_relations(id_thesaurus, custom_relation);
        END IF;

        IF (concept_dcterms IS NOT NULL AND concept_dcterms != 'null') THEN
            -- 'creator@@miled@@fr##contributor@@zozo@@fr'
            CALL opentheso_add_concept_dcterms(id_new_concet, id_thesaurus, concept_dcterms);
        END IF;

        IF (notes IS NOT NULL AND notes != 'null') THEN
            -- 'value@typeCode@lang@id_term'
            CALL opentheso_add_notes(id_new_concet, id_thesaurus, id_user, notes);
        END IF;

        IF (non_pref_terms IS NOT NULL AND non_pref_terms != 'null') THEN
            -- 'id_term@lexical_value@lang@id_thesaurus@source@status@hiden'
            CALL opentheso_add_non_preferred_term(id_thesaurus, id_user, non_pref_terms);
        END IF;

        IF (images IS NOT NULL AND images != 'null') THEN
            -- 'url1##url2'
            CALL opentheso_add_external_images(id_thesaurus, id_new_concet, id_user, images);
        END IF;

        IF (alignements IS NOT NULL AND alignements != 'null') THEN
            -- 'author@concept_target@thesaurus_target@uri_target@alignement_id_type@internal_id_thesaurus@internal_id_concept'
            CALL opentheso_add_alignements(alignements);
        END IF;

        IF (idsConceptsReplaceBy IS NOT NULL AND idsConceptsReplaceBy != 'null') THEN
            FOR concept_Rep_rec IN SELECT unnest(string_to_array(idsConceptsReplaceBy, seperateur)) AS idConceptReplaceBy
                LOOP
                    Insert into concept_replacedby (id_concept1, id_concept2, id_thesaurus, id_user)
                    values(id_new_concet, concept_Rep_rec.idConceptReplaceBy, id_thesaurus, id_user);
                END LOOP;
        END IF;

        IF (gps IS NOT NULL AND gps != 'null') THEN
            CALL opentheso_add_gps(id_new_concet, id_thesaurus, gps);
        END IF;
    END IF;
END;
$BODY$;

CREATE OR REPLACE procedure opentheso_add_gps(
    id_concept character varying,
    id_thesaurus character varying,
    gpsList text)
    LANGUAGE 'plpgsql'
AS $BODY$
DECLARE
    seperateur constant varchar := '##';
    sous_seperateur constant varchar := '@@';

    pos numeric;
    gps_rec record;
    array_string   text[];
BEGIN
    pos = 1;
    FOR gps_rec IN SELECT unnest(string_to_array(gpsList, seperateur)) AS gps_value
        LOOP
            SELECT string_to_array(gps_rec.gps_value, sous_seperateur) INTO array_string;
            IF array_string[1] IS NOT NULL THEN
                insert into gps(position, id_concept, id_theso, latitude, longitude)
                    values (pos, id_concept, id_thesaurus, CAST (array_string[1] AS double precision), CAST (array_string[2] AS double precision));
                pos = pos + 1;
            END IF;
        END LOOP;
END;
$BODY$;