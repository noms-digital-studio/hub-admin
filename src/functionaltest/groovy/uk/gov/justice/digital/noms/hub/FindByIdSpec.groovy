package uk.gov.justice.digital.noms.hub

import com.gmongo.GMongoClient
import com.mongodb.DB
import com.mongodb.MongoClientURI
import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification


class FindByIdSpec  extends Specification {

    @Shared
    private Hub theHub

    @Shared
    private String adminAppRoot

    @Shared
    private DB db

    @Shared
    private ContentItems contentItems

    def setup() {
        theHub = new Hub()
        adminAppRoot = theHub.adminUri

        MongoClientURI mongoUri = new MongoClientURI(theHub.mongoConnectionUri)
        db = new GMongoClient(mongoUri).getDB("hub_metadata")

        contentItems = new ContentItems(db)
    }

    def 'find by id returns item with matching ID when it exists'() {

        given: 'we create two new items'
        def itemOneId = contentItems.insertItem(1)
        def itemTwoId = contentItems.insertItem(2)

        when: 'we get the first item by id'
        def json = (adminAppRoot + '/content-items/' + itemOneId).toURL().getText(theHub.basicAuth)
        def itemJson = new JsonSlurper().parseText(json)

        then: 'the item should have the correct attributes'
        itemJson.filename == 'filename1'
        itemJson.metadata.title == 'title1'
        itemJson.metadata.category == 'category1'
        itemJson.files.main == 'filename1'

        cleanup:
        contentItems.remove([itemOneId, itemTwoId])
    }

    def 'find by id returns 404 when no matching ID'() {

        when: 'we get an id that doesnt exist'
        HttpURLConnection conn = (adminAppRoot + '/content-items/no-such-id').toURL().openConnection()
        Map properties = theHub.basicAuth.requestProperties
        conn.setRequestProperty('Authorization', properties.get('Authorization'))

        then: 'the response is 404'
        conn.getResponseCode() == 404
    }

}
