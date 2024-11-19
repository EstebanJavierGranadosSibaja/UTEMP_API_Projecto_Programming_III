package org.una.programmingIII.UTEMP_Project.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.exceptions.ResourceNotFoundException;
import org.una.programmingIII.UTEMP_Project.services.file.FileService;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@RestController
@RequestMapping("/utemp/files")
@RequiredArgsConstructor
public class FileController {// no borrar este comentario. esta clase es parte del API backend que recibe solicitudes y devuelve resuestas

    private final FileService fileService;
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    // Crear metadatos del archivo
    @PostMapping
    public ResponseEntity<FileMetadatumDTO> createFileMetadata(@RequestBody FileMetadatumDTO fileDTO) {
        try {
            logger.info("Solicitando creación de metadatos para archivo: {}", fileDTO.toString());
            FileMetadatumDTO createdMetadata = fileService.createNewFileMetadata(fileDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMetadata);
        } catch (Exception e) {
            logger.error("Error al crear metadatos del archivo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Obtener los metadatos de un archivo
    @GetMapping("/{id}")
    public ResponseEntity<FileMetadatumDTO> getFileMetadata(@PathVariable Long id) {
        try {
            logger.info("Solicitando metadatos del archivo con ID: {}", id);
            FileMetadatumDTO fileMetadata = fileService.getFileMetadatumById(id);
            return ResponseEntity.ok(fileMetadata);
        } catch (ResourceNotFoundException e) {
            logger.warn("Archivo con ID {} no encontrado: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error inesperado al obtener metadatos del archivo con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Eliminar el archivo y sus metadatos
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable Long id) {
        try {
            logger.info("Solicitando eliminación de archivo con ID: {}", id);
            boolean deleted = fileService.deleteFile(id);

            if (deleted) {
                logger.info("Archivo con ID: {} eliminado correctamente.", id);
                return ResponseEntity.noContent().build();  // 204 No Content
            } else {
                logger.warn("No se pudo eliminar el archivo con ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  // 404 Not Found
            }
        } catch (ResourceNotFoundException e) {
            logger.error("Archivo no encontrado con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            logger.error("Error inesperado al eliminar el archivo con ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();  // 500 Internal Server Error
        }
    }

    @PostMapping("/receive-chunk")
    public ResponseEntity<Void> receiveFileChunk(@RequestBody FileMetadatumDTO fileDTO) {
        try {
            // Log de recepción de fragmento de archivo
            logger.info("Recibiendo fragmento {} de {} para el archivo con ID: {}",
                    fileDTO.getChunkIndex() + 1, fileDTO.getTotalChunks(), fileDTO.getId());

            // Llamada al servicio para recibir y guardar el fragmento
            fileService.receiveFileChunk(fileDTO);

            // Retornar respuesta exitosa
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // Manejo de errores si ocurre un fallo durante la recepción del fragmento
            logger.error("Error al recibir fragmento para el archivo con ID {}: {}", fileDTO.getId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable Long id) {
        logger.info("Iniciando descarga del archivo con ID: {}", id);

        // Obtener metadatos del archivo
        FileMetadatumDTO fileMetadata = fileService.getFileMetadatumById(id);

        if (fileMetadata == null) {
            logger.error("Archivo no encontrado con ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(outputStream -> outputStream.write("Archivo no encontrado".getBytes()));
        }

        String filePath = fileMetadata.getStoragePath();
        File file = new File(filePath);

        if (!file.exists()) {
            logger.error("El archivo no existe físicamente en la ruta: {}", filePath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(outputStream -> outputStream.write("Archivo no disponible".getBytes()));
        }

        long fileSize = file.length();
        logger.info("Tamaño total del archivo: {} bytes", fileSize);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileMetadata.getFileName() + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize)) // Indicar el tamaño total del archivo
                .body(outputStream -> {
                    try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                        byte[] buffer = new byte[512 * 1024]; // Tamaño del fragmento: 512 KB
                        int bytesRead;
                        long bytesSent = 0;

                        while ((bytesRead = raf.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                            outputStream.flush();
                            bytesSent += bytesRead;
                            logger.info("Enviando fragmento: {} bytes enviados de {} total", bytesSent, fileSize);
                        }
                    } catch (IOException e) {
                        logger.error("Error durante la descarga del archivo: {}", e.getMessage(), e);
                        outputStream.write(("Error al descargar el archivo: " + e.getMessage()).getBytes());
                    }
                });
    }

}
