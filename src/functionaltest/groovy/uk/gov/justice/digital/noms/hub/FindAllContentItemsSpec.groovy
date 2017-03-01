package uk.gov.justice.digital.noms.hub

import com.gmongo.GMongo
import com.gmongo.GMongoClient
import com.mongodb.DB
import com.mongodb.MongoClientURI
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

    def 'findAll returns all, newest first, with the right contents'() {

        given: 'the number of items that already exist'
        def originalCount = db.contentItem.find().count()

        and: 'we create two new items'
        def itemOneId = contentItems.insertItem(1)
        def itemTwoId = contentItems.insertItem(2)

        when: 'we get the all content items JSON resource'
        def json = (adminAppRoot + '/content-items?filter={}').toURL().getText(theHub.basicAuth)
        def jsonObject = new JsonSlurper().parseText(json)
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
        matches[0].files.main == 'filename2'

        matches[1].filename == 'filename1'
        matches[1].metadata.title == 'title1'
        matches[1].metadata.category == 'category1'
        matches[1].files.main == 'filename1'

        cleanup:
        contentItems.remove([itemOneId, itemTwoId])
    }

    def 'find all with filter returns only matching items'() {

        given: 'we create two new image items'
        def imageType = 'image/jpg'
        def itemOneId = contentItems.insertItem(1, imageType)
        def itemTwoId = contentItems.insertItem(2, imageType)

        and: 'we create a new video item'
        def videoType = 'video/mp4'
        def itemThreeId = contentItems.insertItem(3, videoType)

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
        contentItems.remove([itemOneId, itemTwoId, itemThreeId])
    }

    List getJsonForMediaType(mediaType) {
        def json = (adminAppRoot + "/content-items?filter={'metadata.mediaType':'${mediaType}'}").toURL().getText(theHub.basicAuth)

        return new JsonSlurper().parseText(json).contentItems
    }
}
