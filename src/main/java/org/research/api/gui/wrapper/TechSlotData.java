package org.research.api.gui.wrapper;

import lombok.Getter;
import org.research.gui.minecraft.component.TechSlot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TechSlotData {

    private volatile boolean dirty = false;
    private final List<TechSlot> cache = new ArrayList<>();
    private List<TechSlot> snapshot = Collections.emptyList();

    @Getter
    private TechSlot focusTechSlot = TechSlot.EMPTY;

    private int cachedDataHash = 0;

    //科技分组数据
    private final List<TechSlot> searchResults = new ArrayList<>();
    private final List<TechSlot> currentPageSlots = new ArrayList<>();
    private int currentPage = 0;
    private boolean isSearchMode = false;

    // 每组的槽位数量
    public static final int SLOTS_PER_PAGE = 20;


    public synchronized void setCachedTechSlots(List<TechSlot> techSlots) {
        cache.clear();
        if (techSlots != null && !techSlots.isEmpty()) {
            cache.addAll(techSlots);
        }
        markDirty();
    }

    public synchronized void addTechSlot(TechSlot techSlot) {
        if (techSlot != null) {
            cache.add(techSlot);
            markDirty();
        }
    }

    public synchronized boolean removeTechSlot(TechSlot techSlot) {
        if (cache.remove(techSlot)) {
            markDirty();
            return true;
        }
        return false;
    }

    public synchronized void clearTechSlots() {
        if (!cache.isEmpty()) {
            cache.clear();
            markDirty();
        }
        cachedDataHash = 0;
    }

    public boolean isHashMatched(int currentHash) {
        return cachedDataHash != 0 && cachedDataHash == currentHash;
    }

    public void updateHash(int newHash) {
        this.cachedDataHash = newHash;
    }

    public int getCachedHash() {
        return cachedDataHash;
    }

    public void resetHash() {
        this.cachedDataHash = 0;
    }

    private void markDirty() {
        dirty = true;
    }

    public List<TechSlot> getCachedTechSlots() {
        if (!dirty) {
            return snapshot;
        }
        synchronized (this) {
            if (dirty) {
                snapshot = List.copyOf(cache);
                dirty = false;
            }
            return snapshot;
        }
    }

    public void clearFocus() {
        if (focusTechSlot != TechSlot.EMPTY) {
            focusTechSlot.setClientFocused(false);
        }
        this.focusTechSlot = TechSlot.EMPTY;
    }

    public void setFocusTechSlot(TechSlot techSlot) {
        if (focusTechSlot != TechSlot.EMPTY) {
            focusTechSlot.setClientFocused(false);
        }
        if (techSlot == null || techSlot == TechSlot.EMPTY) {
            this.focusTechSlot = TechSlot.EMPTY;
        } else {
            this.focusTechSlot = techSlot;
            techSlot.setClientFocused(true);
        }
    }

    public boolean isFocused(TechSlot techSlot) {
        return techSlot != null && focusTechSlot != TechSlot.EMPTY && focusTechSlot.equals(techSlot);
    }

    public int size() {
        return cache.size();
    }

    public boolean isEmpty() {
        return cache.isEmpty();
    }

    public synchronized void initializePositionsWithVecMap(Object vecMap, int guiLeftOffset, int guiTopOffset) {
        if (vecMap == null || isEmpty() || !(vecMap instanceof Map)) {
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) vecMap;

            for (var techSlot : cache) {
                var techId = techSlot.getTechInstance().getIdentifier();
                var vec = map.get(techId);
                if (vec != null) {
                    int x = (int) vec.getClass().getMethod("x").invoke(vec);
                    int y = (int) vec.getClass().getMethod("y").invoke(vec);
                    techSlot.setPosition(guiLeftOffset + x, guiTopOffset + y);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行搜索并更新搜索结果
     * @param searchText 搜索文本（可以是任何语言）
     */
    public synchronized void performSearch(String searchText) {
        searchResults.clear();
        currentPage = 0;
        isSearchMode = true;

        if (searchText == null || searchText.trim().isEmpty()) {
            // 空搜索，显示按首字母排序的所有物品
            populateAllItemsSorted();
            updateCurrentPageSlots();
            return;
        }

        String lowerSearchText = searchText.toLowerCase().trim();

        // 遍历所有科技槽位进行搜索
        for (TechSlot slot : cache) {
            if (slot == null || slot.getTechInstance().isEmpty()) {
                continue;
            }

            // 获取配方输出物品的本地化名称进行匹配
            var recipe = slot.getRecipe();
            if (recipe != null) {
                var minecraft = net.minecraft.client.Minecraft.getInstance();
                if (minecraft.getConnection() != null) {
                    var resultItem = recipe.getResultItem(minecraft.getConnection().registryAccess());
                    if (!resultItem.isEmpty()) {
                        // 获取物品的本地化名称（自动适配当前语言）
                        String itemName = resultItem.getHoverName().getString().toLowerCase();
                        if (itemName.contains(lowerSearchText)) {
                            searchResults.add(slot);
                        }
                    }
                }
            }
        }

        updateCurrentPageSlots();
    }

    /**
     * 填充所有物品并按首字母排序
     */
    private void populateAllItemsSorted() {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return;
        }

        // 收集所有有效的槽位
        java.util.List<TechSlot> validSlots = new java.util.ArrayList<>();
        for (TechSlot slot : cache) {
            if (slot == null || slot.getTechInstance().isEmpty()) {
                continue;
            }

            var recipe = slot.getRecipe();
            if (recipe != null) {
                var resultItem = recipe.getResultItem(minecraft.getConnection().registryAccess());
                if (!resultItem.isEmpty()) {
                    validSlots.add(slot);
                }
            }
        }

        // 按物品名称首字母排序
        validSlots.sort((slot1, slot2) -> {
            try {
                var recipe1 = slot1.getRecipe();
                var recipe2 = slot2.getRecipe();
                if (recipe1 == null || recipe2 == null) return 0;

                var item1 = recipe1.getResultItem(minecraft.getConnection().registryAccess());
                var item2 = recipe2.getResultItem(minecraft.getConnection().registryAccess());

                if (item1.isEmpty() || item2.isEmpty()) return 0;

                String name1 = item1.getHoverName().getString().toLowerCase();
                String name2 = item2.getHoverName().getString().toLowerCase();

                return name1.compareTo(name2);
            } catch (Exception e) {
                return 0;
            }
        });

        searchResults.addAll(validSlots);
    }

    /**
     * 切换到下一页或上一页
     * @param next true表示下一页，false表示上一页
     * @return 是否成功切换
     */
    public synchronized boolean changePage(boolean next) {
        if (!isSearchMode || searchResults.isEmpty()) {
            return false;
        }

        int totalPages = getTotalPages();
        int newPage = next ? currentPage + 1 : currentPage - 1;

        if (newPage < 0 || newPage >= totalPages) {
            return false; // 超出范围
        }

        currentPage = newPage;
        updateCurrentPageSlots();
        return true;
    }

    /**
     * 更新当前页的槽位列表
     */
    private void updateCurrentPageSlots() {
        currentPageSlots.clear();

        if (!isSearchMode || searchResults.isEmpty()) {
            return;
        }

        int startIndex = currentPage * SLOTS_PER_PAGE;
        int endIndex = Math.min(startIndex + SLOTS_PER_PAGE, searchResults.size());

        for (int i = startIndex; i < endIndex; i++) {
            currentPageSlots.add(searchResults.get(i));
        }
    }

    /**
     * 获取当前页的槽位列表
     * @return 当前页的槽位列表
     */
    public List<TechSlot> getCurrentPageSlots() {
        return new ArrayList<>(currentPageSlots);
    }

    /**
     * 根据ID获取槽位（用于搜索结果页面）
     * @param id 槽位在当前页的索引（0-19）
     * @return 对应的TechSlot，如果不存在返回EMPTY
     */
    public TechSlot getSlotById(int id) {
        if (!isSearchMode || id < 0 || id >= currentPageSlots.size()) {
            return TechSlot.EMPTY;
        }
        return currentPageSlots.get(id);
    }

    /**
     * 获取总页数
     * @return 总页数
     */
    public int getTotalPages() {
        if (searchResults.isEmpty()) {
            return 0;
        }
        return (int) Math.ceil((double) searchResults.size() / SLOTS_PER_PAGE);
    }

    /**
     * 获取当前页码（从0开始）
     * @return 当前页码
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * 是否处于搜索模式
     * @return true表示在搜索模式
     */
    public boolean isSearchMode() {
        return isSearchMode;
    }

    /**
     * 是否有下一页
     * @return true表示有下一页
     */
    public boolean hasNextPage() {
        return isSearchMode && currentPage < getTotalPages() - 1;
    }

    /**
     * 是否有上一页
     * @return true表示有上一页
     */
    public boolean hasPreviousPage() {
        return isSearchMode && currentPage > 0;
    }

    /**
     * 清除搜索结果并退出搜索模式
     */
    public synchronized void clearSearch() {
        searchResults.clear();
        currentPageSlots.clear();
        currentPage = 0;
        isSearchMode = false;
    }
}
