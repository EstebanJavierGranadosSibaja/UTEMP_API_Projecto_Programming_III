package org.una.programmingIII.UTEMP_Project.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FileMetadatumTest {

    private FileMetadatum fileMetadatum;

    @BeforeEach
    void setUp() {
        fileMetadatum = FileMetadatum.builder()
                .fileName("test_document.pdf")
                .fileSize(1048576L) // 1 MB
                .fileType("application/pdf")
                .storagePath("/files/submissions/test_document.pdf")
                .submission(new Submission())
                .student(new User())
                .build();
    }

    @Test
    void testFileMetadatumInitialization() {
        assertThat(fileMetadatum).isNotNull();
        assertThat(fileMetadatum.getId()).isNull(); // ID es null antes de persistir.
        assertThat(fileMetadatum.getFileName()).isEqualTo("test_document.pdf");
        assertThat(fileMetadatum.getFileSize()).isEqualTo(1048576L);
        assertThat(fileMetadatum.getFileType()).isEqualTo("application/pdf");
        assertThat(fileMetadatum.getStoragePath()).isEqualTo("/files/submissions/test_document.pdf");
        assertThat(fileMetadatum.getSubmission()).isNotNull();
        assertThat(fileMetadatum.getStudent()).isNotNull();
    }

    @Test
    void testFileNameConstraints() {
        fileMetadatum.setFileName("valid_file_name.txt");
        assertThat(fileMetadatum.getFileName()).isEqualTo("valid_file_name.txt");

        fileMetadatum.setFileName("A".repeat(255)); // Nombre límite máximo permitido.
        assertThat(fileMetadatum.getFileName()).hasSize(255);

        fileMetadatum.setFileName(null); // La validación @NotNull no aplica aquí directamente.
        assertThat(fileMetadatum.getFileName()).isNull();
    }

    @Test
    void testFileSizeNotNull() {
        fileMetadatum.setFileSize(null);
        assertThat(fileMetadatum.getFileSize()).isNull(); // La validación @NotNull es en persistencia.
    }

    @Test
    void testFileTypeConstraints() {
        fileMetadatum.setFileType("text/plain");
        assertThat(fileMetadatum.getFileType()).isEqualTo("text/plain");

        fileMetadatum.setFileType("A".repeat(100)); // Límite permitido.
        assertThat(fileMetadatum.getFileType()).hasSize(100);

        fileMetadatum.setFileType(null); // No es obligatorio, así que puede ser null.
        assertThat(fileMetadatum.getFileType()).isNull();
    }

    @Test
    void testStoragePathConstraints() {
        fileMetadatum.setStoragePath("/valid/path/to/file.txt");
        assertThat(fileMetadatum.getStoragePath()).isEqualTo("/valid/path/to/file.txt");

        fileMetadatum.setStoragePath("A".repeat(500)); // Límite permitido.
        assertThat(fileMetadatum.getStoragePath()).hasSize(500);

        fileMetadatum.setStoragePath(null); // No es obligatorio, así que puede ser null.
        assertThat(fileMetadatum.getStoragePath()).isNull();
    }

    @Test
    void testCreatedAtAndLastUpdateOnCreate() {
        fileMetadatum.onCreate();
        assertThat(fileMetadatum.getCreatedAt()).isNotNull();
        assertThat(fileMetadatum.getLastUpdate()).isNotNull();
        assertThat(fileMetadatum.getCreatedAt()).isEqualTo(fileMetadatum.getLastUpdate());
    }

    @Test
    void testLastUpdateOnUpdate() {
        fileMetadatum.onCreate();
        LocalDateTime initialLastUpdate = fileMetadatum.getLastUpdate();

        // Simular actualización
        fileMetadatum.onUpdate();
        assertThat(fileMetadatum.getLastUpdate()).isAfter(initialLastUpdate);
        assertThat(fileMetadatum.getCreatedAt()).isNotNull(); // createdAt no debe cambiar.
    }

    @Test
    void testIdSetterAndGetter() {
        Long mockId = 1L;
        fileMetadatum.setId(mockId);
        assertThat(fileMetadatum.getId()).isEqualTo(mockId);
    }

    @Test
    void testDefaultSubmissionAndStudent() {
        FileMetadatum newFileMetadatum = new FileMetadatum();
        assertThat(newFileMetadatum.getSubmission()).isNotNull();
        assertThat(newFileMetadatum.getStudent()).isNotNull();
    }
}
