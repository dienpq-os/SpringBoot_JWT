package dienpq.domain.port.external;

import dienpq.domain.model.DomainFile;

public interface FileServicePort {

    void deleteFile(String fileUrl);

    String storeFile(DomainFile file);
}
