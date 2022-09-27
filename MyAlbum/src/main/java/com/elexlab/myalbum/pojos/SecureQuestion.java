package com.elexlab.myalbum.pojos;

/**
 * Created by BruceYoung on 10/20/17.
 */
public class SecureQuestion {
    private int type;
    private String content;
    private String answer;

    public SecureQuestion() {
    }

    public SecureQuestion(int type, String content) {
        this.type = type;
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
