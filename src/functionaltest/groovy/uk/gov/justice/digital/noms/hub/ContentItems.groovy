package uk.gov.justice.digital.noms.hub

import com.mongodb.DB
import org.bson.types.ObjectId


class ContentItems {

    private DB db
    private Date aDate

    ContentItems(DB db){
    this.db = db
        aDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss'Z'", '2017-01-01T10:00:00Z')
    }


    String insertItem(int offset, String mediaType = 'application/pdf') {
        ObjectId id = ObjectId.get();

        db.contentItem.insert(
                _id: id,
                uri: "uri${offset}",
                filename: "filename${offset}",
                timestamp: timestamp(offset),
                files: [
                        main     : "filename${offset}",
                        thumbnail: "someUrl/thumb.jpg"
                ],
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

    def remove(items){
        items.each {
            db.contentItem.remove(_id: new ObjectId(it.toString()))
        }
    }
}
