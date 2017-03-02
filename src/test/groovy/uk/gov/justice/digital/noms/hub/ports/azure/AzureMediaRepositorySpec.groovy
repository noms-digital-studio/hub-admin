package uk.gov.justice.digital.noms.hub.ports.azure

import spock.lang.Specification
import spock.lang.Unroll


class AzureMediaRepositorySpec extends Specification {


    @Unroll
    def 'gets mime types where #fileName gives #mimeType'() {

        expect:
        AzureMediaRepository.getMimeType(fileName) == mimeType

        where:
        fileName        | mimeType
        'course.pdf'    | 'application/pdf'
        'course.doc'    | 'application/msword'
        'image.jpg'     | 'image/jpeg'
        'image.jpeg'    | 'image/jpeg'
        'image.png'     | 'image/png'
        'audio.mp3'     | 'audio/mpeg'
        'video.mp4'     | 'video/mp4'
        'video.mpeg'    | 'video/mpeg'
        'video.mov'     | 'video/quicktime'
        'video.avi'     | 'video/x-msvideo'
        'video.unknown' | 'application/octet-stream'

    }
}
