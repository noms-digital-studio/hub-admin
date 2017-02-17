package uk.gov.justice.digital.noms.hub

import groovy.util.logging.Slf4j

@Slf4j
class Hub {

    String adminUri
    String adminUiUri
    String username
    String password
    String mongoConnectionUri

    Hub() {
        username = System.getenv('BASIC_AUTH_USERNAME') ?: 'user'
        password = System.getenv('BASIC_AUTH_PASSWORD') ?: 'password'

        adminUri = (System.getenv('HUB_ADMIN_URI') ?: "http://localhost:8080/hub-admin")
        adminUri = adminUri.replaceFirst('^https?://', "http://${username}:${password}@")
        log.info("adminUri: ${adminUri}")

        adminUiUri = (System.getenv('HUB_ADMIN_UI_URI') ?: "http://localhost:3000/")
        adminUiUri = adminUiUri.replaceFirst('^https?://', "http://${username}:${password}@")
        log.info("adminUiUri: ${adminUiUri}")

        mongoConnectionUri = System.getenv('MONGODB_CONNECTION_URI') ?: 'localhost:27017'
        log.info("mongoConnectionUri: ${mongoConnectionUri}")
    }
}
