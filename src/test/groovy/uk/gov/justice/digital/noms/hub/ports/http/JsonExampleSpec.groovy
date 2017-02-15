package uk.gov.justice.digital.noms.hub.ports.http

import groovy.json.JsonSlurper
import spock.lang.Specification


class JsonExampleSpec extends Specification {

    def jsonSlurper = new JsonSlurper()

    def json = """{
        "contentItems": [
                {
                    "id": "589d89083ac0e8371ff18ea4",
                    "title": "5",
                    "mediaUri": "http://digitalhub2.blob.core.windows.net/content-items/video400KB.mp4",
                    "filename": "video400KB.mp4",
                    "category": "History"
                },
                {
                    "id": "589d6c2e3ac0e8371ff18ea3",
                    "title": "2",
                    "mediaUri": "http://digitalhub2.blob.core.windows.net/content-items/test.jpg",
                    "filename": "test.jpg",
                    "category": "Business and Economics"
                },
                {
                    "id": "589d897d3ac0e8371ff18ea5",
                    "title": "4",
                    "mediaUri": "http://digitalhub2.blob.core.windows.net/content-items/avatar",
                    "filename": "avatar",
                    "category": "Art and Literature"
                }
        ]
    }"""

    def 'verify metadata per item in order'() {

        given:
        def secondId = "589d6c2e3ac0e8371ff18ea3"

        when:
        def object = jsonSlurper.parseText(json)
        def items = object.contentItems


        then: 'basic collection stuff'

        object instanceof Map
        items.size == 3


        and: 'simple array indexing stuff'

        items[0].id == "589d89083ac0e8371ff18ea4"


        and: 'find a particular item and verify properties'

        def specificItem = items.find { it.id == "589d89083ac0e8371ff18ea4" }
        specificItem.category == 'History'

        items.find { it.id == secondId }.title == '2'


        and: 'find item positions'

        items.findIndexOf { it.category == 'Art and Literature' } == 2

        items.findIndexValues { it.category.length() > 7 } == [1, 2]


        and: 'check presence of expected values'

        def expectedIds = ["589d89083ac0e8371ff18ea4", "589d897d3ac0e8371ff18ea5"]

        def matches = items.findAll { expectedIds.contains(it.id) }
        matches.size == 2


        and: 'keep the expected items and remove all others'

        items.size == 3
        items.retainAll { expectedIds.contains(it.id) }
        items.size == 2
    }
}

