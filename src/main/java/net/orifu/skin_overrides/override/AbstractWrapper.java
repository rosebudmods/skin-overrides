package net.orifu.skin_overrides.override;

import java.util.ArrayList;
import java.util.List;

import com.mojang.authlib.GameProfile;

public abstract class AbstractWrapper<E1, T1, E2, T2, O1 extends AbstractOverride<E1, T1>, O2 extends AbstractOverride<E2, T2>> {
    protected final O1 override1;
    protected final O2 override2;

    public AbstractWrapper(O1 override1, O2 override2) {
        this.override1 = override1;
        this.override2 = override2;
    }

    public boolean hasOverride(GameProfile profile) {
        return this.override1.hasOverride(profile) || this.override2.hasOverride(profile);
    }

    public void removeOverride(GameProfile profile) {
        this.override1.removeOverride(profile);
        this.override2.removeOverride(profile);
    }

    public List<GameProfile> profilesWithOverride() {
        var li = new ArrayList<>(this.override1.profilesWithOverride());
        li.addAll(this.override2.profilesWithOverride());
        return li;
    }
}
