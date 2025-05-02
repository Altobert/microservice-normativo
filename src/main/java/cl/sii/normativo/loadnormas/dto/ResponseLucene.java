package cl.sii.normativo.loadnormas.dto;

import java.io.Serializable;

public class ResponseLucene implements Serializable{

    private static final long serialVersionUID = 1L;
    
    private String filename;
    private String title;
    private String content;
    private String path;
    private String lastModified;
    private String size;

    public ResponseLucene(String filename, String title, String content, String path, String lastModified, String size) {
        this.filename = filename;
        this.title = title;
        this.content = content;
        this.path = path;
        this.lastModified = lastModified;
        this.size = size;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }


}
