package uk.gov.justice.digital.noms.hub.domain;

import java.io.InputStream;

public interface MediaRepository {
    String save(InputStream mediaStream, String filename, long size);
}
