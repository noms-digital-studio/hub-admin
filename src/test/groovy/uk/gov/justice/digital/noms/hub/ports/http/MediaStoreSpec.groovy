package uk.gov.justice.digital.noms.hub.ports.http

import org.springframework.web.multipart.MultipartFile
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import uk.gov.justice.digital.noms.hub.domain.FileSpec
import uk.gov.justice.digital.noms.hub.domain.MediaRepository
import uk.gov.justice.digital.noms.hub.domain.MediaStore

class MediaStoreSpec extends Specification {

    private MediaStore mediaStore
    private mediaRepository = Mock(MediaRepository)

    @Shared
    private file = Mock(FileSpec)

    def setup() {
        mediaStore = new MediaStore(mediaRepository)
    }

    def 'throws exception when no files'() {
        when:
        mediaStore.storeFiles([], someMetadata([]))

        then:
        thrown RuntimeException
    }

    @Unroll
    def 'throws exception when #files.size file(s) and #labels.size label(s)'() {
        when:
        mediaStore.storeFiles(files, someMetadata(labels))

        then:
        thrown RuntimeException

        where:
        files              | labels
        [file]             | []
        [file]             | ["1", "2"]
        [file, file]       | ["1"]
        [file, file]       | ["1", "2", "3"]
        [file, file, file] | ["1", "2"]
    }

    def 'matches labels to files in same order'() {
        given:
        mediaRepository.save(*_) >>> ['first', 'second', 'third']

        when:
        Map fileList = mediaStore.storeFiles([file, file, file], someMetadata(["1", "2", "3"]))

        then:
        fileList.get("1") == 'first'
        fileList.get("2") == 'second'
        fileList.get("3") == 'third'
    }

    @Unroll
    def 'calls mediastore #count times when #count file(s)'() {
        when:
        mediaStore.storeFiles(files, someMetadata(labels))

        then:
        count * mediaRepository.save(*_)

        where:
        files              | labels          | count
        [file]             | ["1"]           | 1
        [file, file]       | ["1", "2"]      | 2
        [file, file, file] | ["1", "2", "3"] | 3
    }

    def someMetadata(fileLabels) {
        [
                title     : "TITLE",
                category  : "CATEGORY",
                mediaType : "MEDIA_TYPE",
                fileLabels: fileLabels
        ]
    }
}
