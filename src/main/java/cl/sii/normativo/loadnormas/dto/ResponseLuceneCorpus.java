package cl.sii.normativo.loadnormas.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @ToString
public class ResponseLuceneCorpus implements Serializable{

    private static final long serialVersionUID = 1L;
    
    private String filename;
    private String title;
    private String content;
    private String path;
    private String lastModified;
    private String size;

    
}
