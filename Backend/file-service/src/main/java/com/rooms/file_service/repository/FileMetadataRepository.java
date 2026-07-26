package com.rooms.file_service.repository;

import com.rooms.file_service.entity.FileMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends MongoRepository<FileMetadata, String> {
    List<FileMetadata> findByRoomIdAndDeletedAtIsNullOrderByUploadedAtDesc(UUID roomId);
}
