package org.research.player.inventory;


/**
 * container负责给PlayerTechTreeData初始化，
 * PlayerTechData负责做步进操作，
 */
@Deprecated
public abstract class AbstractResearchContainer implements IResearchContainer {


//    private HashMap<ResourceLocation, TechInstance> techMap = new HashMap<>();
//    private HashMap<ResourceLocation, Vector2i> vecMap = new HashMap<>();
//
//    private Player player;
//    private PlayerTechTreeData playerTechTreeData;
//
//
//
//    protected abstract void initTechs();
//
//
//
//    public AbstractResearchContainer(ServerPlayer player, PlayerTechTreeData data) {
//        this.player = player;
//        this.playerTechTreeData = data;
//        initTechs();
//    }
//
//    public AbstractResearchContainer(PlayerTechTreeData data) {
//        this.playerTechTreeData = data;
//        this.player = null;
//        initTechs();
//    }
//
//
//    public void addTech(AbstractTech tech, int x, int y) {
//
//
//
//    }
//
//
//
//    public HashMap<ResourceLocation, TechInstance> getTechMap() {
//        return techMap;
//    }
//
//    public HashMap<ResourceLocation, Vector2i> getVecMap() {
//        return vecMap;
//    }
}
