package com.projekt.services;

import com.projekt.models.Version;
import org.springframework.stereotype.Service;

@Service
public interface VersionService {
    Version save(Version version);

    void delete(Integer versionID);
}
