package gregtech.api.util;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.util.function.Function;

/**
 * Relative direction when facing horizontally
 */
public enum RelativeDirection {

    UP(f -> EnumFacing.UP),
    DOWN(f -> EnumFacing.DOWN),
    LEFT(EnumFacing::rotateYCCW),
    RIGHT(EnumFacing::rotateY),
    FRONT(Function.identity()),
    BACK(EnumFacing::getOpposite);

    final Function<EnumFacing, EnumFacing> actualFacing;

    RelativeDirection(Function<EnumFacing, EnumFacing> actualFacing) {
        this.actualFacing = actualFacing;
    }

    public EnumFacing getActualFacing(EnumFacing facing) {
        return actualFacing.apply(facing);
    }

    public EnumFacing apply(EnumFacing facing) {
        return actualFacing.apply(facing);
    }

    public Vec3i applyVec3i(EnumFacing facing) {
        return apply(facing).getDirectionVec();
    }

    public EnumFacing getRelativeFacing(EnumFacing frontFacing, EnumFacing upwardsFacing, boolean isFlipped) {
        EnumFacing.Axis frontAxis = frontFacing.getAxis();
        return switch (this) {
            case UP -> {
                if (frontAxis == Axis.Y) {
                    // same direction as upwards facing
                    yield upwardsFacing;
                } else {
                    // transform the upwards facing into a real facing
                    yield switch (upwardsFacing) {
                        case NORTH -> EnumFacing.UP;
                        case SOUTH -> EnumFacing.DOWN;
                        case EAST -> frontFacing.rotateYCCW();
                        default -> frontFacing.rotateY(); // WEST
                    };
                }
            }
            case DOWN -> {
                if (frontAxis == Axis.Y) {
                    // opposite direction as upwards facing
                    yield upwardsFacing.getOpposite();
                } else {
                    // transform the upwards facing into a real facing
                    yield switch (upwardsFacing) {
                        case NORTH -> EnumFacing.DOWN;
                        case SOUTH -> EnumFacing.UP;
                        case EAST -> frontFacing.rotateY();
                        default -> frontFacing.rotateYCCW(); // WEST
                    };
                }
            }
            case LEFT -> {
                EnumFacing facing;
                if (frontAxis == Axis.Y) {
                    facing = upwardsFacing.rotateY();
                } else {
                    facing = switch (upwardsFacing) {
                        case NORTH -> frontFacing.rotateYCCW();
                        case SOUTH -> frontFacing.rotateY();
                        case EAST -> EnumFacing.DOWN;
                        default -> EnumFacing.UP; // WEST
                    };
                }
                yield isFlipped ? facing.getOpposite() : facing;
            }
            case RIGHT -> {
                EnumFacing facing;
                if (frontAxis == Axis.Y) {
                    facing = upwardsFacing.rotateYCCW();
                } else {
                    facing = switch (upwardsFacing) {
                        case NORTH -> frontFacing.rotateY();
                        case SOUTH -> frontFacing.rotateYCCW();
                        case EAST -> EnumFacing.UP;
                        default -> EnumFacing.DOWN; // WEST
                    };
                }
                // invert if flipped
                yield isFlipped ? facing.getOpposite() : facing;
            }
            // same direction as front facing, upwards facing doesn't matter
            case FRONT -> frontFacing;
            // opposite direction as front facing, upwards facing doesn't matter
            case BACK -> frontFacing.getOpposite();
        };
    }

    public Function<BlockPos, Integer> getSorter(EnumFacing frontFacing, EnumFacing upwardsFacing, boolean isFlipped) {
        // get the direction to go in for the part sorter
        EnumFacing sorterDirection = getRelativeFacing(frontFacing, upwardsFacing, isFlipped);

        // Determined by EnumFacing Axis + AxisDirection
        return switch (sorterDirection) {
            case UP -> BlockPos::getY;
            case DOWN -> pos -> -pos.getY();
            case EAST -> BlockPos::getX;
            case WEST -> pos -> -pos.getX();
            case NORTH -> pos -> -pos.getZ();
            case SOUTH -> BlockPos::getZ;
        };
    }

    /**
     * Simulates rotating the controller around an axis to get to a new front facing.
     *
     * @return Returns the new upwards facing.
     */
    public static EnumFacing simulateAxisRotation(EnumFacing newFrontFacing, EnumFacing oldFrontFacing,
                                                  EnumFacing upwardsFacing) {
        if (newFrontFacing == oldFrontFacing) return upwardsFacing;

        EnumFacing.Axis newAxis = newFrontFacing.getAxis();
        EnumFacing.Axis oldAxis = oldFrontFacing.getAxis();

        if (newAxis != Axis.Y && oldAxis != Axis.Y) {
            // no change needed
            return upwardsFacing;
        } else if (newAxis == Axis.Y && oldAxis != Axis.Y) {
            // going from horizontal to vertical axis
            EnumFacing newUpwardsFacing = switch (upwardsFacing) {
                case NORTH -> oldFrontFacing.getOpposite();
                case SOUTH -> oldFrontFacing;
                case EAST -> oldFrontFacing.rotateYCCW();
                default -> oldFrontFacing.rotateY(); // WEST
            };
            return newFrontFacing == EnumFacing.DOWN && upwardsFacing.getAxis() == Axis.Z ?
                    newUpwardsFacing.getOpposite() : newUpwardsFacing;
        } else if (newAxis != Axis.Y) {
            // going from vertical to horizontal axis
            EnumFacing newUpwardsFacing;
            if (upwardsFacing == newFrontFacing.getOpposite()) {
                newUpwardsFacing = EnumFacing.NORTH;
            } else if (upwardsFacing == newFrontFacing) {
                newUpwardsFacing = EnumFacing.SOUTH;
            } else if (upwardsFacing == newFrontFacing.rotateY()) {
                newUpwardsFacing = EnumFacing.WEST;
            } else { // rotateYCCW
                newUpwardsFacing = EnumFacing.EAST;
            }
            return oldFrontFacing == EnumFacing.DOWN && newUpwardsFacing.getAxis() == Axis.Z ?
                    newUpwardsFacing.getOpposite() : newUpwardsFacing;
        } else {
            // was on vertical axis and still is. Must have flipped from up to down or vice versa
            return upwardsFacing.getOpposite();
        }
    }

