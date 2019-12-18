package io.alerium.lootbags.data;

public class Reward {

    private String reward;
    private Integer chance;

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
