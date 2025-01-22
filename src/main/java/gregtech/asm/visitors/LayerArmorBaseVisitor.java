package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@Deprecated
public class LayerArmorBaseVisitor extends MethodVisitor implements Opcodes {

    public static final String TARGET_CLASS_NAME = "net/minecraft/client/renderer/entity/layers/LayerArmorBase";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_188361_a",
            "(Lnet/minecraft/entity/EntityLivingBase;FFFFFFFLnet/minecraft/inventory/EntityEquipmentSlot;)V");

    private static final String ARMOR_HOOKS_OWNER = "gregtech/asm/hooks/ArmorRenderHooks";
    private static final String ARMOR_HOOKS_SIGNATURE = "(Lnet/minecraft/client/renderer/entity/layers/LayerArmorBase;Lnet/minecraft/entity/EntityLivingBase;FFFFFFFLnet/minecraft/inventory/EntityEquipmentSlot;)V";
    private static final String ARMOR_HOOKS_METHOD_NAME = "renderArmorLayer";

    public LayerArmorBaseVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    @Override
    public void visitInsn(int opcode) {
        if (opcode == Opcodes.RETURN) {
            super.visitVarInsn(ALOAD, 0); // this
            super.visitVarInsn(ALOAD, 1); // entityLivingBaseIn
            for (int i = 0; i < 7; i++)
                super.visitVarInsn(FLOAD, 2 + i); // limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
                                                  // headPitch, scale
            super.visitVarInsn(ALOAD, 9); // slotIn
            super.visitMethodInsn(INVOKESTATIC, ARMOR_HOOKS_OWNER, ARMOR_HOOKS_METHOD_NAME, ARMOR_HOOKS_SIGNATURE,
                    false);
        }
        super.visitInsn(opcode);
    }
}
