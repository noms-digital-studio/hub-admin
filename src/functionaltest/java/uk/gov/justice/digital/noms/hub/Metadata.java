package uk.gov.justice.digital.noms.hub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

class Metadata {
    private static final String TITLE = "A one pixel image";
    private static final String CATEGORY = "A category";
    private static final String MEDIA_TYPE = "aMediaType";

    @SuppressWarnings("unchecked")
    static Map<String, Object> convertToMap(Object documentField) {
        return (Map<String, Object>) documentField;
    }

    static String someJsonMetadata(String valuePostfix) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(someMetadata(valuePostfix));
    }

    static Map<String, Object> someMetadata(String valuePostfix) {
        return ImmutableMap.of(
                "title", TITLE + valuePostfix,
                "category", CATEGORY + valuePostfix,
                "mediaType", MEDIA_TYPE + valuePostfix
        );
    }

}
