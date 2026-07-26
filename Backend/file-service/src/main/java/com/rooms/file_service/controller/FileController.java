package com.rooms.file_service.controller;

import com.rooms.file_service.dto.ApiResponse;
import com.rooms.file_service.entity.FileMetadata;
import com.rooms.file_service.exception.ResourceNotFoundException;
import com.rooms.file_service.exception.UnauthorizedException;
import com.rooms.file_service.repository.FileMetadataRepository;
import com.rooms.file_service.service.CloudinaryService;
import com.rooms.file_service.service.RoomGrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileMetadataRepository fileMetadataRepository;
    private final CloudinaryService cloudinaryService;
    private final RoomGrpcClient roomGrpcClient;

    public FileController(FileMetadataRepository fileMetadataRepository, CloudinaryService cloudinaryService, RoomGrpcClient roomGrpcClient) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.cloudinaryService = cloudinaryService;
        this.roomGrpcClient = roomGrpcClient;
    }

    @PostMapping("/rooms/{roomId}/upload")
    public ResponseEntity<ApiResponse<FileMetadata>> uploadFile(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID roomId,
            @RequestParam("file") MultipartFile file) {

        if (!roomGrpcClient.isUserMemberOfRoom(roomId, userId)) {
            throw new UnauthorizedException("User is not a member of the room");
        }

        try {
            String folderPath = "rooms/" + roomId.toString();
            Map uploadResult = cloudinaryService.uploadFile(file, folderPath);

            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");
            String resourceType = (String) uploadResult.get("resource_type");

            FileMetadata metadata = new FileMetadata(
                    roomId,
                    userId,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    publicId,
                    secureUrl,
                    resourceType
            );

            metadata = fileMetadataRepository.save(metadata);

            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                    true, 201, metadata, "File uploaded successfully"
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, 500, null, "File upload failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ApiResponse<List<FileMetadata>>> listRoomFiles(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID roomId) {

        if (!roomGrpcClient.isUserMemberOfRoom(roomId, userId)) {
            throw new UnauthorizedException("User is not a member of the room");
        }

        List<FileMetadata> files = fileMetadataRepository.findByRoomIdAndDeletedAtIsNullOrderByUploadedAtDesc(roomId);

        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, files, "Files fetched successfully"
        ));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<ApiResponse<FileMetadata>> getFile(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable String fileId) {

        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        if (!roomGrpcClient.isUserMemberOfRoom(file.getRoomId(), userId)) {
            throw new UnauthorizedException("User is not a member of the room");
        }

        return ResponseEntity.ok(new ApiResponse<>(
                true, 200, file, "File metadata fetched successfully"
        ));
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable String fileId) {

        FileMetadata file = fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        // Only uploader or room owner can delete. We'll simplify to uploader for now, 
        // as getting the owner requires another gRPC call (or parsing member roles).
        if (!file.getUploadedBy().equals(userId)) {
            throw new UnauthorizedException("Only the uploader can delete this file");
        }

        try {
            cloudinaryService.deleteFile(file.getCloudinaryPublicId(), file.getResourceType());
            fileMetadataRepository.delete(file);

            return ResponseEntity.ok(new ApiResponse<>(
                    true, 200, null, "File deleted successfully"
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(
                    false, 500, null, "File deletion failed: " + e.getMessage()
            ));
        }
    }
}
