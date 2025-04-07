package cc.olek.webshop.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class UserProfile {
    @Column(length = 50)
    public String fullName;
    @Column(length = 20)
    public String phoneNumber;

    public void merge(UserProfile profile) {
        if(profile.fullName != null) {
            this.fullName = profile.fullName;
        }
        if(profile.phoneNumber != null) {
            this.phoneNumber = profile.phoneNumber;
        }
    }
}
