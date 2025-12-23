package org.research.api.tech.capability;

import net.minecraft.resources.ResourceLocation;

public interface ITechTreeCapability {

    public void nextNode();

    public void complete(ResourceLocation techId);


}
