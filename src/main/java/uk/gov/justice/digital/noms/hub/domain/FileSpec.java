package uk.gov.justice.digital.noms.hub.domain;


import lombok.Data;

import java.io.InputStream;

@Data
public class FileSpec {
    private final InputStream inputStream;
    private final String filename;
    private final long size;

    public FileSpec(InputStream inputStream, String filename, long size) {
        this.inputStream = inputStream;
        this.filename = filename;
        this.size = size;
    }

}
