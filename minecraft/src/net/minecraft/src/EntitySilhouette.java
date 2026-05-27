package net.minecraft.src;

public class EntitySilhouette extends EntityLiving {
    private long spawnTime;
    private EntityPlayer targetPlayer;

    public EntitySilhouette(World world, EntityPlayer target) {
        super(world);
        this.targetPlayer = target;
        this.spawnTime = System.currentTimeMillis();

        // Спавним на расстоянии 30-50 блоков от игрока
        double distance = 30 + world.rand.nextDouble() * 20;
        double angle = world.rand.nextDouble() * Math.PI * 2;

        this.setPosition(
            target.posX + Math.cos(angle) * distance,
            target.posY,
            target.posZ + Math.sin(angle) * distance
        );

        // Смотрим на игрока
        double dx = target.posX - this.posX;
        double dz = target.posZ - this.posZ;
        this.rotationYaw = (float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;

        // Устанавливаем размеры как у игрока
        this.setSize(0.6F, 1.8F);
        this.yOffset = 1.62F;
    }

    public void onLivingUpdate() {
        super.onLivingUpdate();

        // Исчезаем через 3 секунды
        if (System.currentTimeMillis() - spawnTime > 3000) {
            this.setEntityDead();
        }

        // Всегда смотрим на игрока
        if (targetPlayer != null && !targetPlayer.isDead) {
            double dx = targetPlayer.posX - this.posX;
            double dz = targetPlayer.posZ - this.posZ;
            this.rotationYaw = (float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
        }
    }

    protected String getLivingSound() {
        return null;
    }

    protected String getHurtSound() {
        return null;
    }

    protected String getDeathSound() {
        return null;
    }

    public boolean canBePushed() {
        return false;
    }

    protected boolean canTriggerWalking() {
        return false;
    }

    public boolean canBeCollidedWith() {
        return false;
    }
}
