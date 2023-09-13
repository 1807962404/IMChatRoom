package edu.hniu.imchatroom.model.bean.messages;

import edu.hniu.imchatroom.model.bean.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 意见反馈
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackMessage extends Message implements Serializable {

    private static final long serialVersionUID = 1231242342341231L;
    private Long fbId;
    private User publisher;
}
