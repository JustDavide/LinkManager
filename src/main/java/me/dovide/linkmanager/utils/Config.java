package me.dovide.linkmanager.utils;

import me.dovide.utils.Util;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.stream.Collectors;

public class Config extends YamlConfiguration {

    @Override
    public String getString(String text) {
        return Util.cc(super.getString(text));
    }

    @Override
    public List<String> getStringList(String path) {
        return super.getStringList(path).stream().map(Util::cc)
                .collect(Collectors.toList());
    }

}