package uk.gov.justice.digital.noms.hub

import groovy.util.logging.Slf4j

@Slf4j
class Hub {

    String adminUri
    String username
    String password
    String mongoConnectionUri
    String azureConnectionUri
    String azurePublicUrlBase
    Map basicAuth

    Hub() {
        username = System.getenv('BASIC_AUTH_USERNAME') ?: 'user'
        password = System.getenv('BASIC_AUTH_PASSWORD') ?: 'password'

        adminUri = (System.getenv('APPLICATION_URL') ?: "http://localhost:8090/hub-admin")
        adminUri = adminUri.replaceFirst('^https?://', "http://${username}:${password}@")

        mongoConnectionUri = System.getenv('MONGODB_CONNECTION_URI') ?: 'mongodb://localhost:27017'

        azureConnectionUri = System.getenv("AZURE_BLOB_STORE_CONNECTION_URI") ?: 'AccountName=abc;AccountKey=YWJjCg==;BlobEndpoint=http://127.0.0.1:10000/devstoreaccount1;';

        azurePublicUrlBase = System.getenv("AZURE_BLOB_STORE_PUBLIC_URL_BASE") ?: 'http://127.0.0.1:10000';

        String credentials = "${username}:${password}".bytes.encodeBase64()
        basicAuth = [requestProperties: [Authorization: "Basic ${credentials}"]]
    }


}
