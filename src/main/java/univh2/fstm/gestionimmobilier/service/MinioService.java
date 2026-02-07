package univh2.fstm.gestionimmobilier.service;

import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    /**
     * Crée un bucket s'il n'existe pas
     */
    public void createBucketIfNotExists(String bucketName) {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );

            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("✅ Bucket créé: {}", bucketName);
            } else {
                log.debug("ℹ️ Bucket existe déjà: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création du bucket: {}", bucketName, e);
            throw new RuntimeException("Erreur de création du bucket: " + bucketName, e);
        }
    }

    /**
     * Upload un fichier dans MinIO
     *
     * @param file Le fichier à uploader
     * @param bucketName Le nom du bucket
     * @param objectName Le nom de l'objet (chemin dans MinIO)
     * @return UUID du fichier uploadé
     */
    public String uploadFile(MultipartFile file, String bucketName, String objectName) {
        try {
            // Créer le bucket s'il n'existe pas
            createBucketIfNotExists(bucketName);

            // Générer un UUID unique pour le fichier
            String uuid = UUID.randomUUID().toString();
            String extension = getFileExtension(file.getOriginalFilename());
            String finalObjectName = objectName + "/" + uuid + extension;

            // Upload le fichier
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(finalObjectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            log.info("✅ Fichier uploadé: {} dans bucket: {}", finalObjectName, bucketName);
            return uuid;

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'upload du fichier", e);
            throw new RuntimeException("Erreur d'upload du fichier: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Télécharge un fichier depuis MinIO
     *
     * @param bucketName Le nom du bucket
     * @param objectName Le nom de l'objet
     * @return InputStream du fichier
     */
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Erreur lors du téléchargement du fichier: {}", objectName, e);
            throw new RuntimeException("Erreur de téléchargement du fichier: " + objectName, e);
        }
    }

    /**
     * Récupère le content type d'un fichier
     *
     * @param bucketName Le nom du bucket
     * @param objectName Le nom de l'objet
     * @return Content type
     */
    public String getContentType(String bucketName, String objectName) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return stat.contentType();
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération du content type: {}", objectName, e);
            return "application/octet-stream";
        }
    }

    /**
     * Génère une URL de téléchargement présignée (valide 7 jours)
     *
     * @param bucketName Le nom du bucket
     * @param objectName Le nom de l'objet
     * @return URL présignée
     */
    public String getPresignedDownloadUrl(String bucketName, String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(7, TimeUnit.DAYS)
                            .build()
            );
        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération de l'URL présignée: {}", objectName, e);
            throw new RuntimeException("Erreur de génération d'URL: " + objectName, e);
        }
    }

    /**
     * Supprime un fichier de MinIO
     *
     * @param bucketName Le nom du bucket
     * @param objectName Le nom de l'objet
     */
    public void deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("🗑️ Fichier supprimé: {} du bucket: {}", objectName, bucketName);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la suppression du fichier: {}", objectName, e);
            throw new RuntimeException("Erreur de suppression du fichier: " + objectName, e);
        }
    }

    /**
     * Extrait l'extension d'un fichier
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Construit le chemin complet d'un fichier dans MinIO
     */
    public String buildObjectPath(String folder, String uuid, String extension) {
        return folder + "/" + uuid + extension;
    }
}