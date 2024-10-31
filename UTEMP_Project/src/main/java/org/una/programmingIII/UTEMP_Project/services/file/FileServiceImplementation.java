package org.una.programmingIII.UTEMP_Project.services.file;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.una.programmingIII.UTEMP_Project.dtos.FileMetadatumDTO;
import org.una.programmingIII.UTEMP_Project.models.FileMetadatum;
import org.una.programmingIII.UTEMP_Project.models.Submission;
import org.una.programmingIII.UTEMP_Project.models.User;
import org.una.programmingIII.UTEMP_Project.repositories.FileMetadatumRepository;
import org.una.programmingIII.UTEMP_Project.repositories.SubmissionRepository;
import org.una.programmingIII.UTEMP_Project.repositories.UserRepository;
import org.una.programmingIII.UTEMP_Project.transformers.mappers.GenericMapperFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

@Service
@Transactional
public class FileServiceImplementation implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImplementation.class);
    private final FileMetadatumRepository fileMetadatumRepository;
    private final SubmissionRepository submissionRepository;
    private final UserRepository userRepository;
    private final String fileBasePath = "path/to/final";

    // Mapa para almacenar los fragmentos en memoria
    private final Map<Long, List<byte[]>> fileChunksMap = new HashMap<>();

    @Autowired
    public FileServiceImplementation(GenericMapperFactory mapperFactory,
                                     FileMetadatumRepository fileMetadatumRepository,
                                     SubmissionRepository submissionRepository,
                                     UserRepository userRepository) {
        this.fileMetadatumRepository = fileMetadatumRepository;
        this.submissionRepository = submissionRepository;
        this.userRepository = userRepository;
    }

    // Métodos de Consulta

    /**
     * Obtiene los metadatos del archivo por su ID.
     *
     * @param id el ID del archivo
     * @return un DTO con la información del archivo
     * @throws IllegalArgumentException si no se encuentra el archivo
     */
    @Transactional(readOnly = true)
    public Optional<FileMetadatumDTO> getFileMetadatumById(Long id) {
        Optional<FileMetadatum> fileMetadatum = fileMetadatumRepository.findById(id);
        if (fileMetadatum.isEmpty()) {
            throw new IllegalArgumentException("No such file ID: " + id);
        }
        FileMetadatum file = fileMetadatum.get();

        return Optional.ofNullable(FileMetadatumDTO.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .storagePath(file.getStoragePath())
                .lastUpdate(file.getLastUpdate())
                .createdAt(file.getCreatedAt())
                .build());
    }

    // Métodos de Recepción y Finalización

    /**
     * Recibe un fragmento de archivo y lo almacena en memoria.
     *
     * @param fileChunkDTO DTO que contiene información del fragmento
     * @throws FileUploadException si ocurre un error al subir el archivo
     */
    public void receiveFileChunk(FileMetadatumDTO fileChunkDTO) throws IOException {
        Long fileId = fileChunkDTO.getId();
        fileChunksMap.putIfAbsent(fileId, new ArrayList<>());

        List<byte[]> chunks = fileChunksMap.get(fileId);

        // Validar que el chunkIndex es válido
        if (fileChunkDTO.getChunkIndex() < 0 || fileChunkDTO.getChunkIndex() >= fileChunkDTO.getTotalChunks()) {
            throw new IllegalArgumentException("Invalid chunk index: " + fileChunkDTO.getChunkIndex());
        }

        // Agregar el fragmento si es el siguiente en la secuencia
        if (fileChunkDTO.getChunkIndex() == chunks.size()) {
            chunks.add(fileChunkDTO.getFileChunk());
            logger.info("Chunk {} added for file ID: {}", fileChunkDTO.getChunkIndex(), fileId);
        } else {
            logger.warn("Received out-of-order chunk for file ID: {}, expected index: {}", fileId, chunks.size());
        }

        // Verificar si todos los fragmentos han sido recibidos
        if (chunks.size() == fileChunkDTO.getTotalChunks()) {
            finalizeFileUpload(fileId, fileChunkDTO);
        }
    }

    /**
     * Finaliza la carga del archivo escribiendo los fragmentos en un archivo final.
     *
     * @param fileId  el ID del archivo
     * @param fileDTO DTO con los metadatos del archivo
     * @throws IOException si ocurre un error al finalizar la carga
     */
    public void finalizeFileUpload(Long fileId, FileMetadatumDTO fileDTO) throws IOException {
        List<byte[]> chunks = fileChunksMap.get(fileId);
        if (chunks == null || chunks.isEmpty()) {
            throw new IllegalArgumentException("No chunks found for file ID: " + fileId);
        }

        validateStoragePath(fileBasePath);

        String finalFilePath = String.format(fileBasePath + "/%s<%d>.%s", fileDTO.getFileName(), fileId, fileDTO.getFileType());

        // Escribir los fragmentos en el archivo final
        try (FileOutputStream fos = new FileOutputStream(finalFilePath)) {
            for (byte[] chunk : chunks) {
                fos.write(chunk);
            }
            logger.info("File upload finalized for file ID: {}", fileId);
        } catch (IOException e) {
            logger.error("Error finalizing file upload: {}", e.getMessage());
            throw new FileUploadException("Error finalizing file upload", e);
        }

        // Crear y guardar la metadata del archivo
        FileMetadatum fileMetadatum = new FileMetadatum();
        fileMetadatum.setFileName(String.format("%s<%d>.%s", fileDTO.getFileName(), fileId, fileDTO.getFileType()));
        fileMetadatum.setFileSize(chunks.stream().mapToLong(chunk -> chunk.length).sum());
        fileMetadatum.setStoragePath(finalFilePath);
        fileMetadatum.setFileType(fileDTO.getFileType());

        // Validar existencia de usuario y presentación
        Optional<User> userOpt = userRepository.findById(fileDTO.getStudent().getId());
        Optional<Submission> submissionOpt = submissionRepository.findById(fileDTO.getSubmission().getId());

        if (userOpt.isEmpty() || submissionOpt.isEmpty()) {
            throw new IllegalArgumentException("User or Submission not found");
        }

        fileMetadatum.setStudent(userOpt.get());
        fileMetadatum.setSubmission(submissionOpt.get());

        fileMetadatumRepository.save(fileMetadatum);

        // Limpiar los fragmentos después de la carga
        fileChunksMap.remove(fileId);
    }

    // Métodos de Actualización

    /**
     * Actualiza los metadatos de un archivo eliminando el existente y recibiendo uno nuevo.
     *
     * @param id           el ID del archivo a actualizar
     * @param fileChunkDTO DTO que contiene el nuevo archivo
     * @return el nuevo DTO con los metadatos del archivo
     * @throws FileUploadException si ocurre un error al actualizar el archivo
     */
    @Override
    @Transactional
    public FileMetadatumDTO updateFileMetadatum(Long id, FileMetadatumDTO fileChunkDTO) throws IOException {
        // Validar que exista el archivo
        Optional<FileMetadatum> existingFileOpt = fileMetadatumRepository.findById(id);
        if (existingFileOpt.isEmpty()) {
            throw new IllegalArgumentException("No such file ID: " + id);
        }

        // Primero, eliminamos el archivo existente
        deleteFileMetadatum(id);

        // Luego, recibimos el nuevo archivo
        receiveFileChunk(fileChunkDTO);

        // Devolver el nuevo metadata del archivo
        return getFileMetadatumById(id).orElse(null);
    }

    // Métodos de Eliminación

    /**
     * Elimina los metadatos de un archivo y borra el archivo del sistema.
     *
     * @param id el ID del archivo a eliminar
     * @throws IllegalArgumentException si no se encuentra el archivo
     */
    @Transactional
    public void deleteFileMetadatum(Long id) {
        Optional<FileMetadatum> fileMetadatumOpt = fileMetadatumRepository.findById(id);
        if (fileMetadatumOpt.isPresent()) {
            FileMetadatum fileMetadatum = fileMetadatumOpt.get();
            // Eliminar el archivo del sistema de archivos
            File file = new File(fileMetadatum.getStoragePath());
            if (file.exists() && !file.delete()) {
                logger.warn("Failed to delete file: {}", file.getAbsolutePath());
            } else {
                logger.info("Deleted file: {}", file.getAbsolutePath());
            }
            fileMetadatumRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("No such file ID: " + id);
        }
    }

    //descargar de base de datos

    public List<FileMetadatumDTO> downloadFileInChunks(Long fileId) throws IOException {
        Optional<FileMetadatum> fileMetadatumOpt = fileMetadatumRepository.findById(fileId);
        if (fileMetadatumOpt.isEmpty()) {
            throw new IllegalArgumentException("No such file ID: " + fileId);
        }

        FileMetadatum fileMetadatum = fileMetadatumOpt.get();
        File file = new File(fileMetadatum.getStoragePath());
        List<FileMetadatumDTO> chunks = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[512 * 1024]; // 512KB
            int bytesRead;
            int chunkIndex = 0;

            while ((bytesRead = fis.read(buffer)) != -1) {
                byte[] fileChunk = Arrays.copyOf(buffer, bytesRead);
                FileMetadatumDTO chunkDTO = FileMetadatumDTO.builder()
                        .id(fileId)
                        .fileName(fileMetadatum.getFileName())
                        .fileType(fileMetadatum.getFileType())
                        .fileChunk(fileChunk)
                        .chunkIndex(chunkIndex++)
                        .totalChunks((int) Math.ceil((double) fileMetadatum.getFileSize() / (512 * 1024)))
                        .build();
                chunks.add(chunkDTO);
            }
        }

        return chunks;
    }


    // Método para combinar los bloques en un solo array
    private byte[] combineChunks(List<byte[]> chunks) {
        int totalLength = chunks.stream().mapToInt(chunk -> chunk.length).sum();
        byte[] combined = new byte[totalLength];

        int currentIndex = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, combined, currentIndex, chunk.length);
            currentIndex += chunk.length;
        }

        return combined;
    }
    // Métodos de Validación

    /**
     * Valida si la ruta de almacenamiento existe, y si no, la crea.
     *
     * @param path la ruta que se va a validar
     * @throws IOException si ocurre un error al crear la ruta
     */
    private void validateStoragePath(String path) throws IOException {
        File directory = new File(path);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create storage directory: " + path);
            }
        }
    }
}

/**
 * Busca archivos según el nombre del archivo y el tipo.
 *
 * @param fileName el nombre del archivo a buscar
 * @param fileType el tipo de archivo a buscar
 * @return una lista de DTOs de metadatos de archivos que coinciden
 */
//@Transactional(readOnly = true)
//private List<FileMetadatumDTO> searchFiles(String fileName, String fileType) {
//    List<FileMetadatum> files = fileMetadatumRepository.findByFileNameContainingAndFileType(fileName, fileType);
//    return files.stream()
//            .map(file -> FileMetadatumDTO.builder()
//                    .id(file.getId())
//                    .fileName(file.getFileName())
//                    .fileType(file.getFileType())
//                    .fileSize(file.getFileSize())
//                    .storagePath(file.getStoragePath())
//                    .lastUpdate(file.getLastUpdate())
//                    .createdAt(file.getCreatedAt())
//                    .build())
//            .collect(Collectors.toList());
//}
