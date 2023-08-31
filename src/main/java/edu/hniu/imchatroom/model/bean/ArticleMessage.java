package edu.hniu.imchatroom.model.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 管理员发布优质文摘语录
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleMessage extends Message implements Serializable {

    private static final long serialVersionUID = 123123123324241L;
    private Integer aId;
    private User publisher;
}
