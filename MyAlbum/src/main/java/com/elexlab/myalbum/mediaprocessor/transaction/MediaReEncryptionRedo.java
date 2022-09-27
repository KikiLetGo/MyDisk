package com.elexlab.myalbum.mediaprocessor.transaction;


import com.elexlab.myalbum.pojos.Media;

/**
 * Created by BruceYoung on 10/25/17.
 */
public class MediaReEncryptionRedo extends Redo{

    public MediaReEncryptionRedo(Media media, int action) {
        super(media, action);
    }
    private String oldPassword;
    private String newPassword;

    public MediaReEncryptionRedo(Media media,String oldPassword, String newPassword) {
        super(media, Action.RE_ENCRYPTION);
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }
}
