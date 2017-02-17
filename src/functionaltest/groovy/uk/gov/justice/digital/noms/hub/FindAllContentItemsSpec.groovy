package uk.gov.justice.digital.noms.hub

import com.gmongo.GMongo
import com.gmongo.GMongoClient
import com.mongodb.DB
import groovy.json.JsonSlurper
import org.bson.types.ObjectId
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class FindAllContentItemsSpec extends Specification {

    @Shared
    private Hub theHub

    @Shared
    private String adminAppRoot

    @Shared
    private GMongo mongo

    @Shared
    private DB db

    private Map basicAuth
    private Date aDate
    private jsonSlurper

    def setup() {
        theHub = new Hub()

        adminAppRoot = theHub.adminUri

        mongo = new GMongoClient(theHub.mongoConnectionUri)
        db = mongo.getDB("hub_metadata")

        String credentials = "${theHub.username}:${theHub.password}".bytes.encodeBase64()
        basicAuth = [requestProperties: [Authorization: "Basic ${credentials}"]]

        aDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", '2017-01-01T10:00:00Z')

        jsonSlurper = new JsonSlurper()
    }

    @Ignore
    def 'findAll returns all, newest first, with the right contents'() {

        given: 'the number of items that already exist'
        def originalCount = db.contentItem.find().count()

        and: 'we create two new items'
        def itemOneId = insertItem(1)
        def itemTwoId = insertItem(2)

        when: 'we get the all content items JSON resource'
        def json = (adminAppRoot + '/content-items').toURL().getText(basicAuth)
        def jsonObject = jsonSlurper.parseText(json)
        def items = jsonObject.contentItems

        and: 'we count the items now in the database'
        def finalCount = db.contentItem.find().count()

        then: 'there should be 2 more in the database'
        finalCount == originalCount + 2

        and: 'the JSON should have all the items'
        items.size == finalCount

        and: 'the two new items should be in the json'
        def matches = items.findAll { [itemOneId, itemTwoId].contains(it.id) }
        matches.size == 2

        and: 'the items should be in reverse chronological order'
        matches[0].id == itemTwoId
        matches[1].id == itemOneId

        and: 'the items should have the correct attributes'
        matches[0].filename == 'filename2'
        matches[0].metadata.title == 'title2'
        matches[0].metadata.category == 'category2'

        matches[1].filename == 'filename1'
        matches[1].metadata.title == 'title1'
        matches[1].metadata.category == 'category1'

        cleanup:
        [itemOneId, itemTwoId].each {
            db.contentItem.remove(_id: new ObjectId(it.toString()))
        }
    }

    @Ignore
    def 'find all with filter returns only matching items'() {

        given: 'we create two new image items'
        def imageType = 'image/jpg'
        def itemOneId = insertItem(1, imageType)
        def itemTwoId = insertItem(2, imageType)

        and: 'we create a new video item'
        def videoType = 'video/mp4'
        def itemThreeId = insertItem(3, videoType)

        when: 'we get the content items filtered for image'
        def imageItems = getJsonForMediaType(imageType)

        and: 'we get the content items filtered for video'
        def videoItems = getJsonForMediaType(videoType)

        then: 'we have the two new image items in the first query'
        imageItems.find { it.id == itemOneId } != null
        imageItems.find { it.id == itemTwoId } != null
        imageItems.find { it.id == itemThreeId } == null

        then: 'we have the one new video item in the second query'
        videoItems.find { it.id == itemOneId } == null
        videoItems.find { it.id == itemTwoId } == null
        videoItems.find { it.id == itemThreeId } != null

        cleanup:
        [itemOneId, itemTwoId, itemThreeId].each {
            db.contentItem.remove(_id: new ObjectId(it.toString()))
        }
    }

    String insertItem(int offset, String mediaType = 'application/pdf') {
        ObjectId id = ObjectId.get();

        db.contentItem.insert(
                _id: id,
                uri: "uri${offset}",
                filename: "filename${offset}",
                timestamp: timestamp(offset),
                metadata: [
                        title    : "title${offset}",
                        category : "category${offset}",
                        mediaType: "${mediaType}"]
        )

        return id
    }

    String timestamp(int offsetDays) {
        return aDate.plus(offsetDays - 1).format("yyyy-MM-dd'T'HH:mm:ss'Z'")
    }

    List getJsonForMediaType(mediaType) {
        def json = (adminAppRoot + "/content-items?filter={'metadata.mediaType':'${mediaType}'}").toURL().getText(basicAuth)

        return jsonSlurper.parseText(json).contentItems
    }
}
