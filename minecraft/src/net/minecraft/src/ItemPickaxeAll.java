package net.minecraft.src;

public class ItemPickaxeAll extends ItemTool {
	private static Block[] blocksEffectiveAgainst = new Block[]{
		Block.cobblestone, Block.stairDouble, Block.stairSingle, Block.stone,
		Block.sandStone, Block.cobblestoneMossy, Block.oreIron, Block.blockSteel,
		Block.oreCoal, Block.blockGold, Block.oreGold, Block.oreDiamond,
		Block.blockDiamond, Block.ice, Block.netherrack, Block.oreLapis,
		Block.blockLapis, Block.obsidian, Block.bedrock
	};

	public ItemPickaxeAll(int itemId) {
		super(itemId, 2, EnumToolMaterial.EMERALD, blocksEffectiveAgainst);
		this.setMaxDamage(0); // Неразрушимая
	}

	@Override
	public boolean canHarvestBlock(Block block) {
		// Может ломать ВСЁ, включая бедрок
		return true;
	}

	@Override
	public float getStrVsBlock(ItemStack itemStack, Block block) {
		// Мгновенная добыча любого блока
		return 1000.0F;
	}

	@Override
	public boolean onBlockDestroyed(ItemStack itemStack, int blockId, int x, int y, int z, EntityLiving entity) {
		// Не получает урон
		return true;
	}
}
