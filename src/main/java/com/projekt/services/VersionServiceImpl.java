package com.projekt.services;

import com.projekt.models.Version;
import com.projekt.repositories.VersionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("versionService")
public class VersionServiceImpl implements VersionService{
    @Autowired
    private VersionRepository versionRepository;

    @Override
    public Version save(Version version) {
        versionRepository.save(version);
        return version;
    }

    @Override
    public void delete(Integer versionID) {
        versionRepository.deleteById(versionID);
    }
}
