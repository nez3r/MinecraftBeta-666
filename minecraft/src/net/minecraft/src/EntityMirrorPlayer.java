package net.minecraft.src;

public class EntityMirrorPlayer extends EntityOtherPlayerMP {
    private EntityPlayer targetPlayer;
    private long spawnTime;

    public EntityMirrorPlayer(World world, EntityPlayer target) {
        super(world, "Player404");
        this.targetPlayer = target;
        this.spawnTime = System.currentTimeMillis();

        // Спавним далеко от игрока (30-50 блоков)
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
    }

    public void onUpdate() {
        super.onUpdate();

        // Проверяем дистанцию до игрока
        if (targetPlayer != null) {
            double distance = this.getDistanceToEntity(targetPlayer);

            // Если игрок подошел ближе 10 блоков - исчезаем со звуком
            if (distance < 10.0D) {
                this.worldObj.playSoundAtEntity(this, "random.break", 2.0F, 0.5F);
                this.setEntityDead();
            }
        }

        // Автоматически исчезаем через 2 минуты
        if (System.currentTimeMillis() - spawnTime > 120000) {
            this.setEntityDead();
        }
    }
}
