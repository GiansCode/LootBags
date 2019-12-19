package io.alerium.lootbags.data;

public class Reward {

    private final String reward;
    private final Integer chance;

    public Reward(String reward, Integer chance) {
        this.reward = reward;
        this.chance = chance;
    }

    public String getReward() {
        return reward;
    }

    public Integer getChance() {
        return chance;
    }
}
