package com.jme3.gde.templates.downloadedproject;


/**
 *
 * @author rickard
 */
public class Config {
    
    private static final String PROJECT_NAME = "JaimesAscent";
    private static final String GITHUB_REF = "JaimesAscent-1.1.1/";
    private static final String ZIP_NAME = "JaimesAscent.zip";
    private static final String GITHUB_PATH = "https://github.com/neph1/JaimesAscent/archive/refs/tags/v1.1.1.zip";
    
    
    
    public String getProjectName() {
        return PROJECT_NAME;
    }
    
    public String getGithubRef() {
        return GITHUB_REF;
    }
    
    public String getZipName() {
        return ZIP_NAME;
    }
    
    public String getGithubPath() {
        return GITHUB_PATH;
    }
}
