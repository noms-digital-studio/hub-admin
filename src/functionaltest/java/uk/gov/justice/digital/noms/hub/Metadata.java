package uk.gov.justice.digital.noms.hub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

class Metadata {
    private static final String TITLE = "A one pixel image";
    private static final String CATEGORY = "A category";
    static final String MEDIA_TYPE = "application/pdf";

    @SuppressWarnings("unchecked")
    static Map<String, Object> convertToMap(Object documentField) {
        return (Map<String, Object>) documentField;
    }

    static String someJsonMetadata(String valuePostfix) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(someMetadata(valuePostfix, MEDIA_TYPE));
    }

    static Map<String, Object> someMetadata(String valuePostfix, String mediaType) {
        return ImmutableMap.of(
                "title", TITLE + valuePostfix,
                "category", CATEGORY + valuePostfix,
                "mediaType", mediaType
        );
    }

}