    /**
     * Offset a BlockPos relatively in any direction by any amount. Pass negative values to offset down, right or
     * backwards.
     */
    public static BlockPos offsetPos(BlockPos pos, EnumFacing frontFacing, EnumFacing upwardsFacing, boolean isFlipped,
                                     int upOffset, int leftOffset, int forwardOffset) {
        if (upOffset == 0 && leftOffset == 0 && forwardOffset == 0) {
            return pos;
        }

        int oX = 0, oY = 0, oZ = 0;
        final EnumFacing relUp = UP.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
        oX += relUp.getXOffset() * upOffset;
        oY += relUp.getYOffset() * upOffset;
        oZ += relUp.getZOffset() * upOffset;

        final EnumFacing relLeft = LEFT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
        oX += relLeft.getXOffset() * leftOffset;
        oY += relLeft.getYOffset() * leftOffset;
        oZ += relLeft.getZOffset() * leftOffset;

        final EnumFacing relForward = FRONT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
        oX += relForward.getXOffset() * forwardOffset;
        oY += relForward.getYOffset() * forwardOffset;
        oZ += relForward.getZOffset() * forwardOffset;

        return pos.add(oX, oY, oZ);
    }

    /**
     * Offset a BlockPos relatively in any direction by any amount. Pass negative values to offset down, right or
     * backwards.
     */
    public static BlockPos setActualRelativeOffset(int x, int y, int z, EnumFacing facing, EnumFacing upwardsFacing,
                                                   boolean isFlipped, RelativeDirection[] structureDir) {
        int[] c0 = new int[] { x, y, z }, c1 = new int[3];
        if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
            EnumFacing of = facing == EnumFacing.DOWN ? upwardsFacing : upwardsFacing.getOpposite();
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualFacing(of)) {
                    case UP -> c1[1] = c0[i];
                    case DOWN -> c1[1] = -c0[i];
                    case WEST -> c1[0] = -c0[i];
                    case EAST -> c1[0] = c0[i];
                    case NORTH -> c1[2] = -c0[i];
                    case SOUTH -> c1[2] = c0[i];
                }
            }
            int xOffset = upwardsFacing.getXOffset();
            int zOffset = upwardsFacing.getZOffset();
            int tmp;
            if (xOffset == 0) {
                tmp = c1[2];
                c1[2] = zOffset > 0 ? c1[1] : -c1[1];
                c1[1] = zOffset > 0 ? -tmp : tmp;
            } else {
                tmp = c1[0];
                c1[0] = xOffset > 0 ? c1[1] : -c1[1];
                c1[1] = xOffset > 0 ? -tmp : tmp;
            }
            if (isFlipped) {
                if (upwardsFacing == EnumFacing.NORTH || upwardsFacing == EnumFacing.SOUTH) {
                    c1[0] = -c1[0]; // flip X-axis
                } else {
                    c1[2] = -c1[2]; // flip Z-axis
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                switch (structureDir[i].getActualFacing(facing)) {
                    case UP -> c1[1] = c0[i];
                    case DOWN -> c1[1] = -c0[i];
                    case WEST -> c1[0] = -c0[i];
                    case EAST -> c1[0] = c0[i];
                    case NORTH -> c1[2] = -c0[i];
                    case SOUTH -> c1[2] = c0[i];
                }
            }
            if (upwardsFacing == EnumFacing.WEST || upwardsFacing == EnumFacing.EAST) {
                int xOffset = upwardsFacing == EnumFacing.WEST ? facing.rotateY().getXOffset() :
                        facing.rotateY().getOpposite().getXOffset();
                int zOffset = upwardsFacing == EnumFacing.WEST ? facing.rotateY().getZOffset() :
                        facing.rotateY().getOpposite().getZOffset();
                int tmp;
                if (xOffset == 0) {
                    tmp = c1[2];
                    c1[2] = zOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = zOffset > 0 ? tmp : -tmp;
                } else {
                    tmp = c1[0];
                    c1[0] = xOffset > 0 ? -c1[1] : c1[1];
                    c1[1] = xOffset > 0 ? tmp : -tmp;
                }
            } else if (upwardsFacing == EnumFacing.SOUTH) {
                c1[1] = -c1[1];
                if (facing.getXOffset() == 0) {
                    c1[0] = -c1[0];
                } else {
                    c1[2] = -c1[2];
                }
            }
            if (isFlipped) {
                if (upwardsFacing == EnumFacing.NORTH || upwardsFacing == EnumFacing.SOUTH) {
                    if (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) {
                        c1[0] = -c1[0]; // flip X-axis
                    } else {
                        c1[2] = -c1[2]; // flip Z-axis
                    }
                } else {
                    c1[1] = -c1[1]; // flip Y-axis
                }
            }
        }
        return new BlockPos(c1[0], c1[1], c1[2]);
    }
}
