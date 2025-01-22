package gregtech.asm.visitors;

import gregtech.asm.util.ObfMapping;
import gregtech.asm.util.SafeMethodVisitor;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

@Deprecated
public class LayerCustomHeadVisitor extends SafeMethodVisitor {

    public static final String TARGET_CLASS_NAME = "net/minecraft/client/renderer/entity/layers/LayerCustomHead";
    public static final ObfMapping TARGET_METHOD = new ObfMapping(TARGET_CLASS_NAME, "func_177141_a",
            "(Lnet/minecraft/entity/EntityLivingBase;FFFFFFF)V");

    private static final String METHOD_OWNER = "net/minecraft/client/renderer/ItemRenderer";
    private static final String METHOD_SIGNATURE = "(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemCameraTransforms$TransformType;)V";
    private static final String METHOD_NAME = "func_178099_a";
    private static final ObfMapping METHOD_MAPPING = new ObfMapping(METHOD_OWNER, METHOD_NAME, METHOD_SIGNATURE)
            .toRuntime();

    private static final String ARMOR_HOOKS_OWNER = "gregtech/asm/hooks/ArmorRenderHooks";
    private static final String ARMOR_HOOKS_SIGNATURE = "(Lnet/minecraft/entity/EntityLivingBase;)Z";
    private static final String ARMOR_HOOKS_METHOD_NAME = "shouldNotRenderHeadItem";

    public LayerCustomHeadVisitor(MethodVisitor mv) {
        super(Opcodes.ASM5, mv);
    }

    private static boolean checkTargetInsn(int opcode, String owner, String name, String desc) {
        return opcode == Opcodes.INVOKEVIRTUAL && METHOD_MAPPING.s_owner.equals(owner) &&
                METHOD_MAPPING.matches(name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (checkTargetInsn(opcode, owner, name, desc)) {
            markPatchedSuccessfully();
            Label endLabel = new Label();
            Label skipLabel = new Label();
            super.visitVarInsn(Opcodes.ALOAD, 1); // load entity
            super.visitMethodInsn(Opcodes.INVOKESTATIC, ARMOR_HOOKS_OWNER, ARMOR_HOOKS_METHOD_NAME,
                    ARMOR_HOOKS_SIGNATURE, false);
            super.visitJumpInsn(Opcodes.IFEQ, skipLabel);
            for (int i = 0; i < 4; i++) super.visitInsn(Opcodes.POP); // pop this, entity, stack, transformType
            super.visitJumpInsn(Opcodes.GOTO, endLabel);
            super.visitLabel(skipLabel);
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            super.visitLabel(endLabel);
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    protected String getInjectTargetString() {
        return String.format("Patch target: %s; injection point: %s; (point not found)", TARGET_METHOD, METHOD_MAPPING);
    }
}
