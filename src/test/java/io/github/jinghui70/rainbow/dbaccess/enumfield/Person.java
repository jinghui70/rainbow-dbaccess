package io.github.jinghui70.rainbow.dbaccess.enumfield;

public class Person {

    private int id;

    // 用来测试字符保存的枚举
    private Strength arm;

    // 用来测试数字保存的枚举
    private Strength leg;

    // 用啦测试用code保存的枚举，这种情况可以不写 @Column
    private StrengthCode foot;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Strength getArm() {
        return arm;
    }

    public void setArm(Strength arm) {
        this.arm = arm;
    }

    public Strength getLeg() {
        return leg;
    }

    public void setLeg(Strength leg) {
        this.leg = leg;
    }

    public StrengthCode getFoot() {
        return foot;
    }

    public void setFoot(StrengthCode foot) {
        this.foot = foot;
    }
}
