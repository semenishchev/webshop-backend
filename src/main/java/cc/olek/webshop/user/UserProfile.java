package cc.olek.webshop.user;

import jakarta.persistence.Embeddable;

@Embeddable
public class UserProfile {
    public String fullName;
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
