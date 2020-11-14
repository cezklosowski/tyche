package com.cezklosowski.module.media.storage;

import com.cezklosowski.module.media.repository.MediaEntity;
import com.cezklosowski.module.media.repository.MediaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FileSystemStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemStorage.class);

    @Value("${mediaRootLocation}")
    private String path;

    @Autowired
    private MediaRepository mediaRepository;

    public void store(MultipartFile file){
        try {
            LOGGER.info("File {} uploading", file.getOriginalFilename());


            Path destinationFile = Paths.get(path)
                    .resolve(Paths.get(file.getOriginalFilename()))
                    .normalize()
                    .toAbsolutePath();


            try (InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            LOGGER.info("File {} saved on disk", file.getOriginalFilename());

            mediaRepository.saveAndFlush(
                    new MediaEntity(file.getOriginalFilename())
            );

            LOGGER.info("File {} metadata saved in db", file.getOriginalFilename());

        } catch (IOException e) {
            LOGGER.error("File saving failed {}", e.getLocalizedMessage());
            throw new StorageException("Failed to store file", e);
        }
    }

    public List<MediaEntity> list(){
        return mediaRepository.findAll();
    }
}
